package com.ahmetbozkan.fartmap

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ahmetbozkan.fartmap.core.design.FartMapTheme
import com.ahmetbozkan.fartmap.core.navigation.AppNavHost

/** Root composable for both shells. Koin must be started first (Android `FartMapApplication`, iOS `umbrella`). */
@Composable
fun App(modifier: Modifier = Modifier) {
    FartMapTheme {
        AppNavHost(modifier = modifier)
    }
}
