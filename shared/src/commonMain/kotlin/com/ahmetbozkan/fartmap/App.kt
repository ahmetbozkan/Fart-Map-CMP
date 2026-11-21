package com.ahmetbozkan.fartmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ahmetbozkan.fartmap.core.platform.AppEnvironment

/**
 * Bootstrap placeholder (STORY-01-01-01). Shows the environment and Firebase project the shell wired in.
 * Replaced by the navigation shell in STORY-01-01-02.
 */
@Composable
fun App(environment: AppEnvironment, firebaseProjectId: String, modifier: Modifier = Modifier) {
    MaterialTheme {
        Column(
            modifier = modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "Fart Map", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Environment: ${environment.wireName}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = "Firebase project: $firebaseProjectId",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
