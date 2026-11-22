package com.ahmetbozkan.fartmap

import android.app.Application
import com.ahmetbozkan.fartmap.core.di.sharedModules
import com.ahmetbozkan.fartmap.core.platform.AppEnvironment
import com.ahmetbozkan.fartmap.firebase.di.firebaseDataModules
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreSetup
import com.ahmetbozkan.fartmap.firebase.telemetry.FirebaseTelemetry
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/** Android composition root (ADR-0003): environment from the flavor, telemetry switch, then Koin. */
class FartMapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val environment = AppEnvironment.parse(BuildConfig.APP_ENV)
        FirebaseTelemetry.configure(environment)
        FirestoreSetup.configure()
        startKoin {
            androidContext(this@FartMapApplication)
            modules(sharedModules(environment) + firebaseDataModules())
        }
    }
}
