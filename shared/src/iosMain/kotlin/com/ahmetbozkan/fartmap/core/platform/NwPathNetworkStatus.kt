package com.ahmetbozkan.fartmap.core.platform

import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue

/**
 * [NetworkStatus] on one app-lifetime `NWPathMonitor` (a Koin single). The handler runs on the main queue and stores
 * the latest path status; until the first update the status is unknown, which counts as online.
 */
internal class NwPathNetworkStatus : NetworkStatus {

    // Written on the main queue only; read from the main thread (ViewModel scope) in practice.
    private var online = true

    private val monitor = nw_path_monitor_create().also { monitor ->
        nw_path_monitor_set_update_handler(monitor) { path ->
            online = nw_path_get_status(path) == nw_path_status_satisfied
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
    }

    override fun isOnline(): Boolean = online
}
