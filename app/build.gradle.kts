// Required imports for working with local.properties
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

// -------- Load local.properties securely --------
val localProps = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        FileInputStream(localFile).use { fis -> load(fis) }
    } else {
        println("⚠ local.properties file not found! API keys will be empty.")
    }
}

android {
    namespace = "com.unified.healthfitness"
    compileSdk = 36

    buildFeatures {
        buildConfig = true  // ✅ Enable BuildConfig generation
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.unified.healthfitness"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // ✅ Gemini API key from gradle.properties
        val apiKey: String = project.findProperty("GEMINI_API_KEY") as String? ?: ""
        buildConfigField("String", "API_KEY", "\"${apiKey}\"")

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
        // 🔹 Core Android libraries
        implementation("androidx.core:core:1.10.1")
        implementation("androidx.appcompat:appcompat:1.7.0")
        implementation("androidx.cardview:cardview:1.0.0")
        implementation("androidx.recyclerview:recyclerview:1.3.1")
        implementation("androidx.constraintlayout:constraintlayout:2.2.0")
        implementation("com.google.android.material:material:1.13.0")
    implementation("com.google.android.material:material:1.12.0")

        // 🔹 Lifecycle + MVVM
        implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
        implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
        implementation("androidx.lifecycle:lifecycle-runtime:2.7.0")

        // 🔹 Room Database
        val room_version = "2.6.1"
        implementation("androidx.room:room-runtime:$room_version")
        annotationProcessor("androidx.room:room-compiler:$room_version")

        // 🔹 Firebase
        implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
        implementation("com.google.firebase:firebase-auth")
        implementation("com.google.firebase:firebase-firestore")
        implementation("com.google.firebase:firebase-storage")

        // 🔹 Networking
        implementation("com.squareup.okhttp3:okhttp:4.12.0")
        implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")

        // 🔹 Image Loading
        implementation("com.github.bumptech.glide:glide:4.16.0")
        annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

        // 🔹 Charts
        implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

        // 🔹 Gson
        implementation("com.google.code.gson:gson:2.10.1")

        // 🔹 CSV Parsing
        implementation("com.opencsv:opencsv:5.8")

        // 🔹 Coroutines
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

        // 🔹 Navigation
        implementation("androidx.navigation:navigation-fragment:2.7.6")
        implementation("androidx.navigation:navigation-ui:2.7.6")

        // 🔹 Swipe Refresh
        implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

        // 🔹 ExoPlayer (for video playback)
        implementation("com.google.android.exoplayer:exoplayer:2.19.1")

        // 🔹 WorkManager (🆕 added)
        implementation("androidx.work:work-runtime:2.9.0")

        // 🔹 Fragment (🆕 added)
        implementation("androidx.fragment:fragment:1.6.2")

        // 🔹 Testing dependencies
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.2.1")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

// ---------- Dynamically Generate Keys.java ----------
androidComponents.onVariants { variant ->
    val variantName = variant.name.replaceFirstChar { it.uppercaseChar() }

    tasks.register("generateKeys$variantName") {
        doLast {
            val keysFile = file("src/main/java/com/unified/healthfitness/Keys.java")
            keysFile.parentFile.mkdirs()

            // Read keys from local.properties
            val pexelsKey = localProps.getProperty("PEXELS_API_KEY", "")
            val unsplashKey = localProps.getProperty("UNSPLASH_API_KEY", "")
            val weatherKey = localProps.getProperty("OPENWEATHER_API_KEY", "")

            // Write Keys.java
            keysFile.writeText(
                """
                package com.unified.healthfitness;

                public class Keys {
                    public static final String PEXELS_API_KEY = "$pexelsKey";
                    public static final String UNSPLASH_API_KEY = "$unsplashKey";
                    public static final String OPENWEATHER_API_KEY = "$weatherKey";
                }
                """.trimIndent()
            )
            println("✅ Keys.java generated at: ${keysFile.absolutePath}")
        }
    }.also { taskProvider ->
        tasks.named("preBuild").configure {
            dependsOn(taskProvider)
        }
    }
}
