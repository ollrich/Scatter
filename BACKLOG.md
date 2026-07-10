# Backlog

Offene Punkte, nach Aufwand und Priorität gruppiert. Die verbindliche Spezifikation bleibt
[pflichtenheft-share-app.md](pflichtenheft-share-app.md); dies hier ist die laufende Arbeitsliste.

## Als Nächstes (klein, klar umrissen)

- **Post-Aufbau ändern:** `Text` ⏎⏎ `URL` ⏎⏎ `Hashtags`. Aktuell stehen die ergänzenden Hashtags
  *vor* der URL, was im Feed schlecht aussieht. Betrifft `composePost` + Tests; die Facets bleiben
  unverändert, da sie Hashtags per Regex im fertigen Post finden.
- **Post-URL im Erfolgs-Status klickbar machen**, ohne Klammern. Statt `Gepostet ✓ (https://…)`
  künftig `Gepostet ✓` + anklickbarer Link.
- **Neues App-Icon einbauen**, sobald die Datei vorliegt (Pipeline: `design/make_icon.py`).

## Feinschliff, sobald Gerätetests vorliegen

- **Prompt-Ton und Hashtag-Auswahl** nachjustieren — Wortlaut isoliert in `PromptBuilder.kt`.
- **Bluesky-Link-Karte inkl. Vorschaubild prüfen.** War wegen des ExpiredToken-Bugs nie testbar;
  der „Thumbnail-Upload fehlgeschlagen"-Eintrag war ein Folgefehler, keine echte Bildstörung.

## UX-Verbesserungen (vorgeschlagen, noch nicht beauftragt)

- „Neu generieren"-Button (verwirft die aktuellen Texte, neuer KI-Call).
- Titel/Beschreibung **immer** editierbar, nicht nur im Metadaten-Fallback.
- Sichtbar machen, an welche Accounts gesendet wird.
- „In Zwischenablage kopieren" je Netzwerk als Notausgang bei Post-Fehlern.

## Größere Bausteine

- **Einstellungen als Slide-/Burger-Menü** mit Untermenüs; das Protokoll wandert dorthin.
- **„Über"-Screen** mit Kontakt/GitHub-Link (bei Weitergabe an Dritte ggf. Impressumspflicht).
- **Weitere KI-Anbieter direkt** ansprechbar; KI optional abschaltbar. OpenAI ist OpenAI-kompatibel
  (geringer Aufwand), Claude und Gemini brauchen je einen eigenen Adapter.

## Geparkt (auf ausdrücklichen Wunsch)

- **Multi-Account** (mehrere Mastodon-/Bluesky-Konten, Auswahl vor der Generierung, Sprache je Konto).
- **Threads** — offen, ob das Netzwerk *Meta Threads* (OAuth + App-Review, aufwendig) oder
  Multi-Post-Fäden innerhalb Mastodon/Bluesky gemeint sind.
- **Hashtag-Relevanzabgleich** über Mastodon (`GET /api/v2/search?type=hashtags`, `/api/v1/trends/tags`).
  Bluesky bietet keine stabile Entsprechung. Entscheidung: erst über den Prompt verbessern.
- **Repo öffentlich stellen.** Derzeit privat; Obtainium läuft über einen GitHub-Token (siehe README).

## Erledigt / geklärt

- **Play Protect:** „App scan recommended" ist harmlos — Google kennt den APK-Hash nicht. Kein
  Malware-Fund. Abschaltbar über Play Protect → „Improve harmful app detection".
- **`mammouth-recommended` entfernt:** kein aufrufbares Modell, sondern ein OpenRouter-Preset (404).
- **Bluesky-Session-Refresh:** atproto meldet abgelaufene Access-Token als `400/ExpiredToken`,
  nicht als `401` — der Refresh lief deshalb nie an.
- **Dynamische Modellauswahl:** Anbieter → aktuelles Flaggschiff über `/v1/models`.
- **CI-Actions** auf Node24-Majors gehoben.
