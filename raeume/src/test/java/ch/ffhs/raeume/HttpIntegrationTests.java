package ch.ffhs.raeume;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.net.URI;
import java.net.http.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpIntegrationTests {
    @LocalServerPort int port;
    private final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test void frontendUndRestTeilenBuchungen() throws Exception {
        assertThat(get("/").body()).contains("Projekt Delta", "Raum verbindlich buchen");
        assertThat(get("/style.css").statusCode()).isEqualTo(200);
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/buchungen"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {"raum":"Aare","datum":"2026-11-01","fuer":"Integrationstest"}
                        """)).build();
        assertThat(client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(201);
        assertThat(client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(409);
        assertThat(get("/?datum=2026-11-01").body()).contains("Integrationstest");
        assertThat(get("/api/raeume/frei?datum=2026-11-01").body()).doesNotContain("Aare").contains("Ticino");
        assertThat(get("/api/raeume/frei?datum=2026-02-30").statusCode()).isEqualTo(400);
        var formular = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/buchen"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("raum=Ticino&datum=2026-11-02&fuer=Webformular")).build();
        assertThat(client.send(formular, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(302);
        assertThat(get("/?datum=2026-11-02").body()).contains("Webformular");
    }

    private record McpAntwort(int statusCode, HttpHeaders headers, String body) {}

    private McpAntwort mcp(String body, String session) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/raum-mcp"))
                .timeout(java.time.Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream");
        if (session != null) request.header("Mcp-Session-Id", session).header("MCP-Protocol-Version", "2025-03-26");
        var result = client.send(request.POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofInputStream());
        try (var stream = result.body()) {
            String text = "";
            if (result.statusCode() != 202 && result.statusCode() != 204) {
                if (result.headers().firstValue("Content-Type").orElse("").startsWith("text/event-stream")) {
                    var reader = new java.io.BufferedReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data:")) { text = line.substring(5); break; }
                    }
                } else {
                    text = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
            }
            return new McpAntwort(result.statusCode(), result.headers(), text);
        }
    }

    @Test void mcpRegistriertUndRuftWerkzeugeAuf() throws Exception {
        var init = mcp("""
                {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"integration-test","version":"1"}}}
                """, null);
        assertThat(init.statusCode()).isEqualTo(200);
        String session = init.headers().firstValue("Mcp-Session-Id").orElseThrow();
        mcp("""
                {"jsonrpc":"2.0","method":"notifications/initialized"}
                """, session);
        var liste = mcp("""
                {"jsonrpc":"2.0","id":2,"method":"tools/list"}
                """, session);
        assertThat(liste.body()).contains("raeume_frei", "raum_buchen");
        var result = mcp("""
                {"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"raeume_frei","arguments":{"datum":"2026-10-24"}}}
                """, session);
        assertThat(result.body()).contains("Aare (6 Plätze)", "Ticino (20 Plätze)").doesNotContain("Rhone (10 Plätze)");
        var buchung = mcp("""
                {"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"raum_buchen","arguments":{"raum":"Ticino","datum":"2026-11-03","fuer":"MCP-Test"}}}
                """, session);
        assertThat(buchung.body()).contains("Gebucht: Ticino am 2026-11-03 für MCP-Test.");
        assertThat(get("/api/raeume/frei?datum=2026-11-03").body()).doesNotContain("Ticino");
    }
}
