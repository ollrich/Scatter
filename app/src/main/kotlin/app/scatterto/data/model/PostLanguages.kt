package app.scatterto.data.model

import java.util.Locale

/**
 * Kuratierte Liste wählbarer Post-Sprachen (BCP-47). Verbreitete Sprachen, die die KI-Modelle gut
 * beherrschen; bewusst NICHT alle ~180 ISO-Sprachen. Anzeigenamen kommen lokalisiert aus [Locale],
 * müssen also nicht von Hand übersetzt werden. Reine Logik → testbar.
 */
object PostLanguages {

    /** BCP-47-Tags in Anzeige-Reihenfolge. */
    val TAGS: List<String> = listOf(
        "en", "de", "da", "nl", "sv", "no", "fi", "fr", "es", "pt", "it", "pl",
        "cs", "sk", "ro", "hu", "el", "tr", "uk", "ru", "ar", "hi", "ja", "ko", "zh",
    )

    /** Anzeigename in der übergebenen Anzeigesprache (Default: aktuelle Locale). */
    fun displayName(tag: String, inLocale: Locale = Locale.getDefault()): String =
        Locale.forLanguageTag(tag).getDisplayLanguage(inLocale)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(inLocale) else it.toString() }

    /** Englischer Name für die Prompt-Anweisung an die KI (z. B. „German", „Danish"). */
    fun englishName(tag: String): String =
        Locale.forLanguageTag(tag).getDisplayLanguage(Locale.ENGLISH)

    /** Primäres Sprach-Subtag (z. B. „de" aus „de-DE"); für Vergleiche/Fallback. */
    fun primary(tag: String): String = tag.substringBefore('-').lowercase()

    /** [tag] in der kuratierten Liste? Sonst Fallback „en". */
    fun normalizedOrEnglish(tag: String): String {
        val p = primary(tag)
        return if (p in TAGS) p else "en"
    }
}
