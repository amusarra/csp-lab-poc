package github.amusarra.csp.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.UUID;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CspFilter implements ContainerRequestFilter, ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    // Generiamo il nonce già nella fase di request, così è disponibile per il template
    String nonce = UUID.randomUUID().toString().replace("-", "");
    requestContext.setProperty("csp-nonce", nonce);
  }

  @Override
  public void filter(ContainerRequestContext requestContext,
                     ContainerResponseContext responseContext) {
    // Recuperiamo il nonce impostato nella request (fallback se mancante)
    String nonce = (String) requestContext.getProperty("csp-nonce");
    if (nonce == null) {
      nonce = UUID.randomUUID().toString().replace("-", "");
    }

    // Lo passiamo alla richiesta per poterlo usare nell'HTML (opzionale)
    requestContext.setProperty("csp-nonce", nonce);

    // Impostiamo l'header con il nonce
    // Aggiunta di font-src per autorizzare fonts embedded (data:) e risorse self/https
    String policy = "default-src 'self'; " +
                    "script-src 'self' 'nonce-" + nonce + "' 'strict-dynamic' https:; " +
                    "style-src 'self' https:; " +
                    "font-src 'self' data: https:; " +
                    "img-src 'self' data:; " +
                    "connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; upgrade-insecure-requests;";

    responseContext.getHeaders().putSingle("Content-Security-Policy", policy);
    // opzionale: modalità report-only in fase di tuning
    // responseContext.getHeaders().putSingle("Content-Security-Policy-Report-Only", policy + " report-to default");
    // Passa il nonce a template/front-end (es. via header custom o attributi request)
    responseContext.getHeaders().putSingle("X-CSP-Nonce", nonce);
  }
}