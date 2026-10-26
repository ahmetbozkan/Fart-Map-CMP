package com.ahmetbozkan.fartmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ahmetbozkan.fartmap.core.platform.AppEnvironment
import com.ahmetbozkan.fartmap.firebase.FirebaseProjectInfo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // The shell owns environment wiring; shared code receives it as a value (Infrastructure layer).
        val environment = AppEnvironment.parse(BuildConfig.APP_ENV)
        setContent {
            App(environment = environment, firebaseProjectId = FirebaseProjectInfo.projectId())
        }
    }
}
