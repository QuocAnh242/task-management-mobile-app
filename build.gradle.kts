// Top-level build.gradle.kts

buildscript {
    dependencies {
        // This must be here for Firebase Google Services plugin
        classpath("com.google.gms:google-services:4.4.2")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    // Optional: if you plan to use Kotlin
    // alias(libs.plugins.kotlin.android) apply false
}
