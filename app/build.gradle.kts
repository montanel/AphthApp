import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "ch.luca.aphthapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "ch.luca.aphthapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        // The DECISIONS revision the code matches (aphthRevision in gradle.properties), then the
        // build time, which tells installs apart, e.g. "4+2026-11-24.1530".
        versionName = (findProperty("aphthRevision") ?: "0").toString() + "+" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd.HHmm"))
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            // Personal sideloaded app: sign release builds with the debug key so they install directly.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.activity:activity-compose:1.9.3")
    testImplementation("junit:junit:4.13.2")
}
