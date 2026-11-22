package com.ahmetbozkan.fartmap.core.di

import com.ahmetbozkan.fartmap.core.data.preferences.DataStorePreferencesRepository
import com.ahmetbozkan.fartmap.core.data.session.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.data.write.DefaultAppWriteScope
import com.ahmetbozkan.fartmap.core.domain.preferences.PreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SignOut
import com.ahmetbozkan.fartmap.core.domain.write.AppWriteScope
import com.ahmetbozkan.fartmap.core.domain.write.PendingWriteMonitor
import com.ahmetbozkan.fartmap.core.navigation.RootViewModel
import com.ahmetbozkan.fartmap.core.observability.ConsoleLogSink
import com.ahmetbozkan.fartmap.core.observability.LogSink
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.core.observability.Severity
import com.ahmetbozkan.fartmap.core.platform.AppEnvironment
import com.ahmetbozkan.fartmap.core.platform.DefaultsRemoteConfig
import com.ahmetbozkan.fartmap.core.platform.RemoteConfig
import com.ahmetbozkan.fartmap.feature.auth.presentation.AuthViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

/**
 * Everything `shared` binds (ADR-0003), including its [platformModule]. Startup composes
 * `sharedModules(env) + firebaseDataModules()`: Android in `FartMapApplication`, iOS in umbrella `startApp`.
 * The [Logger] collects every [LogSink] bound by any module (console here, Crashlytics in `firebaseData`).
 * `firebaseData` supplies the Firebase-backed contracts used here (`WriteErrorMapper`, `LocalDataReset`).
 */
fun sharedModules(environment: AppEnvironment): List<Module> =
    listOf(coreModule(environment), dataModule, featureModule, platformModule)

private fun coreModule(environment: AppEnvironment) = module {
    single { environment }
    single(named("console")) {
        ConsoleLogSink(minSeverity = if (environment == AppEnvironment.DEV) Severity.DEBUG else Severity.WARN)
    } bind LogSink::class
    single { Logger(sinks = getAll<LogSink>()) }
    single<RemoteConfig> { DefaultsRemoteConfig() }
}

private val dataModule = module {
    // Stub until EPIC-01-02 binds the Firebase Auth implementation from firebaseData.
    single<SessionRepository> { InMemorySessionRepository() }
    single<PreferencesRepository> { DataStorePreferencesRepository(store = get(), logger = get()) }
    single { DefaultAppWriteScope(logger = get(), errorMapper = get()) } binds
        arrayOf(AppWriteScope::class, PendingWriteMonitor::class)
    factoryOf(::SignOut)
}

private val featureModule = module {
    viewModelOf(::RootViewModel)
    viewModelOf(::AuthViewModel)
}
