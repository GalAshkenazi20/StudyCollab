plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.studycollab"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.studycollab"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["disableAnalytics"] = "true"
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
    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.generativeai)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.material3)
//    implementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    // Removed Espresso - using only Compose UI testing for API 36 compatibility
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    // 1. Retrofit (For connecting to your Server/MongoDB)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // To convert JSON to Java/Kotlin objects

    // 2. Navigation for Jetpack Compose (To move between Login, Home, Chat)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // 3. Extended Icons (Optional, but good for UI)
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("javax.inject:javax.inject:1")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    implementation("com.microsoft.identity.client:msal:5.4.0") {
        exclude(group = "com.microsoft.device.display", module = "display-mask")
    }
}

// Resolution strategy removed - using pure Compose UI testing without Espresso

