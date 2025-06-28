plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.codecup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.codecup"
        minSdk = 24
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.security.crypto.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.recyclerview)
    implementation (libs.material.v1110)

    // for firebase database
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.auth.ktx.v2231)

    // for firebase storage
    implementation(libs.firebase.storage.ktx)
    implementation(libs.google.firebase.auth.ktx)
    implementation(libs.google.firebase.firestore.ktx)
    implementation(libs.glide)
    implementation(libs.okhttp)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.google.firebase.storage.ktx)
}