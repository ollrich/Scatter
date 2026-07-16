package app.scatterto.data.mammouth

import app.scatterto.data.model.AiService

/**
 * Anbieter-Familien für die Mammouth-Auswahl und die Filter-/Sortierlogik, die aus der rohen
 * Modell-Liste (Mammouth `/v1/models` bzw. die APIs der Direkt-Dienste) die für kurze Textposts
 * sinnvollen Modelle macht. Reine Funktionen → unit-getestet (§12.3 Nr. 5).
 *
 * Prinzip: **Ausschlussliste** — was nicht explizit ausgeschlossen ist, erscheint. Dadurch tauchen
 * neue Modelle und sogar neue Stufen automatisch auf, ohne App-Update.
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
     * Basis-Ausschlüsse für ALLE Anbieter: Nicht-Text, Spezialzwecke, Code, Reasoning, Preview
     * (Nutzer-Entscheidung: nur stabile Modelle) und Legacy. Bewusst NICHT enthalten: „instruct"
     * (bei Mistral heißt das instruction-tuned und ist genau das, was wir wollen).
     */
    private val EXCLUDE = listOf(
        // Nicht-Text
        "image", "embedding", "tts", "audio", "realtime", "transcribe", "whisper",
        "moderation", "dall", "sora", "imagen", "veo", "lyria",
        // Spezialzwecke
        "customtools", "recommended", "robotics", "computer-use", "search",
        // Code
        "codex", "coder", "codestral", "devstral", "-code",
        // Reasoning
        "thinking", "reasoning", "deep-research", "qwq", "magistral", "-r1",
        // Preview
        "preview",
        // Legacy
        "gpt-3.5", "babbage", "davinci",
    )

    /** OpenAI-Reasoning heißt o1/o3/o4… (ohne „gpt"-Präfix). */
    private val OPENAI_REASONING = Regex("""^o\d""")

    /**
     * Nur bei **OpenAI direkt**: durch die 5.6-Stufen (Sol/Terra/Luna) abgelöst bzw. unerwünscht.
     * Bei Mammouth bleiben mini/nano/chat wählbar, und Geminis „pro" ist eine echte Stufe.
     */
    private val OPENAI_EXCLUDE = listOf("-pro", "-mini", "-nano", "-chat")

    /** true, wenn das Modell ein für Textposts brauchbares Chat-Modell ist. */
    fun isTextModel(id: String): Boolean {
        val lower = id.lowercase()
        if (EXCLUDE.any { lower.contains(it) }) return false
        return !OPENAI_REASONING.containsMatchIn(lower)
    }

    /** Mammouth: Text-Modelle des Anbieters, neueste zuerst (alle Stufen, inkl. mini/nano/chat). */
    fun mammouthModels(provider: MammouthProvider, allIds: List<String>): List<String> =
        allIds.filter { provider.matches(it) && isTextModel(it) }.sortedWith(NEWEST_FIRST)

    /**
     * Direkt-Dienste: **je Stufe die letzten zwei**, nach Stufen gruppiert. Datums-Snapshots fallen
     * weg, wenn der unversionierte Alias existiert; `-latest`-Aliase gelten als neueste.
     */
    fun directModels(service: AiService, allIds: List<String>): List<String> {
        val prefix = when (service) {
            AiService.CLAUDE -> "claude"
            AiService.OPENAI -> "gpt"
            AiService.GEMINI -> "gemini"
            AiService.MAMMOUTH -> return emptyList()
        }
        val extra = if (service == AiService.OPENAI) OPENAI_EXCLUDE else emptyList()
        val candidates = allIds.filter { id ->
            val lower = id.lowercase()
            id.startsWith(prefix) && isTextModel(id) && extra.none { lower.contains(it) }
        }
        return collapseDated(candidates)
            .groupBy { tierOf(service, it) }
            .values
            .map { it.sortedWith(NEWEST_FIRST).take(2) }
            .sortedWith(compareBy(NEWEST_FIRST) { it.first() })
            .flatten()
    }

    /** `X-YYYY-MM-DD` bzw. `X-NNNNNNNN` fliegt raus, wenn der Alias `X` existiert (sonst bleibt er). */
    private val DATED = Regex("""^(.*)-(\d{4}-\d{2}-\d{2}|\d{8})$""")

    private fun collapseDated(ids: List<String>): List<String> {
        val all = ids.toSet()
        return ids.filterNot { id -> DATED.find(id)?.groupValues?.get(1)?.let { it in all } == true }
    }

    private val CLAUDE_FAMILY = Regex("""^claude-([a-z]+)""")
    private val OPENAI_TIER = Regex("""^gpt-[\d.]+-(.+)$""")

    /** Stufe/Familie je Dienst. Unbekanntes landet in der Basis-Gruppe, erscheint also trotzdem. */
    private fun tierOf(service: AiService, id: String): String = when (service) {
        AiService.CLAUDE -> CLAUDE_FAMILY.find(id)?.groupValues?.get(1).orEmpty()
        AiService.OPENAI -> OPENAI_TIER.find(id)?.groupValues?.get(1).orEmpty()
        // „flash-lite" muss VOR „flash" greifen, sonst landen die Lite-Modelle in der Flash-Gruppe.
        AiService.GEMINI -> when {
            id.contains("flash-lite") -> "flash-lite"
            id.contains("flash") -> "flash"
            id.contains("pro") -> "pro"
            else -> ""
        }
        AiService.MAMMOUTH -> ""
    }

    /**
     * Neueste zuerst: `-latest`-Aliase zeigen per Definition auf die neueste Version und ranken
     * daher vorn (sie haben keine Versionsnummer und fielen sonst hinten raus). Sonst gewinnen
     * höhere Versionszahlen; bei Gleichstand die kürzere (schlichtere) ID.
     */
    val NEWEST_FIRST: Comparator<String> = Comparator { a, b ->
        val aLatest = a.endsWith("-latest")
        val bLatest = b.endsWith("-latest")
        if (aLatest != bLatest) return@Comparator if (aLatest) -1 else 1
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
