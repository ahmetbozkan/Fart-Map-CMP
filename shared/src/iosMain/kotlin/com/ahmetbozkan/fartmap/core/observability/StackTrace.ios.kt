package com.ahmetbozkan.fartmap.core.observability

// Kotlin/Native has no settable stack trace; Crashlytics on iOS still gets the sanitized type and message.
internal actual fun copyStackTrace(from: Throwable, to: Throwable) = Unit
