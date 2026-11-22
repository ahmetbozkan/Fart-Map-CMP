package com.ahmetbozkan.fartmap.feature.profile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ahmetbozkan.fartmap.core.design.FartMapTheme
import com.ahmetbozkan.fartmap.core.design.components.EmptyState
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.placeholder_profile_body
import com.ahmetbozkan.fartmap.resources.placeholder_profile_title
import org.jetbrains.compose.resources.stringResource

/** Profile tab placeholder (STORY-01-01-02); the real screen arrives with its epic (planning/ROADMAP.md). */
@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    EmptyState(
        title = stringResource(Res.string.placeholder_profile_title),
        body = stringResource(Res.string.placeholder_profile_body),
        modifier = modifier,
    )
}

@Preview
@Composable
private fun ProfileScreenPreview() = FartMapTheme { ProfileScreen() }
