<div align="center">

<img src="design/icon-source.svg" width="112" alt="ScatterTo">

# ScatterTo

**Teile einen Artikel-Link — ScatterTo macht daraus per KI je einen Post für Mastodon und Bluesky, in der pro Netzwerk gewählten Sprache. Prüfen, anpassen, absenden.**

🇩🇪 [Deutsch](#deutsch) · 🇬🇧 [English](#english)

</div>

---

## Deutsch

ScatterTo ist eine native Android-App für Leute, die denselben Artikel auf **Mastodon** und **Bluesky** teilen. Statt zweimal zu tippen: Link teilen → die App holt die Seiten-Metadaten, ein KI-Modell schreibt je einen kurzen, sachlichen Post pro Netzwerk (in der pro Netzwerk gewählten Sprache) → du prüfst, passt an und sendest.

Es ist ein **persönlicher Ein-Nutzer-Client**: Token-basierte Anmeldung (kein OAuth), keine Telemetrie, Zugangsdaten nur verschlüsselt auf dem Gerät.

### Funktionen

- **Share-Intent:** Link aus einer beliebigen App teilen — die URL landet direkt in ScatterTo.
- **KI-Generierung** über [Mammouth.ai](https://mammouth.ai): sachlicher Stil, je ein Post für Mastodon und Bluesky in der jeweils gewählten Sprache — eigenständige Texte, keine Übersetzung.
- **Modellauswahl:** bei Mammouth wählst du erst den Anbieter (**GPT, Claude, Mistral, Gemini, Kimi, Qwen**) und darunter das konkrete Text-Modell; bei den Direkt-Diensten direkt das Modell. Die Modell-Listen kommen **live vom Anbieter** und aktualisieren sich selbst — neue Modelle erscheinen, abgekündigte verschwinden. Reasoning- und Code-Modelle sind ausgeblendet.
- **Netzwerkauswahl je Post:** Mastodon oder Bluesky einzeln abwählen; abgewählte Netzwerke werden weder generiert noch gesendet.
- **Editierbar:** Text, ergänzende Hashtags (als Chips) und URL pro Netzwerk, mit Zeichen-/Graphem-Zähler (Mastodon-Regeln vs. Bluesky-Grapheme).
- **Bluesky korrekt:** klickbare Links und Hashtags (Facets über UTF-8-Byte-Offsets) plus Link-Vorschaukarte mit Vorschaubild.
- **Robustes Senden:** je Netzwerk unabhängig, mit Retry; Mastodon-Idempotenz gegen Doppelposts; transparenter Bluesky-Session-Refresh.
- **Dark Mode** (System / Hell / Dunkel), Material You.
- **Protokoll** der letzten Aktionen zur Fehlersuche — ohne Zugangsdaten.

### Installation

Verteilung über [Obtainium](https://github.com/ImranR98/Obtainium):

1. In Obtainium **App hinzufügen** → `https://github.com/ollrich/ScatterTo`
2. Obtainium installiert die neueste signierte APK und meldet künftige Updates.

Alternativ die `scatterto-vX.Y.Z.apk` aus den [Releases](https://github.com/ollrich/ScatterTo/releases) laden und per Sideload installieren.

> **Play Protect** zeigt beim Installieren evtl. „App scan recommended" — das ist harmlos (Google kennt den APK-Hash noch nicht), kein Malware-Fund. Auf „Scan app" tippen.

### Einrichtung

Im Menü (Hamburger oben links):

- **Mammouth-KI:** API-Token eintragen, Anbieter/Modell wählen.
- **Accounts → Mastodon:** Instanz-URL + Access-Token (in deiner Instanz unter *Einstellungen → Entwicklung*, Scopes `read:accounts` und `write:statuses`).
- **Accounts → Bluesky:** Handle + **App Password** (in den Bluesky-Einstellungen erzeugt, nicht das Hauptpasswort).

### Datenschutz

Keine Telemetrie, kein Analytics. Zugangsdaten (Tokens, App-Password, JWTs) liegen ausschließlich verschlüsselt auf dem Gerät (Android Keystore). Daten verlassen das Gerät nur zu den drei von dir konfigurierten Diensten: Mammouth, deine Mastodon-Instanz und Bluesky.

### Roadmap

Grobe Aussicht, ohne festen Termin:

- **Bluesky-Link-Karte in der Post-Sprache** (Karten-Titel/-Text), inkl. Hinweis, wenn der verlinkte Artikel in einer anderen Sprache ist.
- **App-Oberfläche in mehreren Sprachen** (Deutsch, Englisch, Dänisch).
- **Mehrere Accounts** je Netzwerk, mit Sprachzuweisung pro Konto.
- **Weitere KI-Anbieter** direkt (OpenAI, Claude, Gemini), KI optional abschaltbar.
- **Eigene Akzentfarbe** als Alternative zu Material You.

### Technik

Kotlin · Jetpack Compose · Material 3 · Retrofit/OkHttp/kotlinx.serialization · jsoup · Coil. `minSdk` 34 (Android 14), `targetSdk` 35. Kein DI-Framework (manuelle Injection).

Verbindliche Spezifikation: [pflichtenheft-share-app.md](pflichtenheft-share-app.md). Arbeitsliste: [BACKLOG.md](BACKLOG.md).

### Build

Ein GitHub-Actions-Workflow baut bei jedem `v*`-Tag eine **signierte** Release-APK und hängt sie als einziges Asset an ein Release (Obtainium-Kontrakt). Der Signing-Keystore liegt nicht im Repo.

---

## English

ScatterTo is a native Android app for people who share the same article on **Mastodon** and **Bluesky**. Instead of typing twice: share a link → the app fetches the page metadata, an AI model writes one short, factual post per network (in the language you choose per network) → you review, tweak and send.

It is a **personal single-user client**: token-based sign-in (no OAuth), no telemetry, credentials stored encrypted on the device only.

### Features

- **Share intent:** share a link from any app — the URL lands straight in ScatterTo.
- **AI generation** via [Mammouth.ai](https://mammouth.ai): factual tone, one post each for Mastodon and Bluesky in the language you choose per network — independent texts, not a translation.
- **Model choice:** with Mammouth you first pick the provider (**GPT, Claude, Mistral, Gemini, Kimi, Qwen**) and then the concrete text model below; with the direct services you pick the model directly. The model lists come **live from the provider** and update themselves — new models appear, retired ones disappear. Reasoning and code models are hidden.
- **Per-post network selection:** disable Mastodon or Bluesky individually; a disabled network is neither generated nor sent.
- **Editable:** text, extra hashtags (as chips) and URL per network, with character/grapheme counters (Mastodon rules vs. Bluesky graphemes).
- **Bluesky done right:** clickable links and hashtags (facets via UTF-8 byte offsets) plus a link preview card with thumbnail.
- **Resilient sending:** independent per network, with retry; Mastodon idempotency against double posts; transparent Bluesky session refresh.
- **Dark mode** (System / Light / Dark), Material You.
- **Activity log** for troubleshooting — without credentials.

### Installation

Distributed via [Obtainium](https://github.com/ImranR98/Obtainium):

1. In Obtainium, **Add App** → `https://github.com/ollrich/ScatterTo`
2. Obtainium installs the latest signed APK and tracks future updates.

Or download `scatterto-vX.Y.Z.apk` from the [Releases](https://github.com/ollrich/ScatterTo/releases) and sideload it.

> **Play Protect** may show “App scan recommended” on install — this is harmless (Google hasn’t seen the APK hash yet), not a malware finding. Tap “Scan app”.

### Setup

In the menu (hamburger, top left):

- **Mammouth AI:** enter the API token, pick a provider/model.
- **Accounts → Mastodon:** instance URL + access token (in your instance under *Preferences → Development*, scopes `read:accounts` and `write:statuses`).
- **Accounts → Bluesky:** handle + **App Password** (created in Bluesky settings, not your main password).

### Privacy

No telemetry, no analytics. Credentials (tokens, app password, JWTs) are stored encrypted on the device only (Android Keystore). Data leaves the device only to the three services you configure: Mammouth, your Mastodon instance and Bluesky.

### Roadmap

Rough outlook, no fixed timeline:

- **Bluesky link card in the post language** (card title/text), with a hint when the linked article is in a different language.
- **App interface in multiple languages** (German, English, Danish).
- **Multiple accounts** per network, with a language assigned per account.
- **More AI providers** directly (OpenAI, Claude, Gemini), AI optionally disableable.
- **Custom accent color** as an alternative to Material You.

### Tech

Kotlin · Jetpack Compose · Material 3 · Retrofit/OkHttp/kotlinx.serialization · jsoup · Coil. `minSdk` 34 (Android 14), `targetSdk` 35. No DI framework (manual injection).

Binding specification (German): [pflichtenheft-share-app.md](pflichtenheft-share-app.md). Working list: [BACKLOG.md](BACKLOG.md).

### Build

A GitHub Actions workflow builds a **signed** release APK on every `v*` tag and attaches it as the single asset of a release (the Obtainium contract). The signing keystore is not in the repo.

---

## Lizenz / License

MIT — siehe [LICENSE](LICENSE).

## Screenshots

_Folgen / coming soon._

<!-- Screenshots werden nach dem Ablegen unter docs/screenshots/ hier eingebunden. -->
