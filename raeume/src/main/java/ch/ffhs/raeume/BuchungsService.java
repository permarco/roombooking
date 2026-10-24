package ch.ffhs.raeume;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class BuchungsService {
    private final Map<String, Integer> raeume = new LinkedHashMap<>();
    private final Map<Schluessel, String> buchungen = new HashMap<>();

    public BuchungsService() {
        raeume.put("Aare", 6);
        raeume.put("Rhone", 10);
        raeume.put("Ticino", 20);
        buchungen.put(new Schluessel("Rhone", LocalDate.of(2026, 10, 24)), "Projekt Delta");
    }

    public LocalDate datumPruefen(String datum) {
        try {
            if (datum == null || !datum.matches("\\d{4}-\\d{2}-\\d{2}")) {
                throw new IllegalArgumentException();
            }
            return LocalDate.parse(datum);
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            throw new IllegalArgumentException("Bitte ein gültiges Datum als JJJJ-MM-TT angeben.");
        }
    }

    public synchronized List<Raum> raeumeAm(String datum) {
        LocalDate tag = datumPruefen(datum);
        return raeume.entrySet().stream().map(e -> new Raum(e.getKey(), e.getValue(),
                buchungen.get(new Schluessel(e.getKey(), tag)))).toList();
    }

    public List<Raum> freieRaeume(String datum) {
        return raeumeAm(datum).stream().filter(Raum::frei).toList();
    }

    // Prüfung und Eintrag erfolgen atomar, auch bei parallelen REST- und MCP-Aufrufen.
    public synchronized Ergebnis buchen(String raum, String datum, String fuer) {
        LocalDate tag = datumPruefen(datum);
        if (raum == null || !raeume.containsKey(raum)) {
            return new Ergebnis(false, 400, "Unbekannter Raum: " + raum + ".");
        }
        if (fuer == null || fuer.isBlank() || fuer.strip().length() > 120) {
            return new Ergebnis(false, 400, "Bitte einen Anlass mit 1 bis 120 Zeichen angeben.");
        }
        var key = new Schluessel(raum, tag);
        if (buchungen.containsKey(key)) {
            return new Ergebnis(false, 409, raum + " ist am " + datum + " schon belegt: " + buchungen.get(key) + ".");
        }
        buchungen.put(key, fuer.strip());
        return new Ergebnis(true, 201, "Gebucht: " + raum + " am " + datum + " für " + fuer.strip() + ".");
    }

    private record Schluessel(String raum, LocalDate datum) {}
    public record Raum(String name, int plaetze, String anlass) {
        public boolean frei() { return anlass == null; }
    }
    public record Ergebnis(boolean erfolgreich, int status, String nachricht) {}
}
