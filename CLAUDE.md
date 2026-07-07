# ScatterTo

Native Android-App (Kotlin, Jetpack Compose): nimmt per Share-Intent eine Artikel-URL entgegen, generiert über die Mammouth.ai-API je einen Post-Text für Mastodon (Deutsch) und Bluesky (Englisch) und postet nach manueller Freigabe.

## Verbindliche Spezifikation

[pflichtenheft-share-app.md](pflichtenheft-share-app.md) ist die einzige und verbindliche Spezifikation — **inklusive §12** (Review-Ergänzungen: 12.1 und 12.2 sind Pflicht, 12.3 sind Empfehlungen, 12.4 dokumentiert entschiedene Design-Fragen). Bei scheinbarem Widerspruch zwischen Abschnitten gilt §12. Vor jeder Implementierungsarbeit die relevanten Abschnitte lesen.

## Eckdaten

- App-Name: **ScatterTo**, `applicationId`: `app.scatterto` (neutral, keine personenbezogenen Daten; nach erstem Release unveränderlich)
- `minSdk` 34, `targetSdk` 35, Kotlin + Jetpack Compose + Material 3, Dark Mode systemabhängig
- Netzwerk: Retrofit + OkHttp + kotlinx.serialization; HTML-Parsing: jsoup
- UI-Sprache: Deutsch (einzige Locale)
- Kein DI-Framework — manuelle Injection über die Application-Klasse (§12.3 Nr. 2)
- `minifyEnabled false` für v1 (§12.3 Nr. 3)

## Build & Release

- **Lokale Toolchain ist installiert** (Stand 2026-07-07): OpenJDK 17 (`JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`) und Android SDK (`ANDROID_HOME=/opt/homebrew/share/android-commandlinetools`, Platform 35 + Build-Tools 35.0.0, Lizenzen akzeptiert). Beides ist in `~/.zshrc` exportiert; in einer Login-Shell funktionieren `java`, `adb`, `sdkmanager` direkt. Lokale Debug-Builds und JVM-Unit-Tests (`./gradlew test`) laufen damit ohne CI. Ein Emulator/AVD ist **nicht** installiert (nur Platform + Build-Tools) — Instrumented Tests bräuchten zusätzlich ein Systemabbild. **Release-Signing** passiert weiterhin ausschließlich in der CI (der Keystore liegt nicht auf dieser Maschine im Build-Pfad).
- Signing: Der Release-Keystore liegt **nicht** im Repo (lokal unter `signing/`, gitignored). Der CI-Workflow [.github/workflows/release.yml](.github/workflows/release.yml) rekonstruiert ihn aus dem Secret `SIGNING_KEYSTORE_BASE64` und übergibt dem Gradle-Build vier Umgebungsvariablen:
  `SIGNING_STORE_FILE`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`.
  Die `signingConfig` in `app/build.gradle.kts` muss genau diese Env-Variablen lesen. Fehlen sie (lokaler Build), darf der Build nicht scheitern — dann unsigned bzw. nur Debug bauen.
- Keystore-Format: **PKCS12**, Alias `scatterto`, Key- und Store-Passwort identisch.
- Release-Ablauf: `versionCode`/`versionName` erhöhen (Name = Tag ohne „v") → Tag `vX.Y.Z` pushen → CI baut `scatterto-vX.Y.Z.apk` und hängt sie als einziges Asset an ein GitHub Release (Obtainium-Kontrakt, §9.4).

## Konventionen

- Reine, unit-getestete Funktionen für: Facet-Byte-Offsets (Umlaute/Emoji-Fälle), Post-Zusammensetzung, URL-Extraktion aus Share-Text, netzwerkspezifische Zeichenzählung (Mastodon: URL = 23 Zeichen; Bluesky: Grapheme) — §12.3 Nr. 5.
- Credentials nur via EncryptedSharedPreferences (bewusst trotz Deprecation, §12.1 Nr. 1), niemals in Logs; OkHttp-Logging nur in Debug-Builds.
- `android:allowBackup="false"` (§12.1 Nr. 7).
