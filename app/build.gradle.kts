import java.util.Properties
import java.io.FileInputStream

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  id("com.google.devtools.ksp")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY") ?: "\"\""

android {
    namespace = "com.example.pokedex"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.pokedex"
        minSdk = 24
        targetSdk = 36
        versionCode = 6
        versionName = "1.5.1"
        buildConfigField("String", "GEMINI_API_KEY", geminiApiKey)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = true
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}


dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // CameraX
  val camerax_version = "1.3.0"
  implementation("androidx.camera:camera-core:${camerax_version}")
  implementation("androidx.camera:camera-camera2:${camerax_version}")
  implementation("androidx.camera:camera-lifecycle:${camerax_version}")
  implementation("androidx.camera:camera-view:${camerax_version}")

  // Room Database
  val room_version = "2.6.1"
  implementation("androidx.room:room-runtime:${room_version}")
  implementation("androidx.room:room-ktx:${room_version}")
  ksp("androidx.room:room-compiler:${room_version}")
  
  // TFLite Image Classifier
  implementation("org.tensorflow:tensorflow-lite:2.5.0")
  implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
  
  // OkHttp for Downloading Models
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  
  // Image Loading
  implementation("io.coil-kt:coil-compose:2.5.0")
}
