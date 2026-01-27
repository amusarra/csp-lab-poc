package io.github.amusarra.csp.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

/**
 * Resource JAX-RS per gestire i submit dei form locali utilizzati nelle demo CSP.
 * Fornisce endpoint per verificare il comportamento della direttiva form-action.
 *
 * @author Antonio Musarra
 * @since 1.0.0
 */
@Path("/")
public class FormSubmitResource {

    private static final Logger LOG = Logger.getLogger(FormSubmitResource.class);

    /**
     * Gestisce il submit del form principale dalla demo form-action.
     * Accetta parametri nome ed email e restituisce una pagina HTML di conferma.
     *
     * @param name il nome inviato dal form
     * @param email l'email inviata dal form
     * @return Response HTTP 200 con pagina HTML di conferma
     */
    @POST
    @Path("/submit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response handleSubmit(
            @FormParam("name") String name,
            @FormParam("email") String email) {

        LOG.infof("Form submit ricevuto - Nome: %s, Email: %s", name, email);

        String html = buildSuccessPage(
            "Form Submit Completato",
            "I dati sono stati ricevuti correttamente dal server locale.",
            "Nome: " + (name != null ? name : "N/A"),
            "Email: " + (email != null ? email : "N/A")
        );

        return Response.ok(html).build();
    }

    /**
     * Gestisce il submit alternativo del form dalla demo form-action.
     * Accetta un parametro generico 'data' e restituisce una pagina HTML di conferma.
     *
     * @param data i dati inviati dal form
     * @return Response HTTP 200 con pagina HTML di conferma
     */
    @POST
    @Path("/local-submit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response handleLocalSubmit(@FormParam("data") String data) {

        LOG.infof("Local submit ricevuto - Data: %s", data);

        String html = buildSuccessPage(
            "Local Submit Completato",
            "I dati sono stati ricevuti correttamente dal server locale.",
            "Dati: " + (data != null ? data : "N/A"),
            null
        );

        return Response.ok(html).build();
    }

    /**
     * Costruisce una pagina HTML di successo per confermare la ricezione dei dati.
     *
     * @param title titolo della pagina
     * @param message messaggio principale
     * @param field1 primo campo dati (opzionale)
     * @param field2 secondo campo dati (opzionale)
     * @return stringa HTML completa
     */
    private String buildSuccessPage(String title, String message, String field1, String field2) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"it\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>").append(title).append(" - CSP Lab</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; max-width: 800px; margin: 2rem auto; padding: 0 1rem; }\n");
        html.append("        .success-box { background: #d4edda; border: 2px solid #28a745; border-radius: 8px; padding: 2rem; margin: 2rem 0; }\n");
        html.append("        h1 { color: #155724; margin-top: 0; }\n");
        html.append("        .message { font-size: 1.1rem; margin: 1rem 0; }\n");
        html.append("        .data { background: #fff; padding: 1rem; border-radius: 4px; margin: 1rem 0; }\n");
        html.append("        .data strong { color: #333; }\n");
        html.append("        a { display: inline-block; margin-top: 1rem; padding: 0.5rem 1rem; background: #007bff; color: white; text-decoration: none; border-radius: 4px; }\n");
        html.append("        a:hover { background: #0056b3; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"success-box\">\n");
        html.append("        <h1>✅ ").append(title).append("</h1>\n");
        html.append("        <p class=\"message\">").append(message).append("</p>\n");
        html.append("        <div class=\"data\">\n");
        html.append("            <p><strong>").append(field1).append("</strong></p>\n");
        if (field2 != null) {
            html.append("            <p><strong>").append(field2).append("</strong></p>\n");
        }
        html.append("        </div>\n");
        html.append("        <p>Questo dimostra che la direttiva <code>form-action 'self'</code> ha permesso il submit al server locale.</p>\n");
        html.append("        <a href=\"/form-action\">← Torna alla Demo</a>\n");
        html.append("        <a href=\"/\" style=\"margin-left: 1rem;\">Home</a>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }
}
