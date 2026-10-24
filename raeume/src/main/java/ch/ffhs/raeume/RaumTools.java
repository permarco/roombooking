package ch.ffhs.raeume;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class RaumTools {
    private final BuchungsService service;
    public RaumTools(BuchungsService service) { this.service = service; }

    @McpTool(name = "raeume_frei", description = "Zeigt freie Räume an einem Datum mit ihrer Platzzahl. Liest nur und verändert keine Buchungen.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, openWorldHint = false))
    public String frei(@McpToolParam(description = "Datum als JJJJ-MM-TT", required = true) String datum) {
        try {
            var liste = service.freieRaeume(datum).stream()
                    .map(r -> r.name() + " (" + r.plaetze() + " Plätze)").collect(Collectors.joining(", "));
            return "Frei am " + datum + ": " + (liste.isEmpty() ? "kein Raum" : liste) + ".";
        } catch (IllegalArgumentException ex) { return ex.getMessage(); }
    }

    @McpTool(name = "raum_buchen", description = "Bucht einen freien Raum für einen Anlass verbindlich. Verändert die Buchungen im laufenden Server; nach einem Neustart werden sie zurückgesetzt. Prüft Raum und Belegung.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = false, openWorldHint = false))
    public String buchen(
            @McpToolParam(description = "Raumname: Aare, Rhone oder Ticino", required = true) String raum,
            @McpToolParam(description = "Datum als JJJJ-MM-TT", required = true) String datum,
            @McpToolParam(description = "Anlass der Buchung, 1 bis 120 Zeichen", required = true) String fuer) {
        try { return service.buchen(raum, datum, fuer).nachricht(); }
        catch (IllegalArgumentException ex) { return ex.getMessage(); }
    }
}
