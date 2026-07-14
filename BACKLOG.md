# Backlog

Offene Punkte, nach Aufwand und Priorität gruppiert. Die verbindliche Spezifikation bleibt
[pflichtenheft-share-app.md](pflichtenheft-share-app.md); dies hier ist die laufende Arbeitsliste.

## v0.9.0 (nächstes Release)

- **Logs in der App-Sprache:** Log-Texte sind aktuell hart Deutsch (`log.info(...)` in
  Repositories/VM). Nach `strings.xml` auslagern und beim Loggen lokalisieren (Alt-Einträge bleiben
  in ihrer Ursprungssprache). Präferenz: an App-Sprache anpassen, nicht fix Englisch.
- **Dynamische Farben abschaltbar** + feste Akzentfarbe: Material You (Dynamic Color) ist aktuell
  immer an; Schalter unter **Anzeige**, bei „aus" das App-Blau als festes Schema.
- **Direkt-Anbieter-Modelllisten feintunen:** die Live-Filter für Claude/OpenAI/Gemini sind
  „best effort" (nur Mammouths echter Katalog ist bekannt). Gegen die realen `models`-Ausgaben der
  Anbieter prüfen (wie bei Mammouth) — nur für die Dienste relevant, die man direkt nutzt.

Danach: als **0.9.0** taggen.

## Weg zu V1 (nach v0.9.0)

- **Bugfixing & Kleinkram** identifizieren (aus den 0.8.5-/0.9.0-Tests). Aus dem UX-Review 2026-07-13.
  **Paket 1 (Feinschliff) umgesetzt am 2026-07-13, uncommittet** — die folgenden Punkte außer dem
  Karten-Bug sind erledigt (Farb-Tweaks + Statusbar-Fix inklusive):
  - **Senden-Feedback bei leerem aktivem Netzwerk:** „Senden" überspringt ein aktives Netzwerk ohne
    Text stillschweigend. Praktisch nur relevant, wenn KI aus ist und man selbst schreibt (sonst ist
    kein Feld leer) — kleiner Hinweis genügt.
  - **KI aus: Metadaten-Pflicht lockern.** `canGenerate` blockiert „Weiter" bei fehlenden Metadaten
    (NeedsManual) auch, wenn man den Text selbst schreibt. Gate nur bei aktiver KI anwenden.
  - **Mammouth: Anbieterwechsel ohne geladene Modell-Liste löscht das gespeicherte Modell**
    (`withModels` setzt bei leerer Liste ""). Bei leerer Liste die bestehende Auswahl behalten;
    zusätzlich in `saveAi` eine leere Mammouth-Auswahl nie einen gespeicherten Wert überschreiben lassen.
  - **Modell-Liste automatisch laden** (aktuell nur über ⟳): beim Speichern und beim Antippen des
    leeren Modell-Dropdowns, wenn ein Token vorhanden ist.
  - **„Neu schreiben"-Button:** ausgeblendet bei KI aus (ist so); zusätzlich **ausgegraut**, wenn
    KI an, aber kein Token gesetzt (aktuell tippbar, tut still nichts).
  - **Konten: „?"-Icon mit Modal zur Post-Sprache** (erklärt, was die Sprachauswahl bewirkt —
    Sprache der KI-Texte + Sprach-Tag des Posts; Grundeinstellung Systemsprache).
  - **KI-Menü: Speicher-Semantik vereinheitlichen.** Master-Schalter persistiert sofort alles
    (inkl. halb editierter Tokens), Rest erst bei „Speichern" — Schalter soll nur `enabled` togglen.
  - **Onboarding/Erste-Schritte-Zustand** auf der leeren Hauptseite bei Frischinstallation
    (KI aus + kein Konto): kleine Checkliste „1. Konto verbinden, 2. optional KI einrichten".
  - **Bug: Quellsprach-Hinweis auf der Bluesky-Karte oft nicht sichtbar.** Der „(in German)"-Zusatz
    (`withSourceLanguageNote` in `MainViewModel`) erscheint nur, wenn (a) die Artikelsprache erkannt
    wurde — sie kommt aus `<html lang>`/`og:locale` (`OgMetadataFetcher`), was viele Seiten nicht
    setzen → dann kein Hinweis — und (b) er hängt nur als kleines „(in X)" ans ENDE der
    Karten-Beschreibung, die Bluesky auf ~2 Zeilen kürzt → oft abgeschnitten, nie prominent. Fix
    später: Hinweis prominenter (Anfang der Beschreibung, Karten-Titel oder Post-Text) und/oder
    Artikelsprache robuster bestimmen (z. B. Fallback aus der Mastodon-Post-Sprache).
