import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // iOS targets compile to klibs only; the app framework is produced by :umbrella (ADR-0021).
    iosArm64()
    iosSimulatorArm64()
    
    android {
       namespace = "com.ahmetbozkan.fartmap.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_17
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.koin.android) // androidContext() for the platform module
            // Google sign-in (STORY-01-02-03): Credential Manager + Sign in with Google.
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.playServicesAuth)
            implementation(libs.googleid)
            // Map host (STORY-01-03-02): Google Maps Compose.
            implementation(libs.maps.compose)
            // Device location (STORY-01-03-04): fused location + the Activity result registry for the prompt.
            implementation(libs.play.services.location)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            // BackHandler for Log Fart (picker / uploading); already a transitive runtime dependency of Compose UI.
            implementation(libs.compose.uiBackhandler)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.compose.navigation)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.datastore.preferences)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        // Architecture tests (Konsist) run on the JVM host only: `./gradlew :shared:testAndroidHostTest`.
        getByName("androidHostTest").dependencies {
            implementation(libs.konsist)
            implementation(libs.kotlin.testJunit)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
// Generated resource accessors live under the app package (Res.string.*, Res.drawable.*).
compose.resources {
    packageOfResClass = "com.ahmetbozkan.fartmap.resources"
}
