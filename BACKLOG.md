# Backlog

Offene Punkte, nach Aufwand und Priorität gruppiert. Die verbindliche Spezifikation bleibt
[pflichtenheft-share-app.md](pflichtenheft-share-app.md); dies hier ist die laufende Arbeitsliste.

## Erledigt

- **v0.9.0** — Umbenennung ScatterTo → Scatter (Anzeigename + GitHub-Repo; `applicationId
  app.scatterto` bleibt), Logs in der App-Sprache, Dynamic-Color-Schalter, Onboarding-Checkliste,
  UX-Review-Paket 1.
- **v0.9.1** — Direkt-Anbieter-Modelllisten: Filter-Umbau nach echten Katalogen (Ausschlussliste,
  Datums-Snapshots einklappen, je Stufe die letzten zwei, `-latest` vorn) + About-Text-Fix.
- **v0.9.2 / v0.9.3** — Dynamische Farben Variante 1; Dunkel-Modus nutzt den exakten Marken-Hex
  (~3,9:1, bewusster Kontrast-Kompromiss — echtes AA gibt es über den aktivierten Modus).
- **v0.9.5** — Tonalitäts-Menü (4 globale Tonalitäten, Radio-Liste + ein „?" mit Vergleichs-
  Beispielen), editierbare Bluesky-Link-Karten-Vorschau (eingeklappt als Standard), URL raus aus
  dem Bluesky-Text (Budget ~170–190 → 250 Zeichen), Share-Intent-Bug.
  - Der Quellsprach-Hinweis steht jetzt **vorn** in der Karten-Beschreibung (hinten kürzt Bluesky
    ihn weg) und ist in der Vorschau korrigierbar. Bleibt: fehlt `<html lang>`/`og:locale`, gibt es
    keinen automatischen Hinweis — dann hilft nur die Hand-Korrektur.

