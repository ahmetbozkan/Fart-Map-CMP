package com.ahmetbozkan.fartmap.feature.social.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ahmetbozkan.fartmap.core.design.FartMapTheme
import com.ahmetbozkan.fartmap.core.design.components.EmptyState
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.placeholder_social_body
import com.ahmetbozkan.fartmap.resources.placeholder_social_title
import org.jetbrains.compose.resources.stringResource

/** Social tab placeholder (STORY-01-01-02); the real screen arrives with its epic (planning/ROADMAP.md). */
@Composable
fun SocialScreen(modifier: Modifier = Modifier) {
    EmptyState(
        title = stringResource(Res.string.placeholder_social_title),
        body = stringResource(Res.string.placeholder_social_body),
        modifier = modifier,
    )
}

@Preview
@Composable
private fun SocialScreenPreview() = FartMapTheme { SocialScreen() }
