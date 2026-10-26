package com.ahmetbozkan.fartmap.core.platform

/**
 * Which backend environment this build talks to. Supplied by the app shell
 * (Android: BuildConfig.APP_ENV per flavor; iOS: Info.plist APP_ENV per xcconfig) and never inferred in shared code.
 */
enum class AppEnvironment(val wireName: String) {
    DEV("dev"),
    PROD("prod");

    companion object {
        fun parse(name: String): AppEnvironment =
            entries.firstOrNull { it.wireName == name }
                ?: error("Unknown APP_ENV '$name'; expected one of ${entries.joinToString { it.wireName }}")
    }
}
