package app.scatterto.data.mammouth

import app.scatterto.data.model.AiService

/**
 * Anbieter-Familien für die Mammouth-Auswahl und die Filter-/Sortierlogik, die aus der rohen
 * Modell-Liste (Mammouth `/v1/models` bzw. die APIs der Direkt-Dienste) die für kurze Textposts
 * sinnvollen Modelle macht: ohne Bild-/Embedding-/Code-/Reasoning-/Preview-Modelle, neueste zuerst.
 * Reine Funktionen → unit-getestet (§12.3 Nr. 5).
 */

/** Die sechs bei Mammouth wählbaren Anbieter (Reihenfolge = Dropdown-Reihenfolge). */
enum class MammouthProvider(val key: String, val label: String, val prefixes: List<String>) {
    GPT("gpt", "GPT", listOf("gpt-")),
    CLAUDE("claude", "Claude", listOf("claude-")),
    MISTRAL("mistral", "Mistral", listOf("mistral-")),
    GEMINI("gemini", "Gemini", listOf("gemini-")),
    KIMI("kimi", "Kimi", listOf("kimi-")),
    QWEN("qwen", "Qwen", listOf("qwen"));

    // Präfix-Treffer für Katalog-IDs; der bloße key deckt zusätzlich alte Provider-Schlüssel ab
    // (Migration eines gespeicherten „mistral"/„gpt" o. Ä.).
    fun matches(id: String): Boolean = id == key || prefixes.any { id.startsWith(it) }

    companion object {
        val DEFAULT = GPT
        fun fromKey(key: String?): MammouthProvider = entries.firstOrNull { it.key == key } ?: DEFAULT
        /** Anbieter aus einer konkreten Modell-ID ableiten (für die Rück-Auswahl im Dropdown). */
        fun ofModel(id: String): MammouthProvider? = entries.firstOrNull { it.matches(id) }
    }
}

object ModelCatalog {

    /**
     * Substrings, die ein Modell für ScatterTo ausschließen (Nicht-Text, Code, Preview, Reasoning,
     * Mammouth-Sonderfall). Bewusst breit — kurze, sachliche Textposts brauchen nichts davon.
     * Klein-/Groß egal (Vergleich auf lowercase).
     */
    private val EXCLUDE = listOf(
        // Nicht-Text / Sonderfälle
        "image", "embedding", "customtools", "recommended",
        "tts", "audio", "realtime", "transcribe", "whisper", "moderation", "dall",
        // Code
        "codex", "coder", "codestral", "devstral", "-code",
        // Preview
        "preview",
        // Reasoning
        "thinking", "reasoning", "deep-research", "qwq", "magistral", "-r1",
    )

    /** OpenAI-Reasoning heißt o1/o3/o4… (ohne „gpt"-Präfix) — für den Direkt-OpenAI-Filter. */
    private val OPENAI_REASONING = Regex("""^o\d""")

    /** true, wenn das Modell ein für Textposts brauchbares Chat-Modell ist. */
    fun isTextModel(id: String): Boolean {
        val lower = id.lowercase()
        if (EXCLUDE.any { lower.contains(it) }) return false
        if (OPENAI_REASONING.containsMatchIn(lower)) return false
        return true
    }

    /** Mammouth: Text-Modelle des gewählten Anbieters, neueste zuerst. */
    fun mammouthModels(provider: MammouthProvider, allIds: List<String>): List<String> =
        allIds.filter { provider.matches(it) && isTextModel(it) }.sortedWith(NEWEST_FIRST)

    /** Direkt-Dienst: relevante Text-Modelle des Anbieters, neueste zuerst. */
    fun directModels(service: AiService, allIds: List<String>): List<String> {
        val prefix = when (service) {
            AiService.CLAUDE -> "claude"
            AiService.OPENAI -> "gpt"
            AiService.GEMINI -> "gemini"
            AiService.MAMMOUTH -> return emptyList()
        }
        return allIds.filter { it.startsWith(prefix) && isTextModel(it) }.sortedWith(NEWEST_FIRST)
    }

    /** Neueste zuerst: höhere Versionszahlen gewinnen; bei Gleichstand die kürzere (schlichtere) ID. */
    val NEWEST_FIRST: Comparator<String> = Comparator { a, b ->
        val va = versionKey(a)
        val vb = versionKey(b)
        for (i in 0 until maxOf(va.size, vb.size)) {
            val x = va.getOrElse(i) { 0 }
            val y = vb.getOrElse(i) { 0 }
            if (x != y) return@Comparator y - x // absteigend
        }
        a.length - b.length // kürzere ID zuerst
    }

    private fun versionKey(id: String): List<Int> =
        Regex("""\d+""").findAll(id).map { it.value.toInt() }.toList()
}
