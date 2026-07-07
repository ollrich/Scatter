# Pflichtenheft: Share-to-Social Android-App

**App-Name:** ScatterTo (festgelegt 2026-07-07)
**Zielnutzer:** Einzelnutzer (privat, keine Mehrbenutzerfähigkeit im Scope)
**Erstellt für:** Umsetzung mit Claude Code

---

## 1. Zweck

Eine native Android-App, die über den System-Share-Intent eine Artikel-URL entgegennimmt, per KI (Mammouth.ai-API) je einen kurzen, nicht-generischen Post-Text plus einen passenden Hashtag für zwei Netzwerke generiert und diese nach manueller Freigabe postet:

- **Mastodon** — auf **Deutsch**
- **Bluesky** — auf **Englisch**

Die App ersetzt keine bestehende Web-Infrastruktur; sie ist ein eigenständiger, vollständiger Client. KI-Calls und Posting laufen ausschließlich in der App.

---

## 2. Technischer Rahmen

| Aspekt | Festlegung |
|---|---|
| Plattform | Native Android |
| `applicationId` | `app.scatterto` (neutral, ohne personenbezogene Daten; nach erstem Release unveränderlich) |
| Sprache | Kotlin |
| UI-Framework | Jetpack Compose + Material 3 (Material You) |
| `minSdk` | 34 (Android 14) |
| `targetSdk` | 35 (Android 15) |
| Dark Mode | Pflicht — System-abhängig (hell/dunkel folgt Systemeinstellung) |
| Token-Speicherung | Android Keystore via EncryptedSharedPreferences — **kein** Klartext (Deprecation-Hinweis: §12.1 Nr. 1) |
| Netzwerk-Layer | Retrofit + OkHttp + kotlinx.serialization (JSON) |
| HTML-Parsing | jsoup (für OG-Meta-Tag-Extraktion) |

---

## 3. Architektur der App

Drei funktionale Bereiche:

1. **Hauptseite** — URL-Eingabe, KI-Generierung, Vorschau/Edit je Netzwerk, Absenden.
2. **Admin-/Einstellungsmenü** — Mammouth-Token + Modellauswahl, Account-Verbindungen.
3. **Share-Intent-Handler** — Einsprungpunkt aus dem System-Share-Dialog.

Empfohlenes Muster: MVVM mit einem `ViewModel` pro Screen, Repository-Schicht für die drei externen APIs (Mammouth, Mastodon, Bluesky). Kein Backend, keine Datenbank nötig; flüchtiger UI-State plus verschlüsselter Credential-Store reichen.

---

## 4. Admin-/Einstellungsmenü

### 4.1 Mammouth-Konfiguration

- Eingabefeld für den **Mammouth-API-Token** (maskiert, mit Sichtbarkeits-Toggle).
- Darunter ein **Dropdown zur Modellauswahl** mit diesen vier vordefinierten Optionen sowie einer fünften Option „Eigene Modell-ID…" (entschieden 2026-07-07, siehe §12.4):

  | Anzeigename | Modell-ID | Anbieter |
  |---|---|---|
  | GPT-4.1 mini | `gpt-4.1-mini` | OpenAI |
  | Mistral Medium 3.1 | `mistral-medium-3.1` | Mistral (EU) |
  | Gemini 2.5 Flash | `gemini-2.5-flash` | Google |
  | Claude Haiku 4.5 | `claude-haiku-4-5` | Anthropic |

  Default: Mistral Medium 3.1 (EU-Anbieter, passende Sprachqualität).
- Die Option „Eigene Modell-ID…" blendet ein Freitextfeld ein; die Eingabe wird unverändert als `model`-Parameter an die API übergeben. Das macht die App unabhängig von ID-Änderungen beim Anbieter (vgl. §11), ohne dass ein App-Update nötig wird.
- **Optionale Validierung** beim Speichern des Tokens: ein Call gegen `GET https://api.mammouth.ai/v1/models`, um zu prüfen, ob Token gültig und die gewählte Modell-ID vorhanden ist. Schlägt der Call fehl, Hinweis anzeigen, Speichern trotzdem erlauben (offline-tolerant).

- API-Basis: `https://api.mammouth.ai/v1` (OpenAI-kompatibel).

### 4.2 Account-Verbindungen

Für beide Netzwerke gilt: **direkte Token-basierte Authentifizierung, kein OAuth.** Begründung — für einen persönlichen Ein-Nutzer-Client ist das die stabilere Architektur (kein Redirect-Flow, kein bzw. simpler Refresh-Zyklus).

