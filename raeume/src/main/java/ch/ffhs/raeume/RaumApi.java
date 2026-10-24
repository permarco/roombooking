package ch.ffhs.raeume;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RaumApi {
    private final BuchungsService service;
    public RaumApi(BuchungsService service) { this.service = service; }

    @GetMapping("/raeume/frei")
    public List<BuchungsService.Raum> frei(@RequestParam String datum) {
        return service.freieRaeume(datum);
    }

    @PostMapping("/buchungen")
    public ResponseEntity<BuchungsService.Ergebnis> buchen(@RequestBody Buchung anfrage) {
        var ergebnis = service.buchen(anfrage.raum(), anfrage.datum(), anfrage.fuer());
        return ResponseEntity.status(ergebnis.status()).body(ergebnis);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> ungueltig(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("nachricht", ex.getMessage()));
    }

    public record Buchung(String raum, String datum, String fuer) {}
}
