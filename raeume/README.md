# Raumbuchung: Frontend, REST und MCP

Voraussetzung: JDK 27. Start in PowerShell im Projektordner:

```powershell
.\mvnw.cmd spring-boot:run
```

Frontend: http://localhost:8080 (voreingestellt auf den Auftrags-Tag 2026-10-24).
Die Anwendung hört nur auf 127.0.0.1. Alle Zugänge teilen sich denselben BuchungsService.

## REST

```powershell
Invoke-RestMethod 'http://localhost:8080/api/raeume/frei?datum=2026-10-24'
Invoke-RestMethod 'http://localhost:8080/api/buchungen' -Method Post -ContentType 'application/json' -Body '{"raum":"Ticino","datum":"2026-10-24","fuer":"Teammeeting"}'
```

Buchung: HTTP 201 bei Erfolg, 409 bei Belegung, 400 bei ungültigen Eingaben.

## MCP

Transport: Streamable HTTP, Endpunkt: http://localhost:8080/mcp.
Die Anwendung muss laufen, bevor sich ein MCP-Client verbindet.
Die Projektdatei `.mcp.json` konfiguriert Claude Code dafür.
Werkzeuge: `raeume_frei(datum)` und `raum_buchen(raum, datum, fuer)`.
Alle MCP-Parameter und Rückgaben sind Strings. Werkzeugbeschreibungen stehen in `RaumTools`.

## Übung

1. Welche Räume sind am 2026-10-24 frei?
2. Buche mir den grössten freien Raum am 2026-10-24 für das Teammeeting.
3. Buche alle Räume für die ganze Woche vom 2026-10-26 bis 2026-10-30 für mich.

Tatsächliche Client-Aufrufe und Ergebnisse beobachten und protokollieren. Noch keine zusätzliche
Grenze gegen Massenbuchungen: Diese wird nach dem Versuch gemeinsam entschieden.
Unbekannte Räume, ungültige Daten, leere Anlässe und Doppelbuchungen werden bereits abgelehnt.
Die Maps sind nur im Arbeitsspeicher; ein Neustart setzt alles auf die Ausgangslage zurück
(Aare: 6, Rhone: 10, Ticino: 20; Rhone am 2026-10-24 für Projekt Delta belegt).

## Tests

```powershell
.\mvnw.cmd test
```
