package app.scatterto.data

import android.content.Context
import app.scatterto.data.bluesky.BlueskyRepository
import app.scatterto.data.mammouth.MammouthRepository
import app.scatterto.data.mastodon.MastodonRepository
import app.scatterto.data.metadata.OgMetadataFetcher

/**
 * Manuelle Dependency-Injection (§12.3 Nr. 2): ein einziger Container, der den Credential-Store
 * und die Repositories hält. Wird von [app.scatterto.ScatterToApplication] erzeugt; die ViewModels
 * greifen darüber zu (kein DI-Framework).
 */
class AppContainer(context: Context) {
    val credentialStore = CredentialStore(context.applicationContext)
    val metadataFetcher = OgMetadataFetcher()
    val mammouthRepository = MammouthRepository()
    val mastodonRepository = MastodonRepository()
    val blueskyRepository = BlueskyRepository(credentialStore)
}
