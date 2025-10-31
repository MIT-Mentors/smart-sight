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

        // Runner for instrumented Android tests
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

    // Allows using Android resources in unit tests
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    // -----------------------------
    // 🔹 Core Android + Compose
    // -----------------------------
    implementation("androidx.core:core-ktx:1.13.1")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-core:1.6.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // -----------------------------
    // Feature Libraries
    // -----------------------------
    implementation("org.java-websocket:Java-WebSocket:1.5.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.accompanist:accompanist-permissions:0.31.5-beta")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("com.google.mlkit:image-labeling:17.0.8")

    // -----------------------------
    // 🔹 Unit Testing
    // -----------------------------
    // JUnit 4 (core testing framework)
    testImplementation("junit:junit:4.13.2")

    // Mockito core + inline mocking support
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.11.0")

    // Mockito Kotlin extensions
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")

    // Mockito JUnit runner (for @RunWith(MockitoJUnitRunner::class))
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")

    // Coroutines testing (for runTest, etc.)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    // AndroidX ViewModel + LiveData testing
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // -----------------------------
    //  Instrumented (Android) Tests
    // -----------------------------
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Compose UI Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debug tools for Compose
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
