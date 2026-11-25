package com.ahmetbozkan.fartmap.core.di

import android.app.Application
import com.ahmetbozkan.fartmap.core.data.preferences.DataStorePreferencesRepository
import com.ahmetbozkan.fartmap.core.platform.AndroidGoogleIdTokenProvider
import com.ahmetbozkan.fartmap.core.platform.CurrentActivityTracker
import com.ahmetbozkan.fartmap.core.platform.GoogleIdTokenProvider
import com.ahmetbozkan.fartmap.core.platform.GoogleMapsMapHost
import com.ahmetbozkan.fartmap.core.platform.MapHost
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal actual val platformModule = module {
    single {
        val file = androidContext().filesDir.resolve(DataStorePreferencesRepository.FILE_NAME)
        DataStorePreferencesRepository.createStore(file.absolutePath)
    }
    // Eager: it must be registered before the first Activity resumes, or the sheet has no Activity to attach to.
    single(createdAtStart = true) { CurrentActivityTracker(androidContext() as Application) }
    single<GoogleIdTokenProvider> {
        AndroidGoogleIdTokenProvider(context = androidContext(), activities = get(), logger = get())
    }
    single<MapHost> { GoogleMapsMapHost() }
}
