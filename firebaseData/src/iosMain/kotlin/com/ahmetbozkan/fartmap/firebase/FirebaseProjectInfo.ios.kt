package com.ahmetbozkan.fartmap.firebase

import cocoapods.FirebaseCore.FIRApp
import kotlinx.cinterop.ExperimentalForeignApi

// FirebaseCore binding shipped with GitLive's firebase-app klib; the Swift shell has already called FirebaseApp.configure().
@OptIn(ExperimentalForeignApi::class)
internal actual fun defaultAppProjectId(): String? = FIRApp.defaultApp()?.options?.projectID
