# Backlog

Offene Punkte, nach Aufwand und Priorität gruppiert. Die verbindliche Spezifikation bleibt
[pflichtenheft-share-app.md](pflichtenheft-share-app.md); dies hier ist die laufende Arbeitsliste.

## v0.9.0 (nächstes Release)

- **Logs in der App-Sprache:** Log-Texte sind aktuell hart Deutsch (`log.info(...)` in
  Repositories/VM). Nach `strings.xml` auslagern und beim Loggen lokalisieren (Alt-Einträge bleiben
  in ihrer Ursprungssprache). Präferenz: an App-Sprache anpassen, nicht fix Englisch.
- **Dynamische Farben abschaltbar** + feste Akzentfarbe: Material You (Dynamic Color) ist aktuell
  immer an; Schalter unter **Anzeige**, bei „aus" das App-Blau als festes Schema.
- ~~Direkt-Anbieter-Modelllisten feintunen~~ → Spec steht, Umsetzung nach v0.9.5 verschoben (s. u.).
- **Umbenennung ScatterTo → Scatter** (Nutzer-Wunsch 2026-07-14): nur der **Anzeigename**
  (`app_name` in 3 Sprachen, Top-Bar-Text, About, README), `applicationId app.scatterto` bleibt
  (unveränderlich, Update-/Cert-Identität). **GitHub-Repo wird mit umbenannt** (`ollrich/Scatter`;
  GitHub leitet Alt-URLs um, zusätzlich die hartcodierten URLs anpassen: About-`GITHUB_URL`,
  `docs/ai-setup.md`-Link, Obtainium/README).

Danach: als **0.9.0** taggen.

## v0.9.5 (vor Code-Audit & Verifizierung)

- **Tonality-Menü in den KI-Einstellungen** (Nutzer 2026-07-14): 4 **globale** Tonalitäten,
  Standard = aktueller sachlich-neutraler Stil, plus „freudig/positiv" und zwei archetyp-basierte
  (Slot 3/4 — Nutzer liefert die konkreten Archetypen separat; Hashtag-/Emoji-Verhalten wird mit
  den Archetypen festgelegt). Technisch: `tonality`-Feld in `AiSettings` + Dropdown im KI-Menü +
  Tonalitäts-Block in `PromptBuilder.system`.
- **Editierbare Link-Karten-Vorschau — NUR Bluesky:** Karten-Titel/-Beschreibung (+ Thumbnail)
  werden aktuell generiert und ungesehen gesendet — als editierbare Mini-Vorschau in der
  Bluesky-Sektion anzeigen. Löst zugleich den Quellsprach-Hinweis-Bug (Karte sichtbar/editierbar).
  **UX-Entscheidung (2026-07-14): eingeklappt als Standard, aber sichtbar** (progressive disclosure) —
  kompakte, erkennbare Zeile mit Thumbnail + gekürztem Kartentitel + Chevron; Antippen klappt die
  Editierfelder (Titel/Beschreibung/Thumbnail + Sprachhinweis) auf. **Muster der vorhandenen
  einklappbaren Metadaten-Karte** wiederverwenden (bekanntes Bedienkonzept).
  **Mastodon geht nicht:** dort baut der Server die Link-Karte selbst aus den OG-Tags der Quellseite
  (kein `card`-Feld in der Status-API, nicht mitsendbar/editierbar) — höchstens read-only, aber
  redundant zur bestehenden Metadaten-Karte. Also Vorschau Bluesky-only.
- ~~Dynamische Farben — Variante 1~~ → **erledigt in v0.9.2**.
- ~~About-Text-Fix „Scatter"~~ → **erledigt in v0.9.1**.
- **Direkt-Anbieter-Modelllisten — Filter-Spec steht** (echte Kataloge geprüft 2026-07-15, alle
  Entscheidungen getroffen; nur noch umzusetzen):
  - **Prinzip: Ausschlussliste** (Nutzer-Entscheidung) — neue Modelle/Stufen erscheinen dadurch
    automatisch, ohne App-Update. Ein App-Update braucht es nur, wenn eine neue *Beiwerk*-Kategorie
    auftaucht, die wir verstecken wollen.
  - **Raus:** Bild/Audio/Realtime/TTS/Transcribe/Whisper/Moderation/Embedding/Codex/Coder/**Search**/
    Sora/**Instruct**/**Chat**/Pro/Mini/Nano/o-Serie/Legacy (gpt-3.5, babbage, davinci)/**Preview**/
    customtools/Robotics/Computer-Use/Deep-Research/Live/Native-Audio.
  - **Datums-Snapshots einklappen:** `X-YYYY-MM-DD` bzw. `X-NNNNNNNN` fliegt raus, wenn der Alias `X`
    existiert; sonst bleibt er (sonst verlöre man z. B. Claude Haiku, das es nur datiert gibt).
  - **Je Stufe/Familie die letzten zwei.** **`-latest`-Aliase gelten als neueste** (ranken vorn) —
    sie haben keine Versionsnummer und würden sonst hinten rausfallen.
  - **Soll-Ergebnis:** Claude 6 (`sonnet-5`, `sonnet-4-6`, `fable-5`, `opus-4-8`, `opus-4-7`,
    `haiku-4-5-20251001`) · OpenAI 4 (`gpt-5.6-sol/terra/luna`, `gpt-5.5`) · Gemini 6 (`pro-latest`,
    `2.5-pro`, `flash-latest`, `3.5-flash`, `flash-lite-latest`, `3.1-flash-lite`).
  - **Gefundene Filter-Lücken (real):** `search` und `instruct` fehlten, gpt-3.5-Legacy kam durch,
    Datums-Snapshots doppelten die Aliase.
  - **Kontext:** OpenAI-Stufen sind seit 5.6 **Sol/Terra/Luna** (Flaggschiff/ausgewogen/günstig,
    analog Opus/Sonnet/Haiku); Geminis Stufen sind Pro/Flash/Flash-Lite (Tier-Erkennung muss
    `flash-lite` vor `flash` matchen). Neueste Gemini-**Pro** sind preview-only → stabiles Pro bleibt
    2.5; für kurze Posts ist Flash ohnehin ausreichend.

