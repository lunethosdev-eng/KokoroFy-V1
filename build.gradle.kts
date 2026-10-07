plugins {
    id("com.android.application") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}

}

android {
    namespace = "com.kokorofy.music"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kokorofy.music"
        minSdk = 26
        targetSdk = 35
        versionCode = 6
        versionName = "1.4.0"
    }

    // MISMA firma siempre → updates sin desinstalar (después del cambio de clave)
    signingConfigs {
        create("kokoro") {
            val ks = rootProject.file("keystore/kokorofy.jks")
            if (ks.exists()) {
                storeFile = ks
                storePassword = "kokorofy123"
                keyAlias = "kokorofy"
                keyPassword = "kokorofy123"
            }
        }
    }

    buildTypes {
        debug {
            val ks = rootProject.file("keystore/kokorofy.jks")
            if (ks.exists()) {
                signingConfig = signingConfigs.getByName("kokoro")
            }
        }
        release {
            val ks = rootProject.file("keystore/kokorofy.jks")
            if (ks.exists()) {
                signingConfig = signingConfigs.getByName("kokoro")
            }
            isMinifyEnabled = false
        }
    }

    buildFeatures { compose = true }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-session:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("androidx.media3:media3-datasource-okhttp:1.5.1")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

kotlin {
    jvmToolchain(17)
}
