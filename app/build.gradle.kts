import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    id("com.mikepenz.aboutlibraries.plugin")
    id("androidx.room")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.byteflipper.everbook"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.byteflipper.everbook"
        minSdk = 26
        targetSdk = 35
        versionCode = 5
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = " Debug"
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = false

            proguardFiles("proguard-rules.pro")

            ndk {
                debugSymbolLevel = "FULL"
            }
        }

        create("release-debug") {
            initWith(getByName("release"))
            applicationIdSuffix = ".release.debug"
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }
}

aboutLibraries {
    registerAndroidTasks = false
    prettyPrint = true
    gitHubApiToken = gradleLocalProperties(rootDir, providers)["github-key"] as? String

    filterVariants = arrayOf("debug", "release", "release-debug")
    excludeFields = arrayOf("generated", "funding", "description")
}

dependencies {

    // Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")

    // Compose BOM libraries
    // Compose BOM was eliminated - it is recognized as Closed Source in AboutLibraries..
    // although it is not.

    implementation("androidx.compose.foundation:foundation:1.8.0-beta03")
    implementation("androidx.compose.animation:animation:1.7.8")
    implementation("androidx.compose.animation:animation-android:1.8.0-beta03")
    implementation("androidx.compose.foundation:foundation-layout:1.7.8")
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.ui:ui-graphics:1.7.8")
    implementation("androidx.compose.ui:ui-android:1.8.0-beta03")
    implementation("androidx.compose.material3:material3:1.4.0-alpha08")
    implementation("androidx.compose.material3:material3-window-size-class:1.3.1")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.material:material:1.7.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")

    debugImplementation("androidx.compose.ui:ui-tooling:1.7.8")

    // All dependencies
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.36.0")

    // All dependencies
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.36.0")

    // Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.55")
    ksp("com.google.dagger:hilt-android-compiler:2.55")
    implementation("com.google.dagger:hilt-compiler:2.55")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-inappmessaging-display")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:2.6.1")

    // Datastore (Settings)
    implementation("androidx.datastore:datastore-preferences:1.1.3")

    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")

    // SAF
    implementation("com.anggrayudi:storage:2.0.0")
    implementation("com.google.accompanist:accompanist-permissions:0.37.0")

    // PDF parser
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    // EPUB parser
    implementation("org.jsoup:jsoup:1.18.3")

    // FB2 parser
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.0")

    // Language Switcher
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.appcompat:appcompat-resources:1.7.0")

    // Coil for loading images
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Open source libraries
    implementation("com.mikepenz:aboutlibraries-core:11.4.0")
    implementation("com.mikepenz:aboutlibraries-compose-m3:11.4.0")

    // Drag & Drop
    implementation("sh.calvin.reorderable:reorderable:2.4.3")

    // Scrollbar
    implementation("com.github.nanihadesuka:LazyColumnScrollbar:2.2.0")

    // Gson
    implementation("com.google.code.gson:gson:2.11.0")

    // Markdown
    implementation("org.commonmark:commonmark:0.24.0")
}