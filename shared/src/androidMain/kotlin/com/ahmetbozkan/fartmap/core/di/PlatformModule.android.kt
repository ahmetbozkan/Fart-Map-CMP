package com.ahmetbozkan.fartmap.core.di

import com.ahmetbozkan.fartmap.core.data.preferences.DataStorePreferencesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal actual val platformModule = module {
    single {
        val file = androidContext().filesDir.resolve(DataStorePreferencesRepository.FILE_NAME)
        DataStorePreferencesRepository.createStore(file.absolutePath)
    }
}
