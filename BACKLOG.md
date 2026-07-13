# Backlog

Offene Punkte, nach Aufwand und Priorität gruppiert. Die verbindliche Spezifikation bleibt
[pflichtenheft-share-app.md](pflichtenheft-share-app.md); dies hier ist die laufende Arbeitsliste.

## Später (Ausbau)

- **Post-Sprache je Netzwerk konfigurierbar:** Statt fest DE=Mastodon / EN=Bluesky beim Verbinden
  je Konto eine Post-Sprache wählen (Dropdown unter der Info-Box im Konten-Menü, später änderbar).
  Sprachen als **BCP-47**-Tags, Anzeigenamen via `Locale`. Betrifft: Konten-Model (Sprache je Konto
  speichern), Settings-UI, `PromptBuilder`/`AiResult`/`GeneratedPosts` (aktuell fest de/en →
  parametrisieren, Schema besser nach Netzwerk statt Sprachcode keyen), Sende-Sprache
  (`MASTODON_LANG`/`BLUESKY_LANGS` → aus Konto), Bluesky-Karten-Sprache + „(in <Sprache>)"-Hinweis,
  sowie die App-Selbstbeschreibung (README DE/EN + `about_description` + Feature-Zeilen, da nicht mehr
  fest Deutsch/Englisch). Größerer Umbau.
- **Protokoll/Logs in der App-Sprache:** Log-Texte sind aktuell hart Deutsch (`log.info(...)` in
  Repositories/VM). Nach `strings.xml` auslagern und beim Loggen lokalisieren (Alt-Einträge bleiben
  in ihrer Ursprungssprache). Nutzer-Präferenz: an App-Sprache anpassen, nicht fix Englisch.
- **Modell-Auswahl für die Direkt-Anbieter (Claude/OpenAI/Gemini):** Statt festem Modell-Textfeld
  ein Dropdown je Anbieter mit den relevanten aktuellen Text-Modellen — **Anzahl pro Anbieter
  unterschiedlich, kein fester Wert** (z. B. Claude: Haiku/Sonnet/Opus; Gemini: eigene Stufen wie
  Flash-Lite/Flash/Pro; OpenAI: kuratierte Auswahl). Jeder Eintrag auf die **neueste Version**
  aufgelöst. Zwei Wege: (1) kuratierte stabile Alias-IDs (einfach, App-Update bei neuer Generation
  nötig) oder (2) live über den models-Endpunkt filtern/gruppieren — Gemini `GET /v1beta/models`
  und OpenAI `GET /v1/models` sind schon verdrahtet, Claude bräuchte noch `GET /v1/models`. „Eigene
  Modell-ID" bleibt als Fallback. Exakte Modell-IDs bei der Umsetzung gegen die Anbieter-Doku
  prüfen (ändern sich laufend). Analog zu Mammouths `ModelResolver`, aber pro Anbieter eigene
  Familien-Definition statt einer gemeinsamen Regel.
- **Englische Bluesky-Link-Karte:** Das KI-Modell liefert im selben Call zusätzlich einen
  englischen Titel + Beschreibung fürs `app.bsky.embed.external` (URL bleibt die deutsche Quelle).
  **Dazu ein Hinweis, dass der verlinkte Artikel auf Deutsch ist** (z. B. „(Artikel auf Deutsch)"
  im Text oder in der Karte). Kein externer Übersetzer nötig — datenschutzmäßig sauber (kein
  weiterer Dritter), siehe unten.
- **Translation-Service (später bewerten):** Für einen *Link* auf eine übersetzte Fassung bräuchte
  es einen Ganzseiten-Übersetzer wie Google Translate. Datenschutzfreundliche Optionen
  (LibreTranslate self-hosted, Lingva/SimplyTranslate) sind für Ganzseiten unzuverlässig
  (Community-Instanzen offline/rate-limited). Erst bei Bedarf neu bewerten.

