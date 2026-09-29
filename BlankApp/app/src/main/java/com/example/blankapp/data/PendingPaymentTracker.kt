package com.example.blankapp.data

/**
 * Tracks an in-flight PayFast checkout so the app can show the Payment
 * Successful screen when the parent returns from the browser.
 *
 * PayFast redirects to a web return URL, not back into the app, so there is
 * no deep link to hook. Instead:
 *  1. Before opening the browser, the finance screens record the checkout here.
 *  2. When the app's ON_RESUME fires with a recent pending checkout, the
 *     Payment Successful screen takes over.
 *  3. That screen polls the invoice statuses until the ITN webhook
 *     (the source of truth) flips them to PAID.
 *
 * Kept in a simple object (not SavedStateHandle) deliberately: the data is
 * only meaningful across the seconds the browser is in the foreground, and a
 * stale entry older than the resume-guard threshold is ignored.
 */
object PendingPaymentTracker {

    data class PendingCheckout(
        val invoiceIds: List<String>,
        val batchId: String?,
        val total: Double,
        val startedAtMillis: Long = System.currentTimeMillis()
    )

    @Volatile
    var pending: PendingCheckout? = null

    /**
     * Returns the pending checkout if one is fresh enough to be a genuine
     * return-from-browser (not an immediate resume because the browser never
     * opened). [minAgeMillis] guards against double-triggers.
     */
    fun consumeIfFresh(minAgeMillis: Long = 4_000): PendingCheckout? {
        val p = pending ?: return null
        val age = System.currentTimeMillis() - p.startedAtMillis
        return if (age >= minAgeMillis) p else null
    }

    fun clear() {
        pending = null
    }
}
