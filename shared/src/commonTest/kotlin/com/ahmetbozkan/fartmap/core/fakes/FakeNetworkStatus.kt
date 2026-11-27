package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.platform.NetworkStatus

class FakeNetworkStatus(var online: Boolean = true) : NetworkStatus {
    override fun isOnline(): Boolean = online
}
