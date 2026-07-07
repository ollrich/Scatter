package app.scatterto

import android.app.Application

/**
 * Einstiegspunkt der App und Ort der manuellen Dependency-Injection (§12.3 Nr. 2):
 * Hier werden später der verschlüsselte Credential-Store und die drei Repositories
 * (Mammouth, Mastodon, Bluesky) als [AppContainer] instanziiert und gehalten.
 */
class ScatterToApplication : Application() {
    // lateinit var container: AppContainer  // folgt in der Daten-/Netzwerk-Schicht
}
