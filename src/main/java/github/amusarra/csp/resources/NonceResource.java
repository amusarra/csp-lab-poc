package github.amusarra.csp.resources;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@Path("nonce")
public class NonceResource {
  private static final Logger LOGGER = Logger.getLogger(NonceResource.class);

  private final Template nonceIndex;

  @Context
  ContainerRequestContext requestContext;

  @Inject
  public NonceResource(@Location("nonce-index.html") Template nonceIndex) {
    this.nonceIndex = nonceIndex;
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance get() {
    // Recuperiamo il nonce che il nostro CspFilter (da implementare) dovrebbe mettere nella richiesta
    String nonce = null;
    if (requestContext != null) {
      Object val = requestContext.getProperty("csp-nonce");
      if (val instanceof String s) {
        nonce = s;
      }
    } else {
      LOGGER.warn("ContainerRequestContext non disponibile nel resource; impossibile leggere il nonce dalla request.");
    }

    if (nonce == null) {
      // fallback: nonce vuoto per evitare di rompere il template durante lo sviluppo
      nonce = "";
      LOGGER.warn("csp-nonce non trovato nella request; assicurati che CspFilter imposti la proprietà prima del rendering.");
    }

    // Passiamo il nonce al template Qute (anche se vuoto)
    return nonceIndex.data("nonce", nonce);
  }
}
