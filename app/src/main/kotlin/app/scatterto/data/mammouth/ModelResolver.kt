package app.scatterto.data.mammouth

/**
 * Löst eine Anbieter-Auswahl auf die aktuell beste (Flaggschiff-)Modell-ID auf (§4.1).
 * Mammouth liefert in `/v1/models` unbrauchbare Metadaten (`owned_by` immer "openai", `created`
 * konstant), daher wird ausschließlich die ID ausgewertet: Präfix = Anbieter, Zahl = Version.
 * Reine Funktion → testbar. Fällt die Live-Liste aus, greift die statische Flaggschiff-Tabelle.
 */
object ModelResolver {

    const val RECOMMENDED_ID = "mammouth-recommended"
    const val DEFAULT_PROVIDER = "mistral"

    // Offline-Fallback (Stand des Katalogs 2026-07). Auto-Update passiert über die Live-Liste.
    val FALLBACK: Map<String, String> = mapOf(
        "mistral" to "mistral-large-3",
        "claude" to "claude-opus-4-8",
        "gpt" to "gpt-5.5",
        "gemini" to "gemini-2.5-pro",
    )

    private data class Rule(val prefix: String, val require: String?, val exclude: List<String>)

    private val RULES: Map<String, Rule> = mapOf(
        // Flaggschiff-Stufe je Anbieter; Gemini bewusst ohne Preview (stabil, Nutzer-Entscheidung).
        "claude" to Rule("claude-", require = "opus", exclude = listOf("image")),
        "mistral" to Rule("mistral-", require = "large", exclude = emptyList()),
        "gemini" to Rule("gemini-", require = "pro", exclude = listOf("image", "preview", "customtools", "flash", "lite")),
        "gpt" to Rule("gpt-", require = null, exclude = listOf("codex", "image", "embedding", "mini", "nano", "audio", "realtime", "search", "tts", "transcribe")),
    )

    fun resolve(provider: String, available: List<String>): String {
        val fallback = FALLBACK[provider] ?: RECOMMENDED_ID
        val rule = RULES[provider] ?: return fallback

        val candidates = available.filter { id ->
            id.startsWith(rule.prefix) &&
                (rule.require == null || id.contains(rule.require)) &&
                rule.exclude.none { id.contains(it) }
        }
        return candidates.maxWithOrNull(::compareModel) ?: fallback
    }

    /** Höhere Version gewinnt; bei Gleichstand die kürzere ID (plain vor „-chat"/„-suffix"). */
    private fun compareModel(a: String, b: String): Int {
        val va = versionKey(a)
        val vb = versionKey(b)
        for (i in 0 until maxOf(va.size, vb.size)) {
            val x = va.getOrElse(i) { 0 }
            val y = vb.getOrElse(i) { 0 }
            if (x != y) return x - y
        }
        return b.length - a.length
    }

    private fun versionKey(id: String): List<Int> =
        Regex("""\d+""").findAll(id).map { it.value.toInt() }.toList()
}
