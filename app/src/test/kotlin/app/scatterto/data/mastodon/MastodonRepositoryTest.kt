package app.scatterto.data.mastodon

import app.scatterto.data.mastodon.MastodonRepository.Companion.normalizeInstanceUrl
import org.junit.Assert.assertEquals
import org.junit.Test

class MastodonRepositoryTest {

    @Test fun addsSchemeAndTrailingSlash() {
        assertEquals("https://mastodon.example/", normalizeInstanceUrl("mastodon.example"))
    }

    @Test fun keepsExistingSchemeAndNormalizesSlash() {
        assertEquals("https://mastodon.example/", normalizeInstanceUrl("https://mastodon.example/"))
    }

    @Test fun trimsWhitespace() {
        assertEquals("https://a.social/", normalizeInstanceUrl("  a.social  "))
    }
}
