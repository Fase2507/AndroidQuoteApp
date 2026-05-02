import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

android {
    namespace = "tr.duzce.edu.bm.androidquoteapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "tr.duzce.edu.bm.androidquoteapp"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        val apiKey = localProperties.getProperty("GEMINI_API_KEY") ?: "YOUR_API_KEY_HERE"
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")

        val mailUsername = localProperties.getProperty("mail.username") ?: ""
        val mailPassword = localProperties.getProperty("mail.password") ?: ""
        buildConfigField("String", "MAIL_USERNAME", "\"$mailUsername\"")
        buildConfigField("String", "MAIL_PASSWORD", "\"$mailPassword\"")
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

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/LICENSE.md"
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.jakarta.mail)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
}
