import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    // Injects the Crashlytics build id; without it the Crashlytics SDK pulled in by :firebaseData aborts app start.
    alias(libs.plugins.firebaseCrashlytics)
}

// Untracked secrets (androidApp/secrets.properties, written by scripts/inject-config.sh). Missing values stay empty
// so the build never fails on a missing secret; the feature that needs the key fails loudly at runtime instead.
val secrets = Properties().apply {
    val file = rootProject.file("androidApp/secrets.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun secret(name: String): String = secrets.getProperty(name, "")

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":firebaseData"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.ahmetbozkan.fartmap"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.ahmetbozkan.fartmap"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    // One dimension: which backend the build talks to (planning/architecture/FirebaseStrategy.md §8).
    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "Fart Map Dev")
            buildConfigField("String", "APP_ENV", "\"dev\"")
            buildConfigField("String", "MAPS_API_KEY", "\"${secret("MAPS_API_KEY_DEV")}\"")
        }
        create("prod") {
            dimension = "env"
            resValue("string", "app_name", "Fart Map")
            buildConfigField("String", "APP_ENV", "\"prod\"")
            buildConfigField("String", "MAPS_API_KEY", "\"${secret("MAPS_API_KEY_PROD")}\"")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
        // AGP 9 disables resValue by default; the flavors above define app_name with it.
        resValues = true
    }
}
