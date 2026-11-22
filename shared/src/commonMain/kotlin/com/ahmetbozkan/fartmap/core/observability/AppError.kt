package com.ahmetbozkan.fartmap.core.observability

/**
 * Typed failure categories shared by every layer (planning/architecture/Observability.md §4).
 * [category] is the stable wire name used in logs, Crashlytics keys and analytics parameters.
 */
sealed interface AppError {
    val category: String

    data object NetworkUnavailable : AppError {
        override val category = "network_unavailable"
    }

    data object Timeout : AppError {
        override val category = "timeout"
    }

    data object PermissionDenied : AppError {
        override val category = "permission_denied"
    }

    /** [field] names the rejected input (an identifier, never the user's value). */
    data class ValidationFailed(val field: String) : AppError {
        override val category = "validation_failed"
    }

    data object Conflict : AppError {
        override val category = "conflict"
    }

    data object AuthExpired : AppError {
        override val category = "auth_expired"
    }

    data object Unknown : AppError {
        override val category = "unknown"
    }
}