- **Multi-Language (App-Oberfläche):** UI in Deutsch, Englisch, **Dänisch** — Sprachwahl unter
  **Anzeige** (zusammen mit Dark Mode, kein eigener Menüpunkt). Kern der Arbeit: alle UI-Texte in String-Ressourcen
  auslagern (`values/` = Deutsch + `values-en/` + `values-da/`), da aktuell vieles in Compose
  hartcodiert ist. Dazu `localeConfig` im Manifest (System-Sprachpicker ab Android 13) + In-App-
  Picker via `LocaleManager.applicationLocales` (minSdk 34 nativ). Betrifft NICHT die Posting-
  Sprachen — die erzeugt das KI-Modell.
- **Display → dynamische Farben abschaltbar** + feste Akzentfarbe (sekundäres Farbschema).
  Aktuell ist Material You (Dynamic Color) immer an. *(vorerst zurückgestellt)*

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

- **UX-Release v0.7.0** (2026-07-12): „Generieren" erscheint wieder bei neuer URL (vorher kein Weg
  zum nächsten Artikel ohne Share); fixe Bottom-Bar mit dominantem „Senden"; nach Komplett-Erfolg
  kompakte Erfolgskarten + „Neuer Artikel"; Rückfrage vor „Neu generieren", wenn Texte bearbeitet
  wurden; Metadaten-Karte einklappbar (auf im manuellen Fallback); Pro-Netzwerk-URL als kompakte
  Zeile mit Stift statt drittem Feld; Kopieren als Icon im Sektions-Kopf; Hashtag-Eingabe hinter
  „+"-Chip; farbige Kante je Netzwerk-Sektion; „<Modell> schreibt …"-Feedback;
  Zwischenablage-Chip; Uri-Tastatur; Start-Hinweis; „Post öffnen" statt langer URL;
  Menü mit Icons und durchgängig deutschen Labels (Protokoll, Über). MainScreen in drei Dateien
  gesplittet (MainScreen/PostSection/MainComponents); neue Strings in `strings.xml`.
  *Aufgeschoben bis Multi-Account:* Netzwerk-Paare im ViewModel zu einer Ziel-Liste verallgemeinern.

- **Code-Audit v0.6.0** (2026-07-12): Share-Intent-Replay nach Rotation/Recreate behoben; neuer
  Share ersetzt jetzt den kompletten Zustand; Wikipedia-URLs mit Klammern bleiben intakt;
  Process-Death-Restore zeigt gerettete Texte wieder an (inkl. Karte/Thumbnail); leerer Post über
  nachträglich aktivierten Netzwerk-Chip nicht mehr möglich (Hinweis + Send-Gate);
  og:image-Download auf 15 MB begrenzt (OOM); ein geteilter OkHttp-Client + gecachte
  API-Instanzen; HTTP-Fehler einheitlich als `ApiException`; Idempotency-Key rotiert bei
  Inhaltsänderung; „Keine Metadaten"-Warnung löst sich nach manueller Generierung auf;
  Avatare in den Post-Sektionen; Fallback-Theme im App-Blau; **Test-CI** (Push/PR) eingerichtet.
  Roadmap-Vorarbeit: Posting-Sprache als Parameter, Artikel-Sprache (`<html lang>`/`og:locale`)
  wird mitgelesen, Strings-Konvention in CLAUDE.md. 70 Unit-Tests.

- **Slide-Panel + Unterseiten:** `ModalNavigationDrawer` (Hamburger), Kopf mit verbundenen Accounts
  (tippbar → Accounts), Menü Accounts / Mammouth-KI / Display / Logs / About. Settings-Monolith zerlegt.
  Trennen mit Rückfrage.
- **Dark Mode:** Theme System / Hell / Dunkel, persistiert, in der MainActivity angewandt.
- **About-Screen:** Version, Kurzbeschreibung, Autor, GitHub-Link, Datenschutz-Zeile.
- **App-Icon klein vor dem Titel** (auch im Panel-Kopf und About), mit runden Ecken.
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
