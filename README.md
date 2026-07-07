# ScatterTo

Teile einen Artikel-Link aus einer beliebigen App — ScatterTo generiert per KI (Mammouth.ai) je einen kurzen, eigenständigen Post auf **Deutsch für Mastodon** und auf **Englisch für Bluesky**. Prüfen, anpassen, mit einem Klick auf beide Netzwerke absenden.

**Status:** in Entwicklung, noch kein Release.

- Verbindliche Spezifikation: [pflichtenheft-share-app.md](pflichtenheft-share-app.md)
- Persönlicher Ein-Nutzer-Client: Token-basierte Anmeldung (kein OAuth), keine Telemetrie, Credentials nur verschlüsselt auf dem Gerät.
- Installation & Updates: per [Obtainium](https://github.com/ImranR98/Obtainium) mit diesem Repo als Quelle. Jedes Release (`v*`-Tag) enthält genau eine signierte APK.

## Installation über Obtainium (privates Repo)

Solange das Repo privat ist, braucht Obtainium einen GitHub-Token:

1. **Token erstellen** — GitHub → Settings → Developer settings → Personal access tokens → *Fine-grained tokens* → *Generate new token*:
   - *Repository access* → *Only select repositories* → `ScatterTo`
   - *Permissions* → *Repository permissions* → **Contents: Read-only** (Metadata: Read-only ist automatisch dabei)
   - *Expiration* nach Wunsch — läuft der Token ab, stoppen Updates bis zur Erneuerung.
2. **Token in Obtainium hinterlegen** — Obtainium → Einstellungen → Abschnitt *GitHub* → *Personal Access Token* → einfügen.
3. **App hinzufügen** — *App hinzufügen* → `https://github.com/ollrich/ScatterTo` → *Hinzufügen*. Obtainium liest das neueste Release und dessen einzige APK.

Wird das Repo später öffentlich, ist der Token nicht mehr nötig.
