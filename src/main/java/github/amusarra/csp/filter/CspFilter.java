package github.amusarra.csp.filter;

import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Filter JAX-RS che imposta l'header Content-Security-Policy (o la sua variante
 * Report-Only) sulle risposte HTTP. Genera un nonce per ogni richiesta e lo rende
 * disponibile nella ContainerRequestContext (proprietà "csp-nonce") e tramite
 * l'header di debug "X-CSP-Nonce".
 *
 * La behavior è configurabile tramite le property:
 * - csp.filter.report-only (boolean): usa Content-Security-Policy-Report-Only se true.
 * - csp.filter.override (boolean): se false non sovrascrive header CSP già presenti.
 * - csp.filter.report-uri (String): URI di report per la direttiva report-uri.
 * - csp.filter.default-policy (String, opzionale): policy predefinita che può contenere
 *   il placeholder {nonce} che verrà sostituito con il nonce generato.
 */
@IfBuildProperty(name = "csp.filter.enabled", stringValue = "true")
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CspFilter implements ContainerRequestFilter, ContainerResponseFilter {

  /**
   * Logger della classe.
   */
  private static final Logger LOG = Logger.getLogger(CspFilter.class);

  /**
   * Nome della proprietà nella request dove viene memorizzato il nonce.
   */
  private static final String NONCE_ATTR = "csp-nonce";

  @ConfigProperty(name = "csp.filter.report-only", defaultValue = "false")
  boolean reportOnly;

  @ConfigProperty(name = "csp.filter.override", defaultValue = "false")
  boolean overrideExisting;

  @ConfigProperty(name = "csp.filter.report-uri", defaultValue = "/csp-report")
  String reportUri;

  // Iniettiamo Optional<String> per la default policy: se la property è assente otteniamo Optional.empty()
  @ConfigProperty(name = "csp.filter.default-policy")
  Optional<String> defaultPolicyProp;

  /**
   * Genera un nonce unico per la richiesta e lo memorizza nella
   * ContainerRequestContext sotto la proprietà "csp-nonce".
   *
   * Questo metodo viene invocato all'inizio dell'elaborazione della request,
   * in modo che template o risorse possano recuperare il nonce per includerlo
   * in script/style inline autorizzati.
   *
   * @param requestContext contesto della richiesta corrente
   */
  @Override
  public void filter(ContainerRequestContext requestContext) {
    // Generiamo il nonce già nella fase di request, così è disponibile per il template
    String nonce = UUID.randomUUID().toString().replace("-", "");
    requestContext.setProperty(NONCE_ATTR, nonce);
  }

  /**
   * Applica (o imposta) l'header Content-Security-Policy sulla response.
   * Se è presente la property default-policy, la usa (sostituendo {nonce}),
   * altrimenti costruisce una policy di default che include il nonce generato.
   *
   * Se è impostato reportOnly=true viene usato l'header
   * Content-Security-Policy-Report-Only; se overrideExisting=false non
   * sovrascrive header CSP già presenti.
   *
   * @param requestContext contesto della richiesta (contiene il nonce)
   * @param responseContext contesto della risposta dove viene impostato l'header CSP
   */
  @Override
  public void filter(ContainerRequestContext requestContext,
                     ContainerResponseContext responseContext) {

    // Recuperiamo il nonce impostato nella request (fallback se mancante)
    String nonce = (String) requestContext.getProperty(NONCE_ATTR);
    if (nonce == null) {
      nonce = UUID.randomUUID().toString().replace("-", "");
    }

    // Lo passiamo ancora alla request per poterlo usare nell'HTML (se necessario)
    requestContext.setProperty(NONCE_ATTR, nonce);
    // sempre inviamo un header custom con il nonce (utile per debug e per JS che lo leggesse)
    responseContext.getHeaders().putSingle("X-CSP-Nonce", nonce);

    // Se c'è già un header CSP e overrideExisting==false non sovrascriviamo
    boolean hasCspHeader = responseContext.getHeaders().containsKey("Content-Security-Policy") ||
                           responseContext.getHeaders()
                               .containsKey("Content-Security-Policy-Report-Only");
    if (hasCspHeader && !overrideExisting) {
      LOG.debug(
          "CSP header already present e 'csp.filter.override'=false: non sovrascrivo la policy dell'app.");
      return;
    }

    // Usiamo una sola policy: preferiamo la defaultPolicy se definita, altrimenti costruiamo la policy dinamica
    String defaultPolicy = defaultPolicyProp.filter(s -> !s.isBlank()).orElse(null);

    String policy;
    if (defaultPolicy != null) {
      policy = defaultPolicy.replace("{nonce}", nonce);
    } else {
      // politica di default costruita dinamicamente (include font-src per consentire i font locali/data)
      policy = "default-src 'self'; " +
               "script-src 'self' 'nonce-" + nonce + "' 'strict-dynamic' https:; " +
               "style-src 'self' 'nonce-" + nonce + "' https:; " +
               "font-src 'self' data: https:; " +
               "img-src 'self' data:; " +
               "connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; upgrade-insecure-requests; " +
               "report-uri " + reportUri + ";";
    }

    String headerName =
        reportOnly ? "Content-Security-Policy-Report-Only" : "Content-Security-Policy";

    responseContext.getHeaders().putSingle(headerName, policy);
    LOG.debug("Impostata CSP (" + headerName + ") con policy: " + policy);
  }
}