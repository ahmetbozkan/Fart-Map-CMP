package com.ahmetbozkan.fartmap

import com.ahmetbozkan.fartmap.core.di.sharedModules
import com.ahmetbozkan.fartmap.core.platform.AppEnvironment
import com.ahmetbozkan.fartmap.firebase.di.firebaseDataModules
import com.ahmetbozkan.fartmap.firebase.telemetry.FirebaseTelemetry
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools

/**
 * iOS composition root (ADR-0003, ADR-0021). The Swift `AppDelegate` calls this right after
 * `FirebaseApp.configure()`, before any view exists: telemetry switch first, then Koin, once per process.
 * [environmentName] is APP_ENV from the active xcconfig (dev | prod).
 */
fun startApp(environmentName: String) {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    val environment = AppEnvironment.parse(environmentName)
    FirebaseTelemetry.configure(environment)
    startKoin { modules(sharedModules(environment) + firebaseDataModules()) }
}
