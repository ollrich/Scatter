package app.scatterto.data.mammouth

import app.scatterto.data.model.AiService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelCatalogTest {

    // Realer Mammouth-/v1/models-Katalog (Stand der Nutzer-Abfrage, 2026-07).
    private val catalog = listOf(
        "claude-haiku-4-5", "claude-opus-4-5", "claude-opus-4-6", "claude-opus-4-7", "claude-opus-4-8",
        "claude-sonnet-4", "claude-sonnet-4-5", "claude-sonnet-4-6", "claude-sonnet-5",
        "codestral-2508", "deepseek-r1-0528", "deepseek-v3.1-terminus", "deepseek-v3.2",
        "deepseek-v4-flash", "deepseek-v4-pro", "devstral-2512",
        "gemini-2.5-flash", "gemini-2.5-flash-image", "gemini-2.5-flash-lite", "gemini-2.5-pro",
        "gemini-3-flash-preview", "gemini-3-pro-image-preview", "gemini-3.1-flash-image-preview",
        "gemini-3.1-flash-lite-image", "gemini-3.1-flash-lite-preview", "gemini-3.1-pro-preview",
        "gemini-3.1-pro-preview-customtools", "gemini-3.5-flash",
        "glm-5", "glm-5-turbo", "glm-5.1", "glm-5.2",
        "gpt-4.1", "gpt-4.1-mini", "gpt-4.1-nano", "gpt-4o", "gpt-5.1", "gpt-5.1-chat",
        "gpt-5.1-codex", "gpt-5.1-codex-max", "gpt-5.1-codex-mini", "gpt-5.2", "gpt-5.2-chat",
        "gpt-5.2-codex", "gpt-5.3-chat", "gpt-5.3-codex", "gpt-5.4", "gpt-5.4-mini", "gpt-5.4-nano",
        "gpt-5.5", "gpt-image-2", "grok-4.3", "grok-4.5",
        "kimi-k2.5", "kimi-k2.6", "kimi-k2.7-code",
        "llama-4-maverick", "llama-4-scout", "mammouth-recommended",
        "minimax-m2.7", "minimax-m2.7-highspeed", "minimax-m3",
        "mistral-large-3", "mistral-medium-3-5", "mistral-medium-3.1", "mistral-small-2603",
        "mistral-small-3.2-24b-instruct",
        "qwen3-coder", "qwen3-coder-flash", "qwen3-coder-plus", "qwen3.5-35b-a3b",
        "qwen3.5-397b-a17b", "qwen3.5-9b", "qwen3.7-max", "qwen3.7-plus",
        "sonar-deep-research", "sonar-pro", "text-embedding-3-large", "text-embedding-3-small",
    )

    @Test fun gptOnlyChatModelsNewestFirst() {
        val gpt = ModelCatalog.mammouthModels(MammouthProvider.GPT, catalog)
        assertEquals("gpt-5.5", gpt.first()) // neuestes zuerst
        assertEquals(13, gpt.size)
        assertTrue("gpt-5.4-nano" in gpt)    // nano/mini bleiben (kleine Text-Modelle)
        assertFalse(gpt.any { "codex" in it })
        assertFalse("gpt-image-2" in gpt)
    }

    @Test fun claudeKeepsAllTextModels() {
        val claude = ModelCatalog.mammouthModels(MammouthProvider.CLAUDE, catalog)
        assertEquals(9, claude.size)
        assertEquals("claude-sonnet-5", claude.first())
        assertTrue("claude-opus-4-8" in claude)
        assertTrue("claude-haiku-4-5" in claude)
    }

    @Test fun mistralExcludesCodestralAndDevstral() {
        val mistral = ModelCatalog.mammouthModels(MammouthProvider.MISTRAL, catalog)
        assertEquals(5, mistral.size)
        assertTrue("mistral-large-3" in mistral)
        assertFalse("codestral-2508" in mistral)
        assertFalse("devstral-2512" in mistral)
    }

    @Test fun geminiExcludesImagePreviewAndTools() {
        val gemini = ModelCatalog.mammouthModels(MammouthProvider.GEMINI, catalog)
        assertEquals(4, gemini.size)
        assertTrue("gemini-2.5-pro" in gemini)
        assertTrue("gemini-3.5-flash" in gemini)
        assertFalse(gemini.any { "image" in it || "preview" in it || "customtools" in it })
    }

    @Test fun kimiAndQwenDropCodeModels() {
        val kimi = ModelCatalog.mammouthModels(MammouthProvider.KIMI, catalog)
        assertEquals(listOf("kimi-k2.6", "kimi-k2.5"), kimi)
        val qwen = ModelCatalog.mammouthModels(MammouthProvider.QWEN, catalog)
        assertEquals(5, qwen.size)
        assertFalse(qwen.any { "coder" in it })
    }

    @Test fun isTextModelExcludesNonTextAndReasoningAndCode() {
        listOf(
            "deepseek-r1-0528", "gpt-5.1-codex", "gemini-2.5-flash-image",
            "text-embedding-3-large", "mammouth-recommended", "sonar-deep-research", "gpt-image-2",
        ).forEach { assertFalse(it, ModelCatalog.isTextModel(it)) }
        assertTrue(ModelCatalog.isTextModel("gpt-5.5"))
        assertTrue(ModelCatalog.isTextModel("claude-opus-4-8"))
    }

    @Test fun providerOfModelMapsIdsAndLegacyKeys() {
        assertEquals(MammouthProvider.GPT, MammouthProvider.ofModel("gpt-5.5"))
        assertEquals(MammouthProvider.CLAUDE, MammouthProvider.ofModel("claude-opus-4-8"))
        assertEquals(MammouthProvider.KIMI, MammouthProvider.ofModel("kimi-k2.5"))
        assertEquals(MammouthProvider.QWEN, MammouthProvider.ofModel("qwen3.7-max"))
        assertEquals(MammouthProvider.MISTRAL, MammouthProvider.ofModel("mistral")) // Alt-Schlüssel
        assertEquals(null, MammouthProvider.ofModel("deepseek-v4-pro"))
    }

    @Test fun directModelsFilterByProvider() {
        val claude = ModelCatalog.directModels(AiService.CLAUDE, catalog)
        assertTrue(claude.all { it.startsWith("claude") })
        assertEquals(9, claude.size)
        val gemini = ModelCatalog.directModels(AiService.GEMINI, catalog)
        assertFalse(gemini.any { "image" in it || "preview" in it })
    }
}