**Mastodon**
- Eingabefelder: Instanz-URL (z. B. `https://mastodon.example`) und Access Token.
- Der Token wird vom Nutzer manuell in den Kontoeinstellungen der Instanz erzeugt (Entwicklung → Neue Anwendung, Scopes mind. `write:statuses` und `read:accounts`). Dieser Token läuft nicht ab.
- Nach dem Speichern: Call `GET /api/v1/accounts/verify_credentials` → Handle und Avatar anzeigen.

**Bluesky**
- Eingabefelder: Handle/Identifier (z. B. `alice.bsky.social`) und **App Password** (nicht das Hauptpasswort; in den Bluesky-Einstellungen erzeugt).
- Ablauf: `com.atproto.server.createSession` mit Identifier + App Password → liefert `accessJwt`, `refreshJwt`, `did`, `handle`.
- Token-Refresh über `com.atproto.server.refreshSession` mit dem `refreshJwt`, wenn der `accessJwt` abgelaufen ist. Refresh-Logik im Repository kapseln (transparent bei 401).
- Avatar über `app.bsky.actor.getProfile`.
- Standard-PDS-Endpoint konfigurierbar lassen (Default `https://bsky.social`), da eigene PDS möglich ist.

**Anzeige verbundener Accounts**
Pro Netzwerk nach erfolgreicher Verbindung mindestens:
- Handle
- Profilfoto (Avatar)
- Netzwerk-Kennzeichnung: Name plus passend eingefärbter Titel bzw. Logo (Mastodon-Violett `#6364FF`, Bluesky-Blau `#0085FF`).
- Status-Indikator (verbunden / Token-Fehler).
- Möglichkeit zum Trennen/Neu-Verbinden.

---

## 5. Hauptseite

### 5.1 URL-Feld (oben)

