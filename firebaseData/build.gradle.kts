import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Firebase adapters (ADR-0021). The only module allowed to depend on the GitLive Firebase SDK.
// It implements the repository and platform contracts declared in :shared; nothing in :shared imports it.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.ahmetbozkan.fartmap.firebase"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.kotlinx.coroutines.core)
            api(libs.gitlive.firebase.auth)
            api(libs.gitlive.firebase.firestore)
            api(libs.gitlive.firebase.storage)
            api(libs.gitlive.firebase.analytics)
            api(libs.gitlive.firebase.crashlytics)
            api(libs.gitlive.firebase.messaging)
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.android.firebase.bom))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
