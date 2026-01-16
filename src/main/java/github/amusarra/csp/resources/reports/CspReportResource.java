package github.amusarra.csp.resources.reports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.logging.Logger;

@Path("/csp-report")
public class CspReportResource {

  private static final Logger LOG = Logger.getLogger(CspReportResource.class);

  private final ObjectMapper mapper;

  @Inject
  public CspReportResource(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @POST
  @Consumes("application/csp-report") // Il browser usa questo Content-Type specifico
  public void handleCspReport(String report) {
    // In una demo, loggare il JSON è perfetto per mostrare cosa succede
    LOG.warn("!!! VIOLAZIONE CSP RILEVATA !!!");

    if (report == null || report.trim().isEmpty()) {
      LOG.warn("<empty report>");
      return;
    }

    try {
      ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
      Object json = mapper.readTree(report);
      String pretty = writer.writeValueAsString(json);
      LOG.warn(pretty);
    } catch (JsonProcessingException e) {
      LOG.warn("Impossibile fare pretty-print del report CSP; loggo il raw payload.");
      LOG.warn(report);
    }
  }
}
