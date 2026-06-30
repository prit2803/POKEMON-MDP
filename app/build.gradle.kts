plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp) // Switched to use your libs.versions.toml alias
}

android {
    namespace = "com.example.proyek_mdp"
    compileSdk = 35 // Ubah ke 35 (Android 15) agar stabil

    defaultConfig {
        applicationId = "com.example.proyek_mdp"
        minSdk = 24
        targetSdk = 35 // Ubah ke 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // UPDATE INI: Gunakan Java 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // UPDATE INI: Gunakan Target 17
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // --- FROM VERSION CATALOG ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.tools.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- HARDCODED DEPENDENCIES ---
    // recyclerview
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // cameraX
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    // ML Kit OCR
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ROOM
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    ksp("androidx.room:room-compiler:2.7.0")

    // ARCore & SceneView
    implementation("com.google.ar:core:1.48.0")
    implementation("io.github.sceneview:arsceneview:2.3.0")
    implementation("io.coil-kt:coil:2.7.0")

    // 1. TAMBAHKAN INI (Lifecycle KTX - Ini yang menyediakan lifecycleScope)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")

    // 2. TAMBAHKAN INI (Coroutines Core - Untuk memperbaiki error Supertype)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

}