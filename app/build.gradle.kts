plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinCompose)
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android") // Apply Hilt plugin
    alias(libs.plugins.googleDevtoolsKsp) // Kapt yerine KSP kullanılacak
}

android {
    namespace = "com.aftekeli.currencytracker"
    compileSdk = 34 // Updated to latest stable SDK (Android 14)

    defaultConfig {
        applicationId = "com.aftekeli.currencytracker"
        minSdk = 24
        targetSdk = 34 // Updated to latest stable SDK (Android 14)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Vector drawables compatibility for older APIs (if you use them)
        // vectorDrawables {
        //     useSupportLibrary = true
        // }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Consider true for production to shrink and obfuscate
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // composeOptions { // Only if you need to override the Kotlin Compiler Extension version
    //     kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    // }

    // Packaging options might be needed if you encounter duplicate file errors with some libraries
    // packagingOptions {
    //     resources {
    //         excludes += "/META-INF/{AL2.0,LGPL2.1}"
    //     }
    // }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx) // General lifecycle ktx extensions
    implementation(libs.androidx.activity.compose)

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")

    // Firebase dependencies
    // Always check for the latest stable Firebase BOM version:
    // https://firebase.google.com/docs/android/setup#available-libraries
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Example: Use a recent stable version
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.android.gms:play-services-auth:21.2.0") // Check for latest stable version

    // Hilt dependencies
    // Check for latest stable Hilt version: https://dagger.dev/hilt/gradle-setup
    implementation("com.google.dagger:hilt-android:2.51.1") // Example: Using latest patch for 2.51
    ksp("com.google.dagger:hilt-compiler:2.51.1")      // Example: Using latest patch for 2.51
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0") // Stays the same or check latest

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7") // Stays the same or check latest

    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0") // Check for latest stable version
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")  // Check for latest stable version

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0") // Check for latest stable version
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0") // Align with coroutines-android

    // Retrofit for network requests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // MPAndroidChart for price charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Room dependencies
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Ensure this aligns with compose implementation BOM
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
