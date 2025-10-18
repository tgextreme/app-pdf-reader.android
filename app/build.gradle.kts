plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    id("com.google.dagger.hilt.android") version "2.48"
}

android {
    namespace = "gonzalez.tomas.pdfreadertomas"
    compileSdk = 36

    defaultConfig {
        applicationId = "gonzalez.tomas.pdfreadertomas"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.4")

    // Hilt para inyección de dependencias
    implementation("com.google.dagger:hilt-android:2.48")
    implementation(libs.androidx.compose.foundation.layout)
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // Room para base de datos
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    implementation("androidx.room:room-paging:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")

    // Paging para listas paginadas
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")

    // DataStore para preferencias
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // PDF
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    // Coil para imágenes/thumbnails
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Media para controles de TTS
    implementation("androidx.media:media:1.6.0") // Media legacy
    implementation("androidx.media3:media3-common:1.2.0") // Nueva API Media3
    implementation("androidx.media3:media3-session:1.2.0") // Para MediaSession
    implementation("androidx.media3:media3-exoplayer:1.2.0") // Para la reproducción

    // Material Icons Extended - para iconos adicionales como Bookmark, Sort, etc.
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // WorkManager para tareas en segundo plano
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // OCR (opcional)
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}