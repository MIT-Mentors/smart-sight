plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.smartsight"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smartsight"
        minSdk = 24
        targetSdk = 36
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
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    dependencies {
        // Core Android + Compose
        implementation("androidx.core:core-ktx:1.13.1")
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)
        implementation("androidx.navigation:navigation-compose:2.7.7")
        implementation("androidx.compose.material:material-icons-core:1.6.0")
        implementation("androidx.compose.material:material-icons-extended:1.6.0")

        // Android Location + Permissions
        implementation("com.google.android.gms:play-services-location:21.2.0")
        implementation("com.google.accompanist:accompanist-permissions:0.31.5-beta")

        // ViewModel support for Compose
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

        // ML Kit
        implementation("com.google.mlkit:image-labeling:17.0.8")

        // WebSockets + Gson
        implementation("org.java-websocket:Java-WebSocket:1.5.3")
        implementation("com.google.code.gson:gson:2.10.1")

        // ✅ Local Unit Tests (no Android runtime)
        testImplementation("junit:junit:4.13.2")
        testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
        testImplementation("androidx.arch.core:core-testing:2.2.0")
        testImplementation("io.mockk:mockk:1.13.10")
        testImplementation("org.mockito:mockito-core:5.7.0")
        testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
        testImplementation("app.cash.turbine:turbine:1.0.0")

        // ✅ Robolectric (to run Android components like Context & TextToSpeech in unit tests)
        testImplementation("org.robolectric:robolectric:4.11.1")
        testImplementation("androidx.test:core:1.5.0")

        // ✅ Instrumented Tests (runs on Android device/emulator)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)

        // Debug UI testing helpers
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)
    }

}
