plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.diplomx"
    compileSdk   = 35

    defaultConfig {
        applicationId = "com.example.diplomx"
        minSdk               = 28
        targetSdk            = 35
        versionCode          = 1
        versionName          = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    kotlinOptions {
        jvmTarget = "11"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES*",
                "META-INF/NOTICE*",
                "META-INF/LICENSE*",
                "META-INF/io.netty.versions.properties",
                // remove duplicate annotation‐processor listing
                "META-INF/gradle/incremental.annotation.processors"
            )
        }
    }
}

dependencies {
    // Hilt
    kapt           (libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Compose & Navigation
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)

    // Lifecycle & Core
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.core.ktx)

    // CameraX (optional)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.hilt.android)

    kapt(libs.hilt.android.compiler)
    implementation(libs.hilt.android)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)

    implementation(libs.guava)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation   (libs.compose.ui.tooling)
    debugImplementation   (libs.compose.ui.test.manifest)
}