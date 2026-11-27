package com.ahmetbozkan.fartmap.core.platform

/**
 * Best-effort connectivity (ConnectivityManager on Android, NWPathMonitor on iOS). Writes never depend on it: Firestore
 * queues them offline (ADR-0022). It only picks the copy after a hand-off ("saved locally") and the `offline` analytics
 * param. A captive or unvalidated network counts as online, and so does "unknown".
 */
interface NetworkStatus {
    fun isOnline(): Boolean
}
