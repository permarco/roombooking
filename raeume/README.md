# Raumbuchung: Frontend, REST und MCP

Voraussetzung: JDK 27. Start in PowerShell im Projektordner:

```powershell
.\dev.ps1
```

Frontend: http://127.0.0.1:8095 (voreingestellt auf den Auftrags-Tag 2026-10-24).
Die Anwendung hört nur auf 127.0.0.1. Alle Zugänge teilen sich denselben BuchungsService.

## REST

```powershell
Invoke-RestMethod 'http://127.0.0.1:8095/api/raeume/frei?datum=2026-10-24'
Invoke-RestMethod 'http://127.0.0.1:8095/api/buchungen' -Method Post -ContentType 'application/json' -Body '{"raum":"Ticino","datum":"2026-10-24","fuer":"Teammeeting"}'
```

Buchung: HTTP 201 bei Erfolg, 409 bei Belegung, 400 bei ungültigen Eingaben.

## MCP

Transport: Streamable HTTP, Endpunkt: http://127.0.0.1:8095/raum-mcp.
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
.\dev.ps1 test
```

Das Windows-Skript verwendet den Windows-Zertifikatsspeicher und einen kurzen
temporären Socket-Pfad. Beide Einstellungen gelten nur für diesen Aufruf.
Port 8095 wurde gewählt, weil 8080 und 8081 auf diesem Rechner belegt waren.
Der alternative MCP-Pfad vermeidet einen in dieser Umgebung beobachteten
HTTP-Framing-Fehler unter `/mcp`; die Ursache ausserhalb der Buchungslogik ist nicht abschliessend geklärt.
Spring AI wurde auf Patch-Version 2.0.2 aktualisiert.
