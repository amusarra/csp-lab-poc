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

@Path("nonce")
public class NonceResource {
  @Inject
  @Location("nonce-index.html")
  Template nonceIndex;

  @Context
  ContainerRequestContext requestContext;

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance get() {
    // Recuperiamo il nonce che il nostro CspFilter ha messo nella richiesta
    String nonce = (String) requestContext.getProperty("csp-nonce");

    // Passiamo il nonce al template Qute
    return nonceIndex.data("nonce", nonce);
  }
}
