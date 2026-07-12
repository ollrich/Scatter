package app.scatterto.ui

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList

/**
 * App-Sprache über die per-App-Locale (minSdk 34 nativ via [LocaleManager]). Das System persistiert
 * die Wahl und startet die Activity neu; nichts Eigenes zu speichern. `null` = Systemsprache.
 */
object AppLocale {

    /** Auswählbare Sprachen mit nativem Anzeigenamen; null-Tag = Systemsprache. */
    val options: List<Pair<String?, String>> = listOf(
        null to "system",
        "de" to "Deutsch",
        "en" to "English",
        "da" to "Dansk",
    )

    fun current(context: Context): String? {
        val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
        return if (locales.isEmpty) null else locales[0].language
    }

    fun set(context: Context, tag: String?) {
        val list = if (tag == null) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(tag)
        context.getSystemService(LocaleManager::class.java).applicationLocales = list
    }
}
