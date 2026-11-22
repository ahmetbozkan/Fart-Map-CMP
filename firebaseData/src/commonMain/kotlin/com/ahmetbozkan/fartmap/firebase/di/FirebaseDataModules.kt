package com.ahmetbozkan.fartmap.firebase.di

import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.observability.LogSink
import com.ahmetbozkan.fartmap.firebase.telemetry.CrashlyticsLogSink
import com.ahmetbozkan.fartmap.firebase.telemetry.FirebaseAnalyticsTracker
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.crashlytics.crashlytics
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/** Firebase implementations of the `shared` contracts (ADR-0021). Composed at startup next to `sharedModules`. */
fun firebaseDataModules(): List<Module> = listOf(telemetryModule)

private val telemetryModule = module {
    single(named("crashlytics")) { CrashlyticsLogSink(Firebase.crashlytics) } bind LogSink::class
    single<AnalyticsTracker> { FirebaseAnalyticsTracker(Firebase.analytics) }
}
