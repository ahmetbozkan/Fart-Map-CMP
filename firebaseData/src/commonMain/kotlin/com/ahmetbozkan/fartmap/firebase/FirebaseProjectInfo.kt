package com.ahmetbozkan.fartmap.firebase

/**
 * Identity of the Firebase project the running app is wired to (bootstrap diagnostic shown on the placeholder screen).
 *
 * Platform actuals read the default app's options directly. The GitLive accessor `Firebase.app.options` is not used
 * because its Apple implementation force-unwraps `databaseURL`, which projects without Realtime Database never set
 * (firebase-kotlin-sdk 2.7.0, `firebase-app/src/appleMain/.../firebase.kt`), so it throws on iOS.
 */
object FirebaseProjectInfo {
    fun projectId(): String = defaultAppProjectId()
        ?: error("Firebase default app has no project id; check the injected config for this environment")
}

internal expect fun defaultAppProjectId(): String?
