import java.util.Properties
// Load local.properties once
val localProperties = Properties().apply {
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        load(localPropsFile.inputStream())
    }
}

val sendgridApiKey = localProperties.getProperty("SENDGRID_API_KEY") ?: ""

fun escapeForJavaString(str: String): String =
    str.replace("\\", "\\\\").replace("\"", "\\\"")

val escapedSendgridKey = escapeForJavaString(sendgridApiKey)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
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

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "SENDGRID_API_KEY", "\"your_actual_key_here\"")
        }
        release {
            buildConfigField("String", "SENDGRID_API_KEY", "\"your_actual_key_here\"")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.fragment.ktx)

    // to use ViewModel, LiveData, ...
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // for saving API keys and send email
    implementation(libs.androidx.security.crypto.ktx)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.recyclerview)
    implementation (libs.material.v1110)

    // for cropping the image
    implementation (libs.material)

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

    // for sending email
    implementation(libs.okhttp)
    implementation(libs.okhttp.v490)

    // for converting profile to JSON and JSON back to Profile
    implementation(libs.gson)
}