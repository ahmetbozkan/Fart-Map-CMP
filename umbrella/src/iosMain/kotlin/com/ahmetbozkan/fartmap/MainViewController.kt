package com.ahmetbozkan.fartmap

import androidx.compose.ui.window.ComposeUIViewController
import com.ahmetbozkan.fartmap.core.platform.AppEnvironment
import com.ahmetbozkan.fartmap.firebase.FirebaseProjectInfo

/**
 * Entry point for the SwiftUI shell. [environmentName] is the APP_ENV value from the active xcconfig (dev | prod).
 */
fun MainViewController(environmentName: String) = ComposeUIViewController {
    App(
        environment = AppEnvironment.parse(environmentName),
        firebaseProjectId = FirebaseProjectInfo.projectId(),
    )
}
