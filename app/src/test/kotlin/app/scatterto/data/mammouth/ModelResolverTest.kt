package app.scatterto.data.mammouth

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelResolverTest {

    // Realistischer Ausschnitt aus dem echten /v1/models-Katalog.
    private val catalog = listOf(
        "gpt-4.1", "gpt-4.1-mini", "gpt-4o", "gpt-5.1", "gpt-5.2", "gpt-5.4",
        "gpt-5.4-mini", "gpt-5.5", "gpt-5.1-codex", "gpt-image-2",
        "mistral-small-2603", "mistral-medium-3.1", "mistral-medium-3-5", "mistral-large-3",
        "claude-haiku-4-5", "claude-sonnet-4-6", "claude-sonnet-5", "claude-opus-4-5", "claude-opus-4-8",
        "gemini-2.5-flash", "gemini-2.5-pro", "gemini-3.1-pro-preview", "gemini-3.1-pro-preview-customtools",
    )

    @Test fun claudePicksNewestOpus() {
        assertEquals("claude-opus-4-8", ModelResolver.resolve("claude", catalog))
    }

    @Test fun mistralPicksLarge() {
        assertEquals("mistral-large-3", ModelResolver.resolve("mistral", catalog))
    }

    @Test fun geminiPicksStableProNotPreview() {
        assertEquals("gemini-2.5-pro", ModelResolver.resolve("gemini", catalog))
    }

    @Test fun gptPicksNewestGeneralNotMiniOrCodex() {
        assertEquals("gpt-5.5", ModelResolver.resolve("gpt", catalog))
    }

    @Test fun fallsBackWhenListEmpty() {
        assertEquals("claude-opus-4-8", ModelResolver.resolve("claude", emptyList()))
        assertEquals("mistral-large-3", ModelResolver.resolve("mistral", emptyList()))
    }
}
