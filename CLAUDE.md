# ScatterTo

Native Android-App (Kotlin, Jetpack Compose): nimmt per Share-Intent eine Artikel-URL entgegen, generiert über die Mammouth.ai-API je einen Post-Text für Mastodon (Deutsch) und Bluesky (Englisch) und postet nach manueller Freigabe.

## Verbindliche Spezifikation

[pflichtenheft-share-app.md](pflichtenheft-share-app.md) ist die einzige und verbindliche Spezifikation — **inklusive §12** (Review-Ergänzungen: 12.1 und 12.2 sind Pflicht, 12.3 sind Empfehlungen, 12.4 dokumentiert entschiedene Design-Fragen). Bei scheinbarem Widerspruch zwischen Abschnitten gilt §12. Vor jeder Implementierungsarbeit die relevanten Abschnitte lesen.

## Eckdaten

- App-Name: **ScatterTo**, `applicationId`: `io.github.ollrich.scatterto`
- `minSdk` 34, `targetSdk` 35, Kotlin + Jetpack Compose + Material 3, Dark Mode systemabhängig
- Netzwerk: Retrofit + OkHttp + kotlinx.serialization; HTML-Parsing: jsoup
- UI-Sprache: Deutsch (einzige Locale)
- Kein DI-Framework — manuelle Injection über die Application-Klasse (§12.3 Nr. 2)
- `minifyEnabled false` für v1 (§12.3 Nr. 3)

## Build & Release

- **Auf dieser Entwicklungsmaschine ist kein JDK und kein Android SDK installiert.** Release-Builds laufen ausschließlich über GitHub Actions. Für lokale Builds müssten zuerst JDK 17+ und das Android SDK (Command-Line-Tools genügen) installiert werden — vorher mit dem Nutzer klären.
- Signing: Der Release-Keystore liegt **nicht** im Repo (lokal unter `signing/`, gitignored). Der CI-Workflow [.github/workflows/release.yml](.github/workflows/release.yml) rekonstruiert ihn aus dem Secret `SIGNING_KEYSTORE_BASE64` und übergibt dem Gradle-Build vier Umgebungsvariablen:
  `SIGNING_STORE_FILE`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`.
  Die `signingConfig` in `app/build.gradle.kts` muss genau diese Env-Variablen lesen. Fehlen sie (lokaler Build), darf der Build nicht scheitern — dann unsigned bzw. nur Debug bauen.
- Keystore-Format: **PKCS12**, Alias `scatterto`, Key- und Store-Passwort identisch.
- Release-Ablauf: `versionCode`/`versionName` erhöhen (Name = Tag ohne „v") → Tag `vX.Y.Z` pushen → CI baut `scatterto-vX.Y.Z.apk` und hängt sie als einziges Asset an ein GitHub Release (Obtainium-Kontrakt, §9.4).

## Konventionen

- Reine, unit-getestete Funktionen für: Facet-Byte-Offsets (Umlaute/Emoji-Fälle), Post-Zusammensetzung, URL-Extraktion aus Share-Text, netzwerkspezifische Zeichenzählung (Mastodon: URL = 23 Zeichen; Bluesky: Grapheme) — §12.3 Nr. 5.
- Credentials nur via EncryptedSharedPreferences (bewusst trotz Deprecation, §12.1 Nr. 1), niemals in Logs; OkHttp-Logging nur in Debug-Builds.
- `android:allowBackup="false"` (§12.1 Nr. 7).
