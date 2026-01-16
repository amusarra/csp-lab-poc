package github.amusarra.csp.resources.reports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.logging.Logger;

/**
 * Resource che espone l'endpoint /csp-report per ricevere i report inviati
 * dai browser quando una direttiva CSP viene violata.
 *
 * Il payload ricevuto ha tipicamente Content-Type "application/csp-report".
 * In questa demo il report viene reso leggibile tramite logging.
 */
@Path("/csp-report")
public class CspReportResource {

  /**
   * Logger per i report CSP.
   */
  private static final Logger LOG = Logger.getLogger(CspReportResource.class);

  private final ObjectMapper mapper;

  /**
   * Costruttore con injection di ObjectMapper per il pretty-print del JSON.
   *
   * @param mapper mapper Jackson usato per leggere e formattare il report
   */
  @Inject
  public CspReportResource(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * Endpoint che riceve il report CSP (application/csp-report) come payload raw.
   * In caso di payload valido viene fatto il pretty-print e loggato; in caso di
   * errori si logga il payload raw.
   *
   * @param report payload JSON del report CSP (stringa)
   */
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
