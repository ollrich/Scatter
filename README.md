<div align="center">

<img src="design/icon-source.svg" width="112" alt="Scatter">

# Scatter

**Teile einen Artikel-Link — Scatter bereitet je einen Post für Mastodon und Bluesky vor, auf Wunsch per KI in der pro Netzwerk gewählten Sprache und Tonalität. Prüfen, anpassen, absenden.**

🇩🇪 [Deutsch](#deutsch) · 🇬🇧 [English](#english)

</div>

---

## Deutsch

Scatter ist eine native Android-App für Leute, die denselben Artikel auf **Mastodon** und **Bluesky** teilen. Statt zweimal zu tippen: Link teilen → die App holt die Seiten-Metadaten und bereitet je einen Post pro Netzwerk vor. Auf Wunsch schreibt ein KI-Modell die Texte, jeweils in der pro Netzwerk gewählten Sprache und Tonalität → du prüfst, passt an und sendest.

Es ist ein **persönlicher Ein-Nutzer-Client**: Token-basierte Anmeldung (kein OAuth), keine Telemetrie, Zugangsdaten nur verschlüsselt auf dem Gerät.

### Funktionen

- **Share-Intent:** Link aus einer beliebigen App teilen — die URL landet direkt in Scatter.
- **KI optional:** Die Texte kommen wahlweise von einem KI-Modell oder du schreibst sie selbst. Standardmäßig ist die KI aus. Vier Anbieter mit eigenem API-Token: **Mammouth, OpenAI, Anthropic (Claude), Google (Gemini)**. Je Netzwerk ein eigenständiger Text in der dort gewählten Sprache, keine Übersetzung.
- **Am meisten drin mit Mammouth:** ein Token, sechs Anbieterfamilien (**GPT, Claude, Mistral, Gemini, Kimi, Qwen**) und ihre Modelle darunter, dazu die Anzeige des verbleibenden API-Guthabens direkt im Menü. Die Modell-Listen kommen bei allen Diensten **live vom Anbieter** und aktualisieren sich selbst; Reasoning- und Code-Modelle sind ausgeblendet.
- **Fünf Tonalitäten:** von sachlich-referierend über locker bis zu drei ausgeprägten Stimmen — für jeden Artikel der passende Ton, global einstellbar.
- **Netzwerkauswahl je Post:** Mastodon oder Bluesky einzeln abwählen; abgewählte Netzwerke werden weder generiert noch gesendet.
- **Editierbar:** Text, ergänzende Hashtags (als Chips) und URL pro Netzwerk, mit Zeichen-/Graphem-Zähler (Mastodon-Regeln vs. Bluesky-Grapheme).
- **Bluesky-Link-Karte editierbar:** Titel, Beschreibung und Vorschaubild vor dem Senden anpassen, oder die Karte ganz weglassen; klickbare Links und Hashtags (Facets über UTF-8-Byte-Offsets).
- **Robustes Senden:** je Netzwerk unabhängig, mit Retry; Mastodon-Idempotenz gegen Doppelposts; transparenter Bluesky-Session-Refresh.
- **Anzeige:** Hell/Dunkel/System, festes Marken-Blau oder Material You (dynamische Farben), App-Sprache Deutsch, Englisch oder Dänisch.
- **Protokoll** der letzten Aktionen zur Fehlersuche — ohne Zugangsdaten.
- **Datensparsam:** Tracking-Parameter werden aus geteilten URLs entfernt.

### Installation

Verteilung über [Obtainium](https://github.com/ImranR98/Obtainium):

1. In Obtainium **App hinzufügen** → `https://github.com/ollrich/Scatter`
2. Obtainium installiert die neueste signierte APK und meldet künftige Updates.

Alternativ die `scatterto-vX.Y.Z.apk` aus den [Releases](https://github.com/ollrich/Scatter/releases) laden und per Sideload installieren.

> **Play Protect** zeigt beim Installieren evtl. „App scan recommended" — das ist harmlos (Google kennt den APK-Hash noch nicht), kein Malware-Fund. Auf „Scan app" tippen.

### Einrichtung

Im Menü (Hamburger oben links):

- **KI (optional):** API-Token des gewählten Dienstes eintragen, Modell wählen. Ohne KI schreibst du die Posts selbst.
- **Konten → Mastodon:** Instanz-URL + Access-Token (in deiner Instanz unter *Einstellungen → Entwicklung*, Scopes `read:accounts` und `write:statuses`).
- **Konten → Bluesky:** Handle + **App Password** (in den Bluesky-Einstellungen erzeugt, nicht das Hauptpasswort).

Details zu den KI-Diensten: [docs/ai-setup.md](docs/ai-setup.md).

### Datenschutz

Keine Telemetrie, kein Analytics. Zugangsdaten (Tokens, App-Password, JWTs) liegen ausschließlich verschlüsselt auf dem Gerät (Android Keystore). Deine Posts gehen an deine Mastodon-Instanz und an Bluesky. Ist die KI aktiv, werden Titel und Beschreibung des Artikels an den von dir gewählten Anbieter gesendet (Mammouth, OpenAI, Anthropic oder Google). Ohne KI verlässt nichts davon das Gerät.

### Screenshots

_Werden mit dem 1.0.0-Release eingebunden._

### Roadmap

Grobe Aussicht, ohne festen Termin:

- **Mehrere Accounts** je Netzwerk, mit Sprachzuweisung pro Konto.
- **F-Droid-Veröffentlichung.**

### Technik

Kotlin · Jetpack Compose · Material 3 · Retrofit/OkHttp/kotlinx.serialization · jsoup · Coil. `minSdk` 34 (Android 14), `targetSdk` 35, `compileSdk` 37. Kein DI-Framework (manuelle Injection). R8-Shrinking im Release.

### Build

Ein GitHub-Actions-Workflow baut bei jedem `v*`-Tag eine **signierte** Release-APK und hängt sie als einziges Asset an ein Release (Obtainium-Kontrakt). Der Signing-Keystore liegt nicht im Repo.

---

## English

Scatter is a native Android app for people who share the same article on **Mastodon** and **Bluesky**. Instead of typing twice: share a link → the app fetches the page metadata and prepares one post per network. Optionally an AI model writes the texts, each in the language and tonality you choose per network → you review, tweak and send.

It is a **personal single-user client**: token-based sign-in (no OAuth), no telemetry, credentials stored encrypted on the device only.

### Features

- **Share intent:** share a link from any app — the URL lands straight in Scatter.
- **AI optional:** the texts come from an AI model of your choice, or you write them yourself. AI is off by default. Four providers with your own API token: **Mammouth, OpenAI, Anthropic (Claude), Google (Gemini)**. One independent text per network in the language chosen there, not a translation.
- **Richest with Mammouth:** one token, six provider families (**GPT, Claude, Mistral, Gemini, Kimi, Qwen**) with their models below, plus your remaining API credit shown right in the menu. Model lists come **live from the provider** for every service and update themselves; reasoning and code models are hidden.
- **Five tonalities:** from factual reporting through casual to three distinct voices — the right tone for each article, set globally.
- **Per-post network selection:** disable Mastodon or Bluesky individually; a disabled network is neither generated nor sent.
- **Editable:** text, extra hashtags (as chips) and URL per network, with character/grapheme counters (Mastodon rules vs. Bluesky graphemes).
- **Editable Bluesky link card:** adjust title, description and thumbnail before sending, or leave the card out entirely; clickable links and hashtags (facets via UTF-8 byte offsets).
- **Resilient sending:** independent per network, with retry; Mastodon idempotency against double posts; transparent Bluesky session refresh.
- **Display:** light/dark/system, fixed brand blue or Material You (dynamic colors), app language German, English or Danish.
- **Activity log** for troubleshooting — without credentials.
- **Data-frugal:** tracking parameters are stripped from shared URLs.

### Installation

Distributed via [Obtainium](https://github.com/ImranR98/Obtainium):

1. In Obtainium, **Add App** → `https://github.com/ollrich/Scatter`
2. Obtainium installs the latest signed APK and tracks future updates.

Or download `scatterto-vX.Y.Z.apk` from the [Releases](https://github.com/ollrich/Scatter/releases) and sideload it.

> **Play Protect** may show “App scan recommended” on install — this is harmless (Google hasn’t seen the APK hash yet), not a malware finding. Tap “Scan app”.

### Setup

In the menu (hamburger, top left):

- **AI (optional):** enter the API token of the chosen service, pick a model. Without AI you write the posts yourself.
- **Accounts → Mastodon:** instance URL + access token (in your instance under *Preferences → Development*, scopes `read:accounts` and `write:statuses`).
- **Accounts → Bluesky:** handle + **App Password** (created in Bluesky settings, not your main password).

Details on the AI services: [docs/ai-setup.md](docs/ai-setup.md).

### Privacy

No telemetry, no analytics. Credentials (tokens, app password, JWTs) are stored encrypted on the device only (Android Keystore). Your posts go to your Mastodon instance and to Bluesky. If AI is enabled, the article's title and description are sent to the provider you choose (Mammouth, OpenAI, Anthropic or Google). Without AI, none of that leaves the device.

### Screenshots

See the German section above.

### Roadmap

Rough outlook, no fixed timeline:

- **Multiple accounts** per network, with a language assigned per account.
- **F-Droid release.**

### Tech

Kotlin · Jetpack Compose · Material 3 · Retrofit/OkHttp/kotlinx.serialization · jsoup · Coil. `minSdk` 34 (Android 14), `targetSdk` 35, `compileSdk` 37. No DI framework (manual injection). R8 shrinking in release.

### Build

A GitHub Actions workflow builds a **signed** release APK on every `v*` tag and attaches it as the single asset of a release (the Obtainium contract). The signing keystore is not in the repo.

---

## Lizenz / License

MIT — siehe / see [LICENSE](LICENSE).
