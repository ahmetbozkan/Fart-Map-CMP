package com.ahmetbozkan.fartmap.core.di

import com.ahmetbozkan.fartmap.core.data.session.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.navigation.RootViewModel
import com.ahmetbozkan.fartmap.core.observability.ConsoleLogSink
import com.ahmetbozkan.fartmap.core.observability.LogSink
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.core.observability.Severity
import com.ahmetbozkan.fartmap.core.platform.AppEnvironment
import com.ahmetbozkan.fartmap.feature.auth.presentation.AuthViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Everything `shared` binds (ADR-0003). Startup composes `sharedModules(env) + firebaseDataModules(env)` plus the
 * platform modules once native capabilities exist: Android in `FartMapApplication`, iOS in `umbrella`.
 * The [Logger] collects every [LogSink] bound by any module (console here, Crashlytics in `firebaseData`).
 */
fun sharedModules(environment: AppEnvironment): List<Module> = listOf(coreModule(environment), featureModule)

private fun coreModule(environment: AppEnvironment) = module {
    single { environment }
    single(named("console")) {
        ConsoleLogSink(minSeverity = if (environment == AppEnvironment.DEV) Severity.DEBUG else Severity.WARN)
    } bind LogSink::class
    single { Logger(sinks = getAll<LogSink>()) }
    // Stub until EPIC-01-02 binds the Firebase Auth implementation from firebaseData.
    single<SessionRepository> { InMemorySessionRepository() }
}

private val featureModule = module {
    viewModelOf(::RootViewModel)
    viewModelOf(::AuthViewModel)
}
