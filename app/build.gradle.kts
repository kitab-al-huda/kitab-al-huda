plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

// --- Charger les variables depuis .env ---
fun loadEnvFile(): Map<String, String> {
    val envFile = rootProject.file(".env")
    if (!envFile.exists()) {
        throw GradleException(
            "Fichier .env introuvable ! Copiez .env.example → .env et remplissez vos secrets."
        )
    }
    return envFile.readLines()
        .filter { it.isNotBlank() && !it.startsWith("#") && it.contains("=") }
        .associate { line ->
            val (key, value) = line.split("=", limit = 2)
            key.trim() to value.trim()
        }
}

val env = loadEnvFile()

android {
    namespace = "com.alfred.kitabalhuda"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.alfred.kitabalhuda"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Clé de déchiffrement (Partie 1) — depuis .env
        buildConfigField("String", "INTERNAL_BUILD_ID", "\"${env["INTERNAL_BUILD_ID"]}\"")

        // Informations d'identification chiffrées — depuis .env
        buildConfigField("String", "API_ENDPOINT_BASE", "\"${env["API_ENDPOINT_BASE"]}\"")
        buildConfigField("String", "API_SERVICE_IDENTIFIER", "\"${env["API_SERVICE_IDENTIFIER"]}\"")
        buildConfigField("String", "API_AUTH_SIGNATURE", "\"${env["API_AUTH_SIGNATURE"]}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-paging:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Retrofit for network calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // GSON for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Preference
    implementation("androidx.preference:preference-ktx:1.2.1")
    
    // Paging
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    
    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.7.3")
    
    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    // Material Components
    implementation ("com.google.android.material:material:1.12.0")
    // ExoPlayer for video playback
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}