- **Vollständiger Code-Audit** — vor dem Play-Protect-Schritt einmal komplett durch den Code
  (Korrektheit, Robustheit, tote Pfade, Sicherheit), analog zu den Audits v0.6.0.
- **Google Play Protect lösen:** App + Zertifikat bei Google registrieren, damit die Install-Warnung
  „gefährlich / muss gescannt werden" verschwindet (ggf. Play Console App-Signing oder andere
  Verteilstrategie). Der Hinweis ist harmlos, aber abschreckend.
- dann **V1** raus.

## Nach V1

- **Bluesky-Link-Karten-Vorschau, editierbar:** Karten-Titel/-Beschreibung (+ Thumbnail) werden
  aktuell generiert und ungesehen gesendet — als editierbare Mini-Vorschau in der Bluesky-Sektion
  anzeigen. Gutes Add-on, bewusst nach V1 (löst nebenbei die Sichtbarkeit des Quellsprach-Hinweises).
- **README / alle `.md` / GitHub-Repo auf Stand bringen** (KI optional/Standard-aus, Menü „KI",
  mehrere Anbieter, Post-Sprache) + **Screenshots** → `docs/screenshots/`.
- **Prompt-Ton und Hashtag-Auswahl** — ongoing, immer mal wieder nachjustieren (Wortlaut isoliert in
  `PromptBuilder.kt`).

## Nice-to-have (eher unrealistisch als Feature)

- **Translation-Service für Links:** für einen *Link* auf eine übersetzte Fassung bräuchte es einen
  Ganzseiten-Übersetzer (Google Translate; datenschutzfreundliche Alternativen wie LibreTranslate/
  Lingva sind für Ganzseiten unzuverlässig). Erst bei Bedarf neu bewerten.
- **Multi-Account:** mehrere Mastodon-/Bluesky-Konten, Auswahl vor der Generierung.

## Geparkt

- **Threads** — offen, ob *Meta Threads* (OAuth + App-Review, aufwendig) oder Multi-Post-Fäden
  innerhalb Mastodon/Bluesky gemeint sind.
- **Hashtag-Relevanzabgleich** über Mastodon (`GET /api/v2/search?type=hashtags`, `/api/v1/trends/tags`).
  Bluesky bietet keine stabile Entsprechung. Entscheidung: erst über den Prompt verbessern.

## Erledigt

- **v0.8.5** (2026-07-13): **KI-Menü-Umbau** — Dienst als Dropdown + adaptives „?"; Mammouth mit
  Anbieter-Dropdown (GPT/Claude/Mistral/Gemini/Kimi/Qwen) + Live-Modell-Dropdown aus `/v1/models`;
  Direkt-Dienste mit Live-Modellen (Claude-`GET /v1/models` ergänzt); `ModelCatalog`-Filter
  (ohne Bild/Embedding/Code/Reasoning/Preview), Selbstheilung; `ModelResolver` entfernt. **Post-Sprache
  je Netzwerk** — pro Konto BCP-47 (Default Gerätesprache), Dropdown im Konten-Menü, Prompt/Schema
  auf Netzwerk-Keys + Sprache je Ziel umgebaut, Karten-Sprachhinweis, Beschreibung generalisiert.
- **v0.8.0** (2026-07-12): Mehrsprachigkeit DE/EN/DA (per-App-Locale); Menü „Anzeige"↔„KI" getauscht,
  „Mammouth-KI"→„KI"; **KI optional/abschaltbar, Standard aus**; **Anbieterwahl** (Mammouth default,
  Claude/OpenAI/Gemini direkt, „?"-Setup-Infos + `docs/ai-setup.md`); **englische Bluesky-Link-Karte**
  (später zu „Post-Sprache" verallgemeinert); Feinschliff (keine Gedankenstriche, „Neu schreiben").

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
