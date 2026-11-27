package com.ahmetbozkan.fartmap.core.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.ahmetbozkan.fartmap.core.observability.Logger

/**
 * [NetworkStatus] on `ConnectivityManager` (`ACCESS_NETWORK_STATE`, a normal permission). Online means the active
 * network claims internet; it isn't validated, so a captive portal reads as online (only copy depends on it).
 */
internal class AndroidNetworkStatus(
    private val context: Context,
    private val logger: Logger,
) : NetworkStatus {

    override fun isOnline(): Boolean {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return true
        return try {
            val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (denied: SecurityException) {
            logger.warn(
                feature = "network",
                operation = "is_online",
                event = "network_status_failed",
                attributes = mapOf("reason" to denied::class.simpleName.orEmpty()),
            )
            true
        }
    }
}
