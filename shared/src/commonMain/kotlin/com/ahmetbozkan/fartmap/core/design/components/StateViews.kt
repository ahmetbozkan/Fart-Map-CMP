package com.ahmetbozkan.fartmap.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.ahmetbozkan.fartmap.core.design.FartMapTheme
import com.ahmetbozkan.fartmap.core.design.PillShape
import com.ahmetbozkan.fartmap.core.design.Spacing
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.error_auth_expired
import com.ahmetbozkan.fartmap.resources.error_conflict
import com.ahmetbozkan.fartmap.resources.error_network
import com.ahmetbozkan.fartmap.resources.error_permission
import com.ahmetbozkan.fartmap.resources.error_timeout
import com.ahmetbozkan.fartmap.resources.error_unknown
import com.ahmetbozkan.fartmap.resources.error_validation
import com.ahmetbozkan.fartmap.resources.state_loading
import com.ahmetbozkan.fartmap.resources.state_retry
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** User-facing copy for a typed error, in the product voice. */
fun AppError.messageResource(): StringResource = when (this) {
    AppError.NetworkUnavailable -> Res.string.error_network
    AppError.Timeout -> Res.string.error_timeout
    AppError.PermissionDenied -> Res.string.error_permission
    is AppError.ValidationFailed -> Res.string.error_validation
    AppError.Conflict -> Res.string.error_conflict
    AppError.AuthExpired -> Res.string.error_auth_expired
    AppError.Unknown -> Res.string.error_unknown
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    val description = stringResource(Res.string.state_loading)
    CenteredColumn(modifier) {
        CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = description })
    }
}

@Composable
fun ErrorState(error: AppError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    CenteredColumn(modifier) {
        Text(
            text = stringResource(error.messageResource()),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry, shape = PillShape, modifier = Modifier.padding(top = Spacing.lg)) {
            Text(stringResource(Res.string.state_retry))
        }
    }
}

/** Empty or not-yet-built content. [title] and [body] are humorous copy from string resources. */
@Composable
fun EmptyState(title: String, modifier: Modifier = Modifier, body: String? = null) {
    CenteredColumn(modifier) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        if (body != null) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
    }
}

@Composable
private fun CenteredColumn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = Spacing.screenEdge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) { content() }
}

@Preview
@Composable
private fun LoadingStatePreview() = FartMapTheme { LoadingState() }

@Preview
@Composable
private fun ErrorStatePreview() = FartMapTheme { ErrorState(AppError.NetworkUnavailable, onRetry = {}) }

@Preview
@Composable
private fun EmptyStatePreview() = FartMapTheme {
    EmptyState(
        "Nothing here yet",
        body = "Be the first to break the silence."
    )
}
