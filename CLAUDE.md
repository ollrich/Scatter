# Scatter

Native Android-App (Kotlin, Jetpack Compose): nimmt per Share-Intent eine Artikel-URL entgegen, generiert optional per KI (Mammouth, OpenAI, Anthropic oder Google, eigener Token) je einen Post-Text pro Netzwerk in der dort eingestellten Sprache und postet nach manueller Freigabe an Mastodon und Bluesky.

## Verbindliche Spezifikation

[pflichtenheft-share-app.md](pflichtenheft-share-app.md) ist die einzige und verbindliche Spezifikation — **inklusive §12** (Review-Ergänzungen: 12.1 und 12.2 sind Pflicht, 12.3 sind Empfehlungen, 12.4 dokumentiert entschiedene Design-Fragen). Bei scheinbarem Widerspruch zwischen Abschnitten gilt §12. Vor jeder Implementierungsarbeit die relevanten Abschnitte lesen.

## Eckdaten

- App-Name: **Scatter** (Anzeigename; bis v0.8.x „ScatterTo"), `applicationId`: `app.scatterto` (unveränderlich, Update-/Cert-Identität)
- `minSdk` 34, `targetSdk` 35, `compileSdk` 37, AGP 9 (Kotlin eingebaut, kein separates kotlin-android-Plugin) + Jetpack Compose + Material 3, Dark Mode systemabhängig
- Netzwerk: Retrofit + OkHttp + kotlinx.serialization; HTML-Parsing: jsoup
- UI-Sprachen: Deutsch (Default), Englisch, Dänisch — die drei `strings.xml` müssen identische Schlüsselmengen behalten; App-Sprache per-App-Locale (`AppLocale`)
- Kein DI-Framework — manuelle Injection über die Application-Klasse (§12.3 Nr. 2)
- `minifyEnabled false` für v1 (§12.3 Nr. 3)

## Build & Release

- **Lokale Toolchain ist installiert** (Stand 2026-07-07): OpenJDK 17 (`JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`) und Android SDK (`ANDROID_HOME=/opt/homebrew/share/android-commandlinetools`, Platforms 35/36/37 + Build-Tools 35.0.0, Lizenzen akzeptiert; `compileSdk` 37 braucht Platform 37 — die CI lädt fehlende Platforms selbst nach). Beides ist in `~/.zshrc` exportiert; in einer Login-Shell funktionieren `java`, `adb`, `sdkmanager` direkt. Lokale Debug-Builds und JVM-Unit-Tests (`./gradlew test`) laufen damit ohne CI. Ein Emulator/AVD ist **nicht** installiert (nur Platform + Build-Tools) — Instrumented Tests bräuchten zusätzlich ein Systemabbild. **Release-Signing** passiert weiterhin ausschließlich in der CI (der Keystore liegt nicht auf dieser Maschine im Build-Pfad).
- Signing: Der Release-Keystore liegt **nicht** im Repo (lokal unter `signing/`, gitignored). Der CI-Workflow [.github/workflows/release.yml](.github/workflows/release.yml) rekonstruiert ihn aus dem Secret `SIGNING_KEYSTORE_BASE64` und übergibt dem Gradle-Build vier Umgebungsvariablen:
  `SIGNING_STORE_FILE`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`.
  Die `signingConfig` in `app/build.gradle.kts` muss genau diese Env-Variablen lesen. Fehlen sie (lokaler Build), darf der Build nicht scheitern — dann unsigned bzw. nur Debug bauen.
- Keystore-Format: **PKCS12**, Alias `scatterto`, Key- und Store-Passwort identisch.
- Release-Ablauf: `versionCode`/`versionName` erhöhen (Name = Tag ohne „v") → Tag `vX.Y.Z` pushen → CI baut `scatterto-vX.Y.Z.apk` und hängt sie als einziges Asset an ein GitHub Release (Obtainium-Kontrakt, §9.4).

## Konventionen

- Reine, unit-getestete Funktionen für: Facet-Byte-Offsets (Umlaute/Emoji-Fälle), Post-Zusammensetzung, URL-Extraktion aus Share-Text, netzwerkspezifische Zeichenzählung (Mastodon: URL = 23 Zeichen; Bluesky: Grapheme) — §12.3 Nr. 5.
- Credentials nur via EncryptedSharedPreferences (bewusst trotz Deprecation, §12.1 Nr. 1), niemals in Logs; OkHttp-Logging nur in Debug-Builds.
- `android:allowBackup="false"` (§12.1 Nr. 7).
- HTTP-Fehler verlassen Repositories einheitlich als `ApiException` (via `apiCall {}` aus `data/net/ApiErrors.kt`) — ViewModels fangen keinen rohen `HttpException`.
- **Neue nutzersichtbare UI-Texte in `res/values/strings.xml`** anlegen statt hartcodiert (Vorbereitung Lokalisierung DE/EN/DA; Bestandstexte werden beim Lokalisierungs-Schritt migriert).
