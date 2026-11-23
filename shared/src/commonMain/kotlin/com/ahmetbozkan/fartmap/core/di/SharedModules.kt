package com.ahmetbozkan.fartmap.core.di

import com.ahmetbozkan.fartmap.core.data.preferences.DataStorePreferencesRepository
import com.ahmetbozkan.fartmap.core.data.profile.ProfileBootstrap
import com.ahmetbozkan.fartmap.core.data.session.DefaultSessionRepository
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
import com.ahmetbozkan.fartmap.feature.onboarding.presentation.AgeGateViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Everything `shared` binds (ADR-0003), including its [platformModule]. Startup composes
 * `sharedModules(env) + firebaseDataModules()`: Android in `FartMapApplication`, iOS in umbrella `startApp`.
 * The [Logger] collects every [LogSink] bound by any module (console here, Crashlytics in `firebaseData`).
 * `firebaseData` supplies the Firebase-backed contracts used here (`AuthGateway`, `AccountRepository`,
 * `ProfileRepository`, `WriteErrorMapper`, `LocalDataReset`).
 */
fun sharedModules(environment: AppEnvironment): List<Module> =
    listOf(coreModule(environment), dataModule, featureModule, platformModule)

private fun coreModule(environment: AppEnvironment) = module {
    single { environment }
    single(named("console")) {
        val dev = environment == AppEnvironment.DEV
        ConsoleLogSink(minSeverity = if (dev) Severity.DEBUG else Severity.WARN, showThrowableMessage = dev)
    } bind LogSink::class
    single { Logger(sinks = getAll<LogSink>()) }
    single<RemoteConfig> { DefaultsRemoteConfig() }
}

private val dataModule = module {
    single<SessionRepository> {
        DefaultSessionRepository(
            auth = get(),
            accounts = get(),
            preferences = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            currentYear = ::currentYear,
        )
    }
    // Eager: starts watching the session at app start (needs ProfileRepository from firebaseData; startKoin builds
    // eager singles after every module is loaded).
    single(createdAtStart = true) {
        ProfileBootstrap(session = get(), profiles = get(), logger = get()).also {
            it.start(CoroutineScope(SupervisorJob() + Dispatchers.Default))
        }
    }
    single<PreferencesRepository> { DataStorePreferencesRepository(store = get(), logger = get()) }
    single { DefaultAppWriteScope(logger = get(), errorMapper = get()) } binds
        arrayOf(AppWriteScope::class, PendingWriteMonitor::class)
    factoryOf(::SignOut)
}

private val featureModule = module {
    viewModelOf(::RootViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::AgeGateViewModel)
}

@OptIn(ExperimentalTime::class)
private fun currentYear(): Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
