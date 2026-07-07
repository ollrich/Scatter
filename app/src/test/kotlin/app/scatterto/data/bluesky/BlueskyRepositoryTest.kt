package app.scatterto.data.bluesky

import app.scatterto.data.bluesky.BlueskyRepository.Companion.atUriToWebUrl
import app.scatterto.data.bluesky.BlueskyRepository.Companion.normalizePds
import org.junit.Assert.assertEquals
import org.junit.Test

class BlueskyRepositoryTest {

    @Test fun buildsWebUrlFromAtUri() {
        assertEquals(
            "https://bsky.app/profile/alice.bsky.social/post/3kabc",
            atUriToWebUrl("at://did:plc:xyz/app.bsky.feed.post/3kabc", "alice.bsky.social"),
        )
    }

    @Test fun defaultsPdsToHttpsWithSlash() {
        assertEquals("https://bsky.social/", normalizePds("bsky.social"))
        assertEquals("https://custom.pds/", normalizePds("https://custom.pds/"))
    }
}
