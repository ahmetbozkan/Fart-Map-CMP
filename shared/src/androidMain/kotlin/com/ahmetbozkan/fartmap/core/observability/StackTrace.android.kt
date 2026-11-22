package com.ahmetbozkan.fartmap.core.observability

internal actual fun copyStackTrace(from: Throwable, to: Throwable) {
    to.stackTrace = from.stackTrace
}
