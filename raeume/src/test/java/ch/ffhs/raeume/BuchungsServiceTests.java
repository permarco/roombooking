package ch.ffhs.raeume;

import org.junit.jupiter.api.Test;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class BuchungsServiceTests {
    @Test void anfangsbelegungUndGemeinsameWerkzeuge() {
        var service = new BuchungsService();
        var tools = new RaumTools(service);
        assertThat(tools.frei("2026-10-24")).isEqualTo("Frei am 2026-10-24: Aare (6 Plätze), Ticino (20 Plätze).");
        assertThat(service.buchen("Ticino", "2026-10-24", "Teammeeting").status()).isEqualTo(201);
        assertThat(tools.buchen("Ticino", "2026-10-24", "Andere Sitzung")).contains("schon belegt: Teammeeting");
        service.buchen("Aare", "2026-10-24", "Workshop");
        assertThat(tools.frei("2026-10-24")).contains("kein Raum");
    }

    @Test void ungueltigeEingabenAendernKeineBuchungen() {
        var service = new BuchungsService();
        assertThatThrownBy(() -> service.freieRaeume("2026-02-30")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.freieRaeume(null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(service.buchen("Unbekannt", "2026-10-24", "Test").status()).isEqualTo(400);
        assertThat(service.buchen("Aare", "2026-10-24", "   ").status()).isEqualTo(400);
        assertThat(service.freieRaeume("2026-10-24")).hasSize(2);
    }

    @Test void paralleleBuchungenHabenGenauEinenGewinner() throws Exception {
        var service = new BuchungsService();
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<BuchungsService.Ergebnis> buchen = () -> service.buchen("Aare", "2026-10-26", "Meeting");
            var ergebnisse = executor.invokeAll(List.of(buchen, buchen));
            assertThat(List.of(ergebnisse.get(0).get().status(), ergebnisse.get(1).get().status()))
                    .containsExactlyInAnyOrder(201, 409);
        }
    }
}
