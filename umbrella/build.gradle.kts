// iOS umbrella (ADR-0021): composes :shared and :firebaseData into the single static framework `Shared` that iosApp embeds.
// Only iOS targets; no code of its own beyond MainViewController and the iOS Koin composition.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        iosMain.dependencies {
            implementation(project(":shared"))
            implementation(project(":firebaseData"))
            implementation(libs.koin.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
        }
    }
}
