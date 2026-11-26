package com.ahmetbozkan.fartmap.core.di

import com.ahmetbozkan.fartmap.core.data.preferences.DataStorePreferencesRepository
import com.ahmetbozkan.fartmap.core.platform.CoreLocationProvider
import com.ahmetbozkan.fartmap.core.platform.LocationProvider
import com.ahmetbozkan.fartmap.core.platform.MapHost
import com.ahmetbozkan.fartmap.core.platform.MapKitMapHost
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

// Application Support: not user-visible and, unlike Documents, not exposed through file sharing.
internal actual val platformModule = module {
    single { DataStorePreferencesRepository.createStore(appSupportPath(DataStorePreferencesRepository.FILE_NAME)) }
    single<MapHost> { MapKitMapHost(logger = get()) }
    single<LocationProvider> { CoreLocationProvider(logger = get()) }
}

@OptIn(ExperimentalForeignApi::class)
private fun appSupportPath(fileName: String): String {
    val appSupport: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val dir = requireNotNull(appSupport?.path) { "iOS Application Support directory unavailable" }
    return "$dir/$fileName"
}
