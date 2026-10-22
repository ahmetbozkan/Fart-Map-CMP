package com.ahmetbozkan.fartmap.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.app

internal actual fun defaultAppProjectId(): String? = Firebase.app.options.projectId
