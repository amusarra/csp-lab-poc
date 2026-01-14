package github.amusarra.csp.resources.reports;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.logging.Logger;

@Path("/csp-report")
public class CspReportResource {

  private static final Logger LOG = Logger.getLogger(CspReportResource.class);

  @POST
  @Consumes("application/csp-report") // Il browser usa questo Content-Type specifico
  public void handleCspReport(String report) {
    // In una demo, loggare il JSON è perfetto per mostrare cosa succede
    LOG.warn("!!! VIOLAZIONE CSP RILEVATA !!!");
    LOG.warn(report);
  }
}
