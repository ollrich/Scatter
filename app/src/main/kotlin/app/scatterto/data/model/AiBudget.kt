package app.scatterto.data.model

/**
 * Guthabenstand eines KI-Dienstes (§4.1) — **nur Mammouth**. Die anderen drei Anbieter haben
 * keinen Endpoint dafür: OpenAI bietet gar keinen, Anthropic nur mit einem Admin-Key (der weit
 * mehr darf, als eine Client-App haben sollte), Google nur über die Cloud Billing API mit OAuth.
 *
 * [spent] und [max] sind USD der laufenden Abrechnungsperiode.
 */
data class AiBudget(
    val spent: Double,
    val max: Double,
    /** ISO-Zeitpunkt, an dem die Periode neu beginnt; null, wenn der Dienst keinen nennt. */
    val resetAt: String? = null,
) {
    /** Anteil 0..1 für den Fortschrittsbalken; über 100 % gehende Werte werden gekappt. */
    val fraction: Float get() = if (max <= 0) 0f else (spent / max).coerceIn(0.0, 1.0).toFloat()

    /** Ab hier wird der Balken als Warnung eingefärbt. */
    val isLow: Boolean get() = fraction >= 0.9f
}