- **v0.9.6** — Audit-Fixes (Code-Audit 2026-07-17, komplett): Regenerieren überschreibt nur noch
  angeforderte, nicht erfolgreich gepostete Netzwerke (Teilerfolg bleibt erhalten); Datums- und
  KI-Fehlertexte lokalisiert; „HTTP 400 (…)" statt Gedankenstrich; Karten-Schalter „Karte
  mitsenden" (aus = URL zurück in den Bluesky-Text); Metadaten-Retry bei Fehlschlag;
  Ziffern-Hashtags (#2026wahl) als Facets; tote Symbole/Strings/Doppel-Import entfernt;
  `dataExtractionRules` (kein Cloud-Backup/D2D), Auth-Header im Debug-Log geschwärzt,
  CredentialStore crash-fest; Fastlane-Texte (de/en/da); README-Privacy/Roadmap berichtigt.
  **Dependency-Refresh komplett:** AGP 9.3 (Kotlin 2.4 eingebaut), Gradle 9.6.1, `compileSdk` 37,
  Compose BOM 2026.06, OkHttp 5, Retrofit 3 (offizieller kotlinx-Converter), Coil 3,
  security-crypto 1.1.0 stabil, neue Clipboard-API. Lint 23 → 4 Findings, 0 Errors.

- **v0.9.7** — Tonalität „Hape" heißt jetzt „Hans-Peter" (Enum-Key bleibt `hape`, sonst würde
  jede gespeicherte Auswahl zurückgesetzt). **R8/Shrinking an** (weicht bewusst von §12.3 Nr. 3 ab):
  APK 49 MiB → 3,5 MiB, weil `material-icons-extended` seine ~11.400 Vektoren als Bytecode
  mitbringt und R8 exakt die 20 genutzten übrig lässt. Nötig war eine einzige Regel
  (`-dontwarn com.google.errorprone.**` für Tink); die CI sichert jetzt `mapping.txt` als
  Workflow-Artifact (90 Tage), sonst wären Stacktraces aus Release-Builds unlesbar.
  Dynamische Farben standardmäßig **aus** (frisch installiert = Markenfarben). „Karte mitsenden"
  hat ein „?" mit Modal. Karten-Prompt: näher an den Meta-Tags, max. ~100 Zeichen statt 150,
  Meta-Einleitungen („Der Artikel beschreibt …") explizit verboten. About-Datenschutztext benennt
  die vier KI-Anbieter konkret. **Mammouth-Guthaben** im KI-Menü: Mammouth fährt LiteLLM und legt
  dessen Endpoints offen, deshalb `spend`/`max_budget` abrufbar (zwei Calls: `/key/info` liefert die
  `user_id`, `/user/info` das Account-Budget; beide NICHT unter `/v1`). Best-effort, das Schema ist
  undokumentiert und die Doku zeigt fälschlich `0.0.0.0:4000`. **Nur Mammouth:** OpenAI hat keinen
  Endpoint, Anthropic nur mit Admin-Key (zu viele Rechte für eine Client-App), Google nur über die
  Cloud Billing API mit OAuth.

## Weg zu V1

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

- **F-Droid-Veröffentlichung** (Nutzer-Plan 2026-07-16, zusätzlich zur Google-Verifizierung, nicht
  statt): Passt gut — MIT, reine FOSS-Deps, keine Play Services, und die `signingConfig` scheitert
  ohne `SIGNING_*`-Env-Vars bewusst nicht (F-Droid baut aus dem Quellcode).
  - **Anti-Feature „NonFreeNet" erwarten** (proprietäre KI-APIs) — kein Blocker, aber ein Label.
  - **Wichtigste Entscheidung — Signatur:** F-Droid signiert standardmäßig mit **eigenem Key** →
    andere Signatur als die GitHub/Obtainium-APK → Nutzer müssten zum Wechseln deinstallieren.
    **Ziel daher: Reproducible Builds** (F-Droid baut nach, vergleicht byte-genau, veröffentlicht
    die **eigene signierte** APK) → eine Signatur für alle, Fingerprint `36b474d5…` bleibt stabil.
  - Vorarbeit: Merge-Request ins `fdroiddata`-Repo (Build-Rezept) und Screenshots (Doppelnutzen
    mit der README). Beschreibungstexte liegen seit v0.9.6 unter `fastlane/metadata/android/`
    (de/en/da). Tag-Schema `v*` passt bereits.
  - **Ersetzt die Google-Entwicklerverifizierung nicht** — ab 2027 fallen alt. Stores auch darunter.
- **README / GitHub-Repo auf Stand bringen** — der V1-Schritt nach der neuen Tonalität, bewusst als
  ein Paket (Nutzer 2026-07-17: „bedarf besonderer Aufmerksamkeit"):
  - **Funktionsumfang** aktualisieren: KI optional und ab Werk aus, vier Anbieter, Menü „KI",
    Post-Sprache je Netzwerk, Tonalitäten, editierbare Bluesky-Karte, Mammouth-Guthaben.
  - **Mammouth als reichhaltigste Option positionieren** (Nutzer 2026-07-17, „nicht in diesen
    Worten"): mit Mammouth ist der Funktionsumfang am größten — Guthaben-/Credit-Anzeige und die
    Anbieter-übergreifende Modellvielfalt (GPT/Claude/Mistral/Gemini/Kimi/Qwen unter einem Token)
    gibt es nur dort. Im Fließtext beiläufig mittragen, nicht als Werbeblock. Betrifft README (DE/EN)
    und die F-Droid-`full_description` (de/en/da).
  - **Namensänderung Scatter konsistent** durchziehen (README, About, alle `.md`).
  - **About** überarbeiten (Funktionsumfang, nicht nur der Datenschutztext).
  - **Screenshots** → `docs/screenshots/`, macht der Nutzer selbst nach der neuen Tonalität;
    Doppelnutzen mit den F-Droid-Metadaten (`fastlane/metadata/android/*/`, liegen seit v0.9.6).
  - **F-Droid-Bedarf gleich mitziehen**, nicht zweimal anfassen.
  - **Entschieden 2026-07-17:** Das **Pflichtenheft bleibt im Repo** — 53 Kotlin-Dateien verweisen
    per `§`-Kommentar darauf, ohne die Datei zeigten die ins Leere (F-Droid-Leser lesen den Code).
    Die **README verlinkt es aber nicht mehr** (öffentliche Ansicht = fertiges Produkt, keine
    offene Werkstatt). Betrifft README-Zeile 67 (DE) und 127 (EN), die beide Dokumente in einem
    Satz nennen. Offen: ob der Backlog ebenso nur entlinkt oder ganz aus dem Repo genommen wird.
  - `CLAUDE.md` mit prüfen: verweist aufs Pflichtenheft und enthält lokale Pfade/`~/.zshrc`-Details.
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
