# Backlog

Offene Punkte, nach Aufwand und Priorität gruppiert. Die verbindliche Spezifikation bleibt
[pflichtenheft-share-app.md](pflichtenheft-share-app.md); dies hier ist die laufende Arbeitsliste.

## Als Nächstes: Slide-Panel + Unterseiten (ersetzt das Einzel-Settings-Fenster)

`ModalNavigationDrawer`, geöffnet über ein Hamburger-Icon in der Top-Bar der Hauptseite.

- **Panel-Kopf:** verbundene Accounts (Avatar + Handle + Netzwerk-Farbe), tippbar → Accounts-Unterseite.
- **Menüpunkte** (führen je auf eine eigene Unterseite; heutiges Settings-Monolith wird zerlegt):
  - **Accounts** — verbinden/trennen je Netzwerk. Trennen mit Rückfrage (destruktiv, Token weg).
    Immer derselbe Weg, egal ob 0 oder Accounts verbunden (kein Sonderfall). Kein direktes
    „Zahnrad zum Trennen" — zu fehlklick-anfällig.
  - **Mammouth-KI** — Token-Eingabe + Modellauswahl (dynamische Anbieter-Auflösung, wie gehabt).
  - **Display** — zunächst **Theme: System / Hell / Dunkel** (Drei-Zustand statt Toggle, damit
    „dem System folgen" erhalten bleibt; §2). Persistenz in normalen Prefs (keine Credentials).
    Theme-Wahl muss in der MainActivity gelesen und ans Theme gegeben werden (leicht übergreifend).
    *Später:* Schalter „Dynamische Farben (Material You) aus" + feste Akzentfarbe.
  - **Logs** — wie gehabt, wandert vom Einstellungs-Button hierher.
  - **About** — kurze App-Erklärung, Info zum Autor, GitHub-Link, **App-Version** (Obtainium-relevant),
    Datenschutz-Zeile („keine Telemetrie, Zugangsdaten nur verschlüsselt auf dem Gerät", §8).
    Impressum erst bei Weitergabe an Dritte.

## Feinschliff

- **Prompt-Ton und Hashtag-Auswahl** weiter nachjustieren — Wortlaut isoliert in `PromptBuilder.kt`.
  Stand: mit GPT bereits gut.

## Geparkt (auf ausdrücklichen Wunsch)

- **Multi-Account** (mehrere Mastodon-/Bluesky-Konten, Auswahl vor der Generierung, Sprache je Konto).
- **Threads** — offen, ob das Netzwerk *Meta Threads* (OAuth + App-Review, aufwendig) oder
  Multi-Post-Fäden innerhalb Mastodon/Bluesky gemeint sind.
- **Weitere KI-Anbieter direkt** ansprechbar; KI optional abschaltbar. Nur relevant, wenn die App
  für andere geöffnet wird.
- **Hashtag-Relevanzabgleich** über Mastodon (`GET /api/v2/search?type=hashtags`, `/api/v1/trends/tags`).
  Bluesky bietet keine stabile Entsprechung. Entscheidung: erst über den Prompt verbessern.
- **Repo öffentlich stellen.** Derzeit privat; Obtainium läuft über einen GitHub-Token (siehe README).

## Erledigt

- **Netzwerkauswahl je Post:** Chips (Mastodon/Bluesky) unter der URL, einzeln abwählbar
  (mind. eins bleibt aktiv). Ein abgewähltes Netzwerk wird nicht generiert und nicht gesendet —
  die KI erzeugt dann nur die aktive Sprache. Beim Teilen kein Auto-Start mehr: erst auswählen,
  dann „Generieren" (gilt für Share und manuelles Einfügen gleich).
- **Post-Aufbau:** Text ⏎⏎ URL ⏎⏎ Hashtags.
- **Post-URL im Erfolgs-Status klickbar** (öffnet den Post im Browser).
- **App-Icon** aus `design/icon-source.svg` (Motiv freigestellt, Blau als Hintergrundebene,
  monochrome Variante für Themed Icons). Reproduzierbar via `design/make_icon.py`.
- **„Neu generieren"-Button** — neuer KI-Call ohne erneuten Metadaten-Abruf.
- **Titel/Beschreibung immer editierbar** — speisen KI-Prompt und Bluesky-Link-Karte.
- **Sende-Ziele sichtbar** (Netzwerk + Handle vor dem Absenden).
- **„Post kopieren"** je Netzwerk als Notausgang.
- **Protokoll-Screen** mit den letzten 50 Aktionen, kopierbar; ohne Credentials.
- **Play Protect:** „App scan recommended" ist harmlos — Google kennt den APK-Hash nicht. Kein
  Malware-Fund. Abschaltbar über Play Protect → „Improve harmful app detection".
- **`mammouth-recommended` entfernt:** kein aufrufbares Modell, sondern ein OpenRouter-Preset (404).
- **Bluesky-Session-Refresh:** atproto meldet abgelaufene Access-Token als `400/ExpiredToken`,
  nicht als `401` — der Refresh lief deshalb nie an.
- **Bluesky-Link-Karte** inkl. Vorschaubild funktioniert.
- **Dynamische Modellauswahl:** Anbieter → aktuelles Flaggschiff über `/v1/models`.
- **Medien-Name aus der Domain**, wenn `og:site_name` fehlt.
- **CI-Actions** auf Node24-Majors gehoben.