Danach: als **0.9.5** taggen.

## Weg zu V1 (nach v0.9.5)

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
    Karten-Beschreibung, die Bluesky auf ~2 Zeilen kürzt → oft abgeschnitten, nie prominent. Wird
    mit der **editierbaren Link-Karten-Vorschau in v0.9.5** gelöst (Karte sichtbar/editierbar); dazu
    Artikelsprache robuster bestimmen (Fallback aus der Mastodon-Post-Sprache).
- **Vollständiger Code-Audit** — vor dem Play-Protect-Schritt einmal komplett durch den Code
  (Korrektheit, Robustheit, tote Pfade, Sicherheit), analog zu den Audits v0.6.0.
- **Google Play Protect / Sideload-Warnung angehen** (Stand recherchiert 2026-07-14): Der Hinweis ist
  reputationsbasiert (App unbekannt, frischer Signing-Key, kleine Installbasis) — **kein Malware-Fund**.
  ScatterTo nutzt KEINE der hart geblockten Sensibel-Permissions (SMS/Notification-Listener/
  Accessibility), wird also nicht install-geblockt, nur „unverifiziert" gewarnt. Google verlangt
  zunehmend **Entwickler-Identitätsverifizierung auch für Sideload** (Rollout ab Sept. 2026 in
  einzelnen Regionen, global bis 2027 — betrifft dann auch alt. Stores/F-Droid).
  **Entscheidung (Nutzer 2026-07-14): Google-Entwicklerverifizierung** — Identitätsverifizierung/
  App-Registrierung ohne Play-Veröffentlichung. (Alternativen wären F-Droid, Reputation-abwarten
  oder Warnung-akzeptieren; vollständig Google-frei wird durch die 2026/2027-Regeln ohnehin schwerer.)
- dann **V1** raus.

## Nach V1

- **README / alle `.md` / GitHub-Repo auf Stand bringen** (KI optional/Standard-aus, Menü „KI",
  mehrere Anbieter, Post-Sprache) + **Screenshots** → `docs/screenshots/`.
- **Prompt-Ton und Hashtag-Auswahl** — ongoing, immer mal wieder nachjustieren (Wortlaut isoliert in
  `PromptBuilder.kt`).

## Ideen-Pool (nach V1 sortieren)

Inspiration 2026-07-14, noch nicht priorisiert — wird nach V1 einsortiert.

Direkter Kernnutzen (Favoriten):
- **„Kürzen"-Aktion bei Überlänge:** geht der Post übers Limit, ein Knopf, der die KI den Text
  straffen lässt (statt manuell zu kürzen).
- **Hashtag-Verlauf / Favoriten:** zuletzt/oft genutzte Hashtags lokal merken und per Tipp anhängen.
- **Mastodon: Sichtbarkeit + Content Warning:** optionale Felder `visibility`
  (öffentlich/nicht gelistet/nur Follower) und `spoiler_text` — native Mastodon-Funktionen.

Weitere:
- **Geplantes Posten:** Mastodon nativ (`scheduled_at`); Bluesky müsste lokal geplant werden (mehr
  Aufwand). Für ein Posting-Tool stark, aber größer.
- **Alt-Text fürs Karten-Bild:** Barrierefreiheit, beide Netze unterstützen es.
- **Teilen auch von reinem Text/Bild** (nicht nur URL) — erweitert den Input.
- **Kompakte „so wird's aussehen"-Post-Vorschau:** der zusammengesetzte Post (Text ⏎ URL ⏎ Hashtags)
  als Ganzes statt nur Einzelteile + Zähler (stand schon im UX-Review).
- **A11y-Durchgang** vor „für andere öffnen": TalkBack, große Schrift, Kontraste.

Niedrige Priorität / bewusst skeptisch (Fokus-Verwässerung):
- Weitere Netzwerke (Nostr/Lemmy/Pixelfed …), Home-Screen-Widget, Statistik-Dashboard.

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
