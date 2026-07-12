package app.scatterto.data

import android.content.Context
import app.scatterto.data.bluesky.BlueskyRepository
import app.scatterto.data.log.EventLog
import app.scatterto.data.mammouth.MammouthRepository
import app.scatterto.data.mastodon.MastodonRepository
import app.scatterto.data.metadata.OgMetadataFetcher

/**
 * Manuelle Dependency-Injection (§12.3 Nr. 2): ein einziger Container, der den Credential-Store,
 * das Protokoll und die Repositories hält. Wird von [app.scatterto.ScatterToApplication] erzeugt.
 */
class AppContainer(context: Context) {
    /** Für nutzersichtbare Strings in ViewModels (via getString). */
    val appContext: Context = context.applicationContext
    val eventLog = EventLog()
    val themePreferences = ThemePreferences(context)
    val credentialStore = CredentialStore(context.applicationContext)
    val metadataFetcher = OgMetadataFetcher()
    val mammouthRepository = MammouthRepository(eventLog)
    val mastodonRepository = MastodonRepository()
    val blueskyRepository = BlueskyRepository(credentialStore, eventLog)
}
