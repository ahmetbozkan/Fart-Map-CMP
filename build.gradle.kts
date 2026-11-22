import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    // Declared once here so every subproject's classloader sees the same plugin versions.
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.firebaseCrashlytics) apply false
    alias(libs.plugins.detekt) apply false
}

// Static analysis for every Kotlin module (rules: config/detekt/detekt.yml). `./gradlew detekt` runs all;
// `check` depends on it. Enforces the no-swallowed-errors rule and Compose conventions automatically.
val detektModules = setOf("shared", "firebaseData", "umbrella", "androidApp")
val detektFormatting = libs.detekt.formatting
val composeRulesDetekt = libs.compose.rules.detekt
val detektKotlinVersion = libs.versions.detekt.kotlin.get()

subprojects {
    if (name !in detektModules) return@subprojects
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        source.setFrom(files("src"))
        parallel = true
        autoCorrect = providers.gradleProperty("detektAutoCorrect").isPresent
    }

    dependencies {
        add("detektPlugins", detektFormatting)
        add("detektPlugins", composeRulesDetekt)
    }

    // detekt 1.23 is compiled against Kotlin 2.0.21; keep the Kotlin Gradle plugin from aligning its
    // analyzer classpath to the project's Kotlin version.
    configurations.matching { it.name == "detekt" }.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") useVersion(detektKotlinVersion)
        }
    }
}
