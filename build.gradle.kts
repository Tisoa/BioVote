// Top-level build file
plugins {
    alias(libs.plugins.android.application)   apply false
    alias(libs.plugins.kotlin.android)       apply false
    alias(libs.plugins.compose.compiler)     apply false

    // Declare Hilt & KSP versions here, but don’t apply
    id("com.google.dagger.hilt.android")     version "2.56.2" apply false
    id("com.google.devtools.ksp") version "2.1.21-2.0.1" apply false

}