- Wird die App über den Share-Intent geöffnet, ist das URL-Feld **automatisch mit der geteilten URL vorbefüllt** und der KI-Prozess startet **automatisch**.
- Wird die App manuell geöffnet, kann eine URL manuell eingefügt werden. In diesem Fall startet der Prozess **erst per Button** („Generieren") unter dem Feld.

### 5.2 Metadaten-Beschaffung (Variante c)

- App lädt das HTML der URL selbst und extrahiert per jsoup die Open-Graph-Tags: `og:title`, `og:description` (Fallback auf `<title>` bzw. `<meta name="description">`).
- **Fallback bei fehlenden Metadaten:** Können keine brauchbaren Meta-Tags geladen werden (Paywall, JS-SPA, Timeout), zeigt die App zwei manuell editierbare Felder (Titel, Beschreibung), die der Nutzer selbst befüllt, bevor der KI-Call ausgelöst wird. Es wird kein voller Artikeltext geladen — Titel + Beschreibung genügen für die Textgenerierung.

### 5.3 KI-Call

- **Genau ein** Call pro Generierung (token-sparsam), strukturiertes JSON zurück.
- `response_format: { "type": "json_object" }` setzen.
- Erwartetes Antwortschema:

  ```json
  {
    "de_text": "kurzer deutscher Post-Text, nicht generisch",
    "de_hashtag": "#Beispiel",
    "en_text": "short English post text, non-generic",
    "en_hashtag": "#Example"
  }
  ```

- System-Prompt-Anforderungen (knapp halten, das ist der Token-Hebel):
  - Deutscher Text für Mastodon, englischer Text für Bluesky — **keine Übersetzung, zwei eigenständige Texte**.
  - Ton: locker, idiomatisch, à la „Hab das gerade gefunden" / casual find. Kein Marketing-Sprech, kein generisches „Interessanter Artikel über…".
  - Hashtag **je Netzwerk separat** aus dem Inhalt (primär Headline) ableiten — kann sich zwischen DE und EN unterscheiden. Genau ein Hashtag pro Netzwerk.
  - Kein zusätzlicher Fließtext, nur das JSON.
- Robustes Parsing: Antwort defensiv parsen (evtl. Markdown-Fences strippen), bei Parse-Fehler klare Fehlermeldung plus Retry-Möglichkeit.

### 5.4 Vorschau- und Edit-Bereiche

Zwei separate Bereiche untereinander, je Netzwerk. **Keine** gerenderte Netzwerk-Vorschau — nur die editierbaren Textbausteine:

1. **Text** (mehrzeilig editierbar)
2. **Hashtag** (editierbar)
3. **URL** (editierbar, vorbefüllt aus dem URL-Feld)

Jeder Bereich ist klar dem Netzwerk zugeordnet (eingefärbter Titel/Logo wie in 4.2). Zeichen-/Graphem-Zähler pro Netzwerk anzeigen (Mastodon-Limit instanzabhängig, i. d. R. 500; Bluesky 300 Graphemes). Die Zählregeln der beiden Netzwerke unterscheiden sich grundlegend — siehe §12.1 Nr. 2.

### 5.5 Absenden

- **Ein Button**, der an alle **verbundenen** Netzwerke sendet. Ist nur ein Netzwerk verbunden, wird nur dieses bespielt; der Bereich des nicht verbundenen Netzwerks ist inaktiv (entschieden 2026-07-07, siehe §12.4 Nr. 4).
- Zusammengesetzter Post pro Netzwerk: `Text` + Leerzeile/Trenner + `Hashtag` + `URL` (exakte Zusammensetzung im Code als reine Funktion kapseln, damit testbar).

---

## 6. Facets & Link-Karte (Bluesky) — Pflicht

Bluesky rendert Links und Hashtags **nicht** automatisch aus Plaintext. Die App muss beim Posting `facets` mitschicken, sonst sind URL und Hashtag nicht klickbar.

- Für die URL: Facet vom Typ `app.bsky.richtext.facet#link`.
- Für den Hashtag: Facet vom Typ `app.bsky.richtext.facet#tag`.
- **Byte-Offsets, nicht Zeichen-Offsets:** `byteStart`/`byteEnd` beziehen sich auf UTF-8-Bytes. Bei Umlauten/Emoji weichen Byte- und Zeichenindex ab — Offsets korrekt über die UTF-8-Byte-Repräsentation des Post-Strings berechnen.
- Facet-Berechnung als eigene, unit-getestete Funktion implementieren.

**Link-Vorschau-Karte (entschieden 2026-07-07, siehe §12.4 Nr. 1):** Bluesky erzeugt — anders als Mastodon — serverseitig **keine** Link-Vorschau. Die App schickt deshalb beim Posten zusätzlich ein `app.bsky.embed.external` mit:

- `uri` = Artikel-URL, `title` = `og:title`, `description` = `og:description` (bzw. die manuellen Fallback-Werte aus 5.2).
- `thumb` = `og:image`: Bild herunterladen, bei Bedarf herunterskalieren und als JPEG re-komprimieren (Blob-Größenlimit ≈ 1 MB beachten), per `com.atproto.repo.uploadBlob` hochladen und die zurückgegebene Blob-Referenz eintragen.
- Fallback-Kette: kein `og:image` vorhanden oder Bild-Download/-Upload scheitert → Karte ohne `thumb` senden; scheitert die Karten-Erstellung insgesamt → Post ohne Embed absetzen. **Die Karte darf das Posten nie blockieren.**

Mastodon rendert Hashtags und Links serverseitig aus dem Klartext und erzeugt die Link-Vorschau selbst — dort weder Facets noch Embed nötig.

---

## 7. Fehler- und Teilerfolg-Handling

Der Doppelpost darf bei Teilfehlern **nicht** zu Doppelposts führen.

- Posting je Netzwerk **unabhängig** ausführen und Status **getrennt** tracken (z. B. `Idle / Pending / Success / Failed(reason)`).
- Nach dem Absenden pro Netzwerk sichtbarer Status (Erfolg mit Link zum Post / Fehler mit Grund).
- **Retry nur für das fehlgeschlagene Netzwerk.** Ein erneuter Klick darf ein bereits erfolgreich gepostetes Netzwerk nicht erneut bespielen.
- Häufige Fehlerursachen berücksichtigen: abgelaufener Bluesky-`accessJwt` (→ automatischer Refresh + einmaliger Retry), Rate Limit (HTTP 429), Netzwerkfehler, ungültiger/fehlender Token (→ Verweis ins Admin-Menü).

---

## 8. Sicherheit / Datenschutz

- Alle Credentials (Mammouth-Token, Mastodon-Token, Bluesky-App-Password bzw. -JWTs) ausschließlich verschlüsselt via Android Keystore / EncryptedSharedPreferences.
- Keine Telemetrie, kein Analytics, keine Weitergabe an Dritte außer den drei genannten APIs.
- Keine Credentials im Log, keine Credentials im Klartext im Speicher persistieren.

---

## 9. Versionierung & Distribution (GitHub + Obtainium)

Die App wird über ein GitHub-Repository versioniert und als APK über GitHub Releases bereitgestellt, damit sie per **Obtainium** installiert und aktualisiert werden kann.

### 9.1 Repository

- Repo: `https://github.com/ollrich/ScatterTo` (angelegt 2026-07-07, zunächst privat — bei privatem Repo Obtainium mit GitHub-Token konfigurieren, oder Repo vor dem ersten Release auf öffentlich stellen).
- Semantische Versionierung über Git-Tags: `v1.0.0`, `v1.1.0`, …
- `versionCode` und `versionName` in `build.gradle` bei jedem Release erhöhen; `versionName` muss zum Tag passen.

### 9.2 Signierung — kritisch für Obtainium

- Die Release-APK muss mit einem **stabilen, gleichbleibenden Signing-Key** signiert werden. Obtainium verweigert Updates bei Signaturwechsel (dann nur Neuinstallation möglich).
- Der Keystore darf **nicht** ins Repo. Er wird als Base64-codiertes GitHub-Actions-Secret hinterlegt (z. B. `SIGNING_KEYSTORE_BASE64`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, `SIGNING_STORE_PASSWORD`).
- Einmalig einen Keystore erzeugen, sicher sichern (Verlust = kein Update-Pfad mehr).

### 9.3 GitHub-Actions-Workflow

Ein Workflow, der bei Push eines Version-Tags (`v*`) ausgelöst wird und:

1. JDK + Android-SDK einrichtet.
2. Keystore aus dem Base64-Secret rekonstruiert.
3. Eine **signierte Release-APK** baut (`assembleRelease`).
4. Ein GitHub Release zum Tag erstellt und die APK als **Release-Asset** anhängt.

Der APK-Dateiname sollte stabil und versioniert sein (z. B. `scatterto-v1.0.0.apk`). Obtainium liest das Release-Asset des jeweils neuesten Releases.

### 9.4 Obtainium-Kompatibilität

- App-Quelle in Obtainium: die GitHub-Repo-URL.
- Sicherstellen, dass jedes Release genau eine APK als Asset enthält (sonst muss in Obtainium ein Filter gesetzt werden).
- `versionName` konsistent mit dem Tag halten, damit Obtainium Updates korrekt erkennt.

---

## 10. Abgrenzung (nicht im Scope)

- Keine Mehrbenutzerfähigkeit.
- Kein OAuth-Flow.
- Kein Laden/Verarbeiten des vollen Artikeltextes (nur OG-Metadaten bzw. manueller Fallback).
- Keine gerenderte Netzwerk-Vorschau.
- Keine Bild-/Media-Anhänge im Post. (Das Thumbnail der Bluesky-Link-Karte aus §6 ist Teil der Link-Vorschau, kein Media-Anhang.)
- Keine Thread-/Multi-Post-Funktion.
- Kein iOS.

---

## 11. Offene Punkte für die Umsetzung

- Exakte Mammouth-Modell-IDs vor dem ersten Build gegen `GET /v1/models` gegenprüfen (IDs können sich beim Anbieter ändern).
- Ob der Mammouth-Proxy `response_format: json_object` für alle vier Anbieter durchreicht, beim ersten Test prüfen (Fallback-Verhalten definiert in §12.2 Nr. 5).
- App-Icon final gestalten. *(Name ist festgelegt: **ScatterTo** — 2026-07-07.)*

*(Der frühere Punkt „Mastodon-Zeichenlimit instanzabhängig" ist keine offene Frage mehr — festgelegt in §12.2 Nr. 7.)*

---

## 12. Review-Ergänzungen (2026-07-07)

Ergebnis eines technischen Reviews dieses Pflichtenhefts. **Alle Punkte in 12.1 und 12.2 sind Teil der Spezifikation**, 12.3 sind Empfehlungen. Die vier gemeinsam mit dem Auftraggeber entschiedenen Fragen stehen in 12.4 und sind zusätzlich direkt in §2, §4.1, §5.4, §5.5, §6, §10 und §11 eingearbeitet.

### 12.1 Korrekturen & Risiken (Muss)

1. **`androidx.security:security-crypto` (EncryptedSharedPreferences) ist deprecated** (→ §2, §8). Die Bibliothek wird von Google nicht weiterentwickelt, funktioniert aber stabil. Festlegung für v1: trotzdem verwenden (letzte veröffentlichte Version pinnen) — für eine Ein-Nutzer-App das geringste Risiko; eine selbstgebaute Keystore-Lösung (AES/GCM + DataStore) wäre fehleranfälliger. Deprecation als Code-Kommentar dokumentieren, Migration ggf. später.
2. **Zeichenzählung ist netzwerkspezifisch** (→ §5.4). Mastodon zählt **jede URL pauschal als 23 Zeichen**, unabhängig von ihrer tatsächlichen Länge; Hashtags zählen voll. Bluesky zählt echte Grapheme über den gesamten Text **inklusive voller URL** (Graphem-Zählung in Kotlin: `android.icu.text.BreakIterator.getCharacterInstance()`). Der Zähler muss den **zusammengesetzten Gesamt-Post** (Text + Hashtag + URL gemäß §5.5) bewerten, nicht nur das Textfeld. Bei Überschreitung: Senden für das betroffene Netzwerk deaktivieren (die API würde ohnehin ablehnen), das andere Netzwerk bleibt sendbar.
3. **Share-Intents liefern selten nur die URL** (→ §3, §5.1). Viele Apps übergeben in `EXTRA_TEXT` „Titel + URL" oder Fließtext mit eingebetteter URL. Die URL per Regex extrahieren (erste `http(s)://`-URL im Text); ein vorhandenes `EXTRA_SUBJECT` als Titel-Kandidat für den Fallback aus §5.2 vormerken. Enthält der geteilte Text keine URL → Hinweis anzeigen, URL-Feld leer lassen.
4. **Bluesky-Session persistieren** (→ §4.2). `createSession` ist serverseitig rate-limitiert und darf nicht bei jedem App-Start aufgerufen werden: `accessJwt`/`refreshJwt` verschlüsselt persistieren und wiederverwenden. Auch der `refreshJwt` läuft irgendwann ab bzw. kann invalidiert werden — deshalb bleibt das App-Password gespeichert; scheitert der Refresh, erzeugt das Repository transparent eine neue Session (ohne Nutzerinteraktion).
5. **Doppelpost-Schutz konkretisiert** (→ §7). Mastodon: `POST /api/v1/statuses` immer mit **`Idempotency-Key`-Header** senden (pro Generierung und Netzwerk eine zufällige UUID, die bis zum bestätigten Erfolg unverändert bleibt) — deckt auch den Timeout-Fall ab („Request kam an, Antwort ging verloren"). Bluesky hat keinen Idempotenz-Mechanismus: Nach einem Timeout mit unklarem Ausgang **nicht** automatisch retryen, sondern Status „unklar — bitte im Profil prüfen" anzeigen und den Retry dem Nutzer überlassen.
6. **Timeouts differenzieren** (→ §5.2, §5.3). Der LLM-Call braucht ein langes Read-Timeout (**≥ 60 s**) — der OkHttp-Default (10 s) bricht sonst mitten in der Generierung ab. Der OG-Fetch dagegen kurz (≈ 10 s), Redirects folgen, Response-Größe begrenzen (1–2 MB genügen für den `<head>`), realistischen Browser-User-Agent setzen (Default-UAs werden von manchen Seiten geblockt oder ohne OG-Tags beliefert).
7. **Auto-Backup ausschließen** (→ §8). `android:allowBackup="false"` setzen (oder Backup-Rules, die die Prefs ausschließen). Sonst landen die verschlüsselten Prefs im Google-Backup, die Keystore-Schlüssel wandern aber nicht mit — nach einem Geräte-Restore wäre die App in einem defekten Zustand.

### 12.2 Präzisierungen

1. **Sprach-Metadaten setzen** (→ §5.5, §6): Mastodon-Post mit `language=de`, Bluesky-Record mit `langs=["en"]`. Trivial umzusetzen, verbessert Filterung und Reichweite.
2. **Flow bei Metadaten-Fallback** (→ §5.1/§5.2): Auch beim Share-Einsprung gilt — scheitert der OG-Fetch, startet der KI-Call **nicht** automatisch, sondern erst nachdem der Nutzer Titel/Beschreibung befüllt und „Generieren" gedrückt hat.
3. **Längenbudget im Prompt mitgeben** (→ §5.3): Da die volle URL im Bluesky-Text steht (§12.4 Nr. 2), das Budget vor dem Call dynamisch berechnen: `300 − Graphemlänge(URL) − Reserve für Hashtag und Trenner (~40)` als Obergrenze für `en_text`; analog für `de_text` gegen das Mastodon-Limit (URL zählt dort fix 23). Ohne diese Vorgabe generiert das Modell regelmäßig zu lange Texte.
4. **KI-Antwort normalisieren/validieren** (→ §5.3): fehlendes „#" beim Hashtag ergänzen, Leerzeichen im Hashtag entfernen (CamelCase zusammenziehen), leere/fehlende Felder → Fehlermeldung + Retry. Überlange Texte nicht stillschweigend kürzen — anzeigen und den Zähler warnen lassen (der Nutzer editiert ohnehin).
5. **`response_format`-Fallback** (→ §5.3): Ob der Mammouth-Proxy `json_object` für alle vier Anbieter durchreicht, ist unklar. Antwortet die API mit HTTP 400, den Call einmalig ohne `response_format` wiederholen (das defensive Parsing existiert ohnehin).
6. **Hashtag-Facet-Detail** (→ §6): Im `tag`-Feld des Facets steht der Tag **ohne** führendes „#"; im Post-Text steht er mit „#", und die Byte-Range umfasst das „#".
7. **Mastodon-Zeichenlimit dynamisch** (aus §11 übernommen, jetzt festgelegt): Beim Verbinden `GET /api/v1/instance` abrufen, `configuration.statuses.max_characters` persistieren, Fallback 500.
8. **Neuer Share bei laufender App** (→ §3): `launchMode="singleTask"` (o. Ä.) + `onNewIntent`; ein neuer Share ersetzt den aktuellen Zustand kommentarlos (Einzelnutzer-Flow, kein Draft-Management).
9. **Edge-to-Edge** (→ §2): targetSdk 35 erzwingt Edge-to-Edge — `enableEdgeToEdge()` plus Insets-Handling in Compose (Scaffold-Padding) von Anfang an einplanen, sonst liegen Inhalte unter Status-/Navigationsleiste.
10. **UI-Sprache**: Deutsch (einzige Locale für v1).
11. **Leere Zustände** (→ §5): Ohne gespeicherten Mammouth-Token ist Generieren gesperrt, ohne verbundene Netzwerke ist Senden gesperrt — jeweils mit Hinweistext und Direktlink ins Einstellungsmenü.

### 12.3 Empfehlungen (Kann — bei Aufwandsdruck streichbar)

1. **Tracking-Parameter strippen**: Bekannte Tracker (`utm_*`, `fbclid`, `gclid`, `igshid`) vor dem Vorbefüllen aus der URL entfernen; die URL-Felder bleiben editierbar, der Nutzer kann es also übersteuern.
2. **Kein DI-Framework**: Manuelle Injection (Application-Klasse hält die drei Repositories) statt Hilt — bei drei Screens weniger Boilerplate und Build-Magie.
3. **`minifyEnabled false` für v1**: Vermeidet R8-Keep-Regel-Fallen mit Retrofit/kotlinx.serialization; die APK-Größe ist bei Obtainium-Distribution unkritisch.
4. **Process-Death überleben**: Generierte Texte + URL im `SavedStateHandle` halten, damit ein bezahlter KI-Call nicht verloren geht, wenn Android die App im Hintergrund beendet.
5. **Test-Mindestumfang** (konkretisiert §5.5/§6): Facet-Byte-Offsets (Testfälle: Umlaute, Emoji, Hashtag am Textende), Post-Zusammensetzungs-Funktion, URL-Extraktion aus Share-Text, netzwerkspezifische Zeichenzählung (URL-23-Regel, Grapheme).
6. **Nur HTTPS fetchen**: Bei `http://`-URLs einmal `https://` versuchen; scheitert das, greift der manuelle Fallback aus §5.2 (kein `usesCleartextTraffic`-Opt-in).

### 12.4 Entschiedene Punkte (mit Auftraggeber, 2026-07-07)

| # | Frage | Entscheidung |
|---|---|---|
| 1 | Bluesky-Link-Vorschau | **Karte mit Thumbnail** (`app.bsky.embed.external` + `uploadBlob`); Fallback-Kette: Karte ohne Bild → Post ohne Karte. Eingearbeitet in §6 und §10. |
| 2 | URL im Bluesky-Text | **Volle URL im Text**, keine Kürzungslogik. Konsequenz: dynamisches Längenbudget im KI-Prompt (§12.2 Nr. 3). |
| 3 | Modellauswahl | **4 vordefinierte Modelle + Freitextfeld „Eigene Modell-ID"**. Eingearbeitet in §4.1. |
| 4 | Nur ein Netzwerk verbunden | **Ein verbundenes Netzwerk genügt**: KI generiert weiterhin beide Texte (bleibt ein Call), aktiv und sendbar ist nur der Bereich der verbundenen Netzwerke. Eingearbeitet in §5.5. |
