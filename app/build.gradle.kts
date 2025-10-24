plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Must be applied here (no "apply false")
}

android {
    namespace = "com.prm392.taskmanaapp"
    compileSdk = 34 // use stable SDK instead of 36 (still preview)

    defaultConfig {
        applicationId = "com.prm392.taskmanaapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // AndroidX
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")

    // --- Firebase ---
    // Firebase BoM (manages all Firebase versions automatically)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Firebase modules
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Optional (for analytics / debugging)
    implementation("com.google.firebase:firebase-analytics")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
