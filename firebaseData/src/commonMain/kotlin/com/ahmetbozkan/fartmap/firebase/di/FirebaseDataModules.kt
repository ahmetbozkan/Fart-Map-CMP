package com.ahmetbozkan.fartmap.firebase.di

import com.ahmetbozkan.fartmap.core.data.write.WriteErrorMapper
import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticRepository
import com.ahmetbozkan.fartmap.core.domain.fart.FartRepository
import com.ahmetbozkan.fartmap.core.domain.profile.ProfileRepository
import com.ahmetbozkan.fartmap.core.domain.session.AccountRepository
import com.ahmetbozkan.fartmap.core.domain.session.AuthGateway
import com.ahmetbozkan.fartmap.core.domain.session.LocalDataReset
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.observability.LogSink
import com.ahmetbozkan.fartmap.feature.map.domain.MapFartRepository
import com.ahmetbozkan.fartmap.firebase.cosmetics.FirestoreCosmeticRepository
import com.ahmetbozkan.fartmap.firebase.fart.FirestoreFartRepository
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreLocalDataReset
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreProvider
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreWriteErrorMapper
import com.ahmetbozkan.fartmap.firebase.map.FirestoreMapFartRepository
import com.ahmetbozkan.fartmap.firebase.profile.FirestoreProfileRepository
import com.ahmetbozkan.fartmap.firebase.session.FirebaseAuthGateway
import com.ahmetbozkan.fartmap.firebase.session.FirestoreAccountRepository
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
fun firebaseDataModules(): List<Module> = listOf(telemetryModule, firestoreModule, sessionModule)

private val sessionModule = module {
    single<AuthGateway> { FirebaseAuthGateway(get()) }
    single<AccountRepository> { FirestoreAccountRepository(get(), get(), get()) }
    single<ProfileRepository> { FirestoreProfileRepository(get(), get(), get()) }
}

private val telemetryModule = module {
    single(named("crashlytics")) { CrashlyticsLogSink(Firebase.crashlytics) } bind LogSink::class
    single<AnalyticsTracker> { FirebaseAnalyticsTracker(Firebase.analytics, get()) }
}

private val firestoreModule = module {
    single { FirestoreProvider() }
    single<WriteErrorMapper> { FirestoreWriteErrorMapper }
    single<LocalDataReset> { FirestoreLocalDataReset(get(), get()) }
    single<MapFartRepository> { FirestoreMapFartRepository(get(), get()) }
    single<FartRepository> { FirestoreFartRepository(get(), get()) }
    single<CosmeticRepository> { FirestoreCosmeticRepository(get(), get()) }
}
