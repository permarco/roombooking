A3 · Ihr erster MCP-Server
Block 3 · allein, Austausch mit dem Nachbarn erlaubt · 25 Minuten · Ihr Rechner mit Python und ClaudeCode
Auftrag
Ein KI-Werkzeug wie Claude Code kennt nur, was es im Training gelesen hat, und kann von sich aus nichtstun. Über MCP (Model Context Protocol) bieten Sie ihm Werkzeuge an: Funktionen eines Dienstes, die esfinden, verstehen und aufrufen kann. Sie schreiben einen kleinen Dienst mit zwei Werkzeugen, binden ihn anClaude Code an und sehen zu, wie das Modell die Werkzeuge wählt. Zum Schluss entscheiden Sie, wo IhrDienst eine Grenze setzen muss.
Das Ergebnis ist ein laufender MCP-Server, ein kurzes Protokoll der Aufrufe und Ihr Entscheid zur Grenze. Dasist das Muster für die Werkstatt A5 (Schritte B1 bis B3), in der dasselbe für SupportFlow entsteht.
Ausgangslage
Ein Raumbuchungsdienst, bewusst ohne SupportFlow. Drei Räume, eine Buchung. Die Daten liegen in zweiPython-Dictionaries. Kein Modell der Welt kennt sie.
Raum
Plätze
Aare
6
Rhone
10
Ticino
20
Gebucht ist Rhone am 24.10.2026 für «Projekt Delta».
Der Dienst bietet zwei Werkzeuge an:
Werkzeug
Parameter
liest / wirkt
raeume_frei
datum
liest: welche Räume an einem Tag frei sind
raum_buchen
raum, datum, fuer
wirkt: trägt eine Buchung ein
Begriffe
MCP-Server:
ein Prozess, den das KI-Werkzeug startet und über die Standardeingabe anspricht. Ersagt, welche Werkzeuge er hat, und führt sie auf Wunsch aus.
Werkzeug:
eine Python-Funktion mit dem Dekorator
@mcp.tool()
. Name, Docstring und Typen derParameter wandern als Beschreibung in den Kontext des Modells.
Beschreibung:
der Docstring. Das Modell liest ihn wie eine Anweisung und entscheidet danach, wannes das Werkzeug wählt. Die Beschreibung ist ein Prompt.
Liest oder wirkt:
Ein lesendes Werkzeug verändert nichts. Ein wirkendes hinterlässt etwas, das bleibt.
FastMCP:
eine Python-Bibliothek, die das Protokoll übernimmt. Sie schreiben nur die Funktionen.
Voraussetzungen
Python 3.10 oder neuer und Claude Code (seit Block 2 eingerichtet). Eine Internetverbindung für das Modell.
Schritte
1. Schreiben
Legen Sie einen Ordner
raeume
an, darin eine virtuelle Umgebung mit der Bibliothek:
mkdir
raeume
&&
cd
raeume
python3
-m
venv .venv
.venv/bin/pip
install fastmcp
Schreiben Sie
server.py
nach dieser Spezifikation. Von Hand oder als Auftrag an Claude Code, beides isterlaubt. Wer die KI schreiben lässt, gibt ihr die Spezifikation und die Bausteine unten mit.
Spezifikation
Was
Wie
Daten
RAEUME
: Raum → Plätze (Aare 6, Rhone 10, Ticino 20).
BUCHUNGEN
:(Raum, Datum) → Anlass, mit der einen Buchung (Rhone, 2026-10-24,Projekt Delta).
raeume_frei(datum)
Liest. Gibt einen Satz zurück: «Frei am 2026-10-24: Aare (6 Plätze), Ticino(20 Plätze)», oder «kein Raum». Die Platzzahl gehört in den Text, sonstkann das Modell nicht vergleichen.
raum_buchen(raum,datum, fuer)
Wirkt. Prüft, ob es den Raum gibt und ob er an dem Tag frei ist, trägt dieBuchung ein und bestätigt: «Gebucht: Ticino am 2026-10-24 fürTeammeeting.» Ist der Raum belegt oder unbekannt, kommt statt einerFehlermeldung ein Satz zurück, etwa «Rhone ist am 2026-10-24 schonbelegt: Projekt Delta.» Das Modell liest ihn und entscheidet, wie esweitergeht.
Beide Werkzeuge
Alle Parameter sind
str
, das Datum als JJJJ-MM-TT. Jede Rückgabe istein Satz. Jeder Docstring sagt dem Modell in Alltagssprache, was dasWerkzeug tut und ob es etwas verändert, zum Beispiel «Die Buchung istverbindlich und bleibt bestehen.»
Die vier Bausteine von FastMCP
from
fastmcp
import
FastMCP
# die Bibliothek übernimmt das Protokoll
mcp
=
FastMCP(
"raeume"
)
# ein Server mit Namen
@mcp.tool
()
# macht aus einer Funktion ein Werkzeug
def
beispiel(datum:
str
)
->
str
:
# Name, Typen und …
"""… der Docstring gehen in den Kontext des Modells."""
return
"ein Satz"
mcp.run()
# am Schluss der Datei: Server starten, wartet auf Nachri
Prüfen, bevor Sie weitergehen:
Lesen Sie beide Docstrings und die Typen. Das ist alles, was das Modellüber Ihre Werkzeuge erfährt. Steht darin, was das Werkzeug tut und ob es wirkt?
2. Anbinden
Legen Sie im Ordner
raeume
die Datei
.mcp.json
an. Sie sagt Claude Code, wie es Ihren Server startet:
{
"mcpServers"
:
{
"raeume"
:
{
"command"
:
".venv/bin/python"
,
"args"
:
[
"server.py"
]
}
}
}
Starten Sie Claude Code im Ordner
raeume
:
claude
Beim ersten Start fragt Claude Code, ob es dem MCP-Server aus diesem Projekt vertrauen soll. BestätigenSie. Mit
/mcp
sehen Sie den Server
raeume
und seine zwei Werkzeuge. Steht dort ein Fehler, prüfen Sie denPfad zur virtuellen Umgebung und ob
server.py
ohne Fehlermeldung startet (
.venv/bin/pythonserver.py
, der Server wartet dann auf Eingabe, Abbruch mit Ctrl-C). Stimmt in
/mcp
der Name einesWerkzeugs nicht, hat die KI anders benannt als die Spezifikation: nachbessern, damit das Protokoll nachhervergleichbar ist.
3. Benutzen
Stellen Sie nacheinander zwei Fragen und beobachten Sie, was Claude Code anzeigt: welches Werkzeug esaufruft, mit welchen Parametern, was zurückkommt und was es daraus macht. Vor jedem Aufruf fragt ClaudeCode um Erlaubnis.
1.
«Welche Räume sind am 2026-10-24 frei?»
2.
«Buche mir den grössten freien Raum am 2026-10-24 für das Teammeeting.»
Notieren Sie für die zweite Frage die Aufrufe in ihrer Reihenfolge. Woher wusste das Modell, welcher Raumder grösste ist?
4. Die Grenze
Eine dritte Anweisung:
3.
«Buche alle Räume für die ganze Woche vom 2026-10-26 bis 2026-10-30 für mich.»
Beobachten Sie, was geschieht, und wie oft Claude Code um Erlaubnis fragt. Dann entscheiden Sie: Wasmuss sich ändern, damit ein Agent nicht alle Räume einer Woche blockieren kann? Drei Orte stehen zur Wahl:
die Beschreibung
von
raum_buchen
(ein Satz wie «bucht höchstens einen Raum»),
die Werkzeugliste
(Buchen gar nicht anbieten, nur Lesen),
eine Prüfung im Code
von
raum_buchen
(zum Beispiel: je Anlass und Tag ein Raum; oder: dieBuchung ist nur ein Vorschlag, den ein Mensch bestätigt).
Wählen Sie einen Ort, begründen Sie ihn in zwei Sätzen, und bauen Sie ihn ein, wenn die Zeit reicht.Probieren Sie dann die dritte Anweisung nochmals.
Ergebnis
Protokoll zu Frage 2
Schritt
Werkzeug
Parameter
Ergebnis
1
2
Anweisung 3:
Was geschah, und was hat den Agenten aufgehalten?
Entscheid zur Grenze:
Ort und Begründung in zwei Sätzen.
Formatbeispiel, aus einem anderen Thema
Eine Kaffeekasse als MCP-Server:
kontostand(name)
liest,
einzahlen(name, betrag)
wirkt.
Schritt
Werkzeug
Parameter
Ergebnis
1
kontostand
name=«Lea»
«Lea: 12.50 CHF»
2
einzahlen