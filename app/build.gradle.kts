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
}

val requestedTaskNames = gradle.startParameter.taskNames.map { it.lowercase() }
val appliesPlayStoreServices = requestedTaskNames.isEmpty() || requestedTaskNames.any { taskName ->
    taskName.contains("playstore") ||
        taskName.endsWith(":assemble") ||
        taskName == "assemble" ||
        taskName.endsWith(":build") ||
        taskName == "build"
}

if (appliesPlayStoreServices) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

android {
    namespace = "com.byteflipper.everbook"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.byteflipper.everbook"
        minSdk = 26
        targetSdk = 36
        versionCode = 2008
        versionName = "1.6.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("everbook") {
            dimension = "distribution"
        }

        create("playStore") {
            dimension = "distribution"
        }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = " Debug"
            manifestPlaceholders["adMobAppId"] = "ca-app-pub-3940256099942544~3347511713"
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["adMobAppId"] = "ca-app-pub-4346225518624754~1470713545"

            proguardFiles("proguard-rules.pro")

            ndk {
                debugSymbolLevel = "FULL"
            }
        }

        create("release-debug") {
            initWith(getByName("release"))
            applicationIdSuffix = ".release.debug"
            signingConfig = signingConfigs.getByName("debug")
            manifestPlaceholders["adMobAppId"] = "ca-app-pub-3940256099942544~3347511713"
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
        buildConfig = true
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

    filterVariants = arrayOf(
        "everbookDebug",
        "everbookRelease",
        "everbookRelease-debug",
        "playStoreDebug",
        "playStoreRelease",
        "playStoreRelease-debug"
    )
    excludeFields = arrayOf("generated", "funding", "description")
}

tasks.configureEach {
    val isEverbookVariantTask = name.contains("Everbook", ignoreCase = true)
    val isGoogleServicesTask = name.contains("GoogleServices", ignoreCase = true)
    val isCrashlyticsTask = name.contains("Crashlytics", ignoreCase = true)

    if (isEverbookVariantTask && (isGoogleServicesTask || isCrashlyticsTask)) {
        enabled = false
    }
}

dependencies {

    // Core
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.13.0")

    // Compose BOM — alpha
    val composeBom = platform("androidx.compose:compose-bom-alpha:2025.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    testImplementation(composeBom)

    // Compose libraries
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")

    debugImplementation("androidx.compose.ui:ui-tooling")

    // All dependencies
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.work:work-runtime-ktx:2.11.2")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.36.0")

    // Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.57.1")
    ksp("com.google.dagger:hilt-android-compiler:2.57.1")
    ksp("androidx.hilt:hilt-compiler:1.3.0")
    implementation("androidx.hilt:hilt-work:1.3.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    add("playStoreImplementation", platform("com.google.firebase:firebase-bom:34.11.0"))
    add("playStoreImplementation", "com.google.firebase:firebase-analytics")
    add("playStoreImplementation", "com.google.firebase:firebase-crashlytics")
    add("playStoreImplementation", "com.google.firebase:firebase-messaging")
    add("playStoreImplementation", "com.google.firebase:firebase-inappmessaging-display")
    add("playStoreImplementation", "com.google.firebase:firebase-config")

    add("playStoreImplementation", "com.android.billingclient:billing:8.0.0")
    add("playStoreImplementation", "com.google.android.ump:user-messaging-platform:3.1.0")
    add("playStoreImplementation", "com.google.android.gms:play-services-ads:24.9.0")
    add("playStoreImplementation", "com.google.mlkit:translate:17.0.3")
    add("playStoreImplementation", "com.google.mlkit:language-id:17.0.6")
    add("playStoreImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1")

    // Room
    implementation("androidx.room:room-runtime:2.7.1")
    ksp("androidx.room:room-compiler:2.7.1")

    // Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:2.7.1")

    // Datastore (Settings)
    implementation("androidx.datastore:datastore-preferences:1.2.1")

    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.2.0")

    // SAF
    implementation("com.anggrayudi:storage:2.0.0")
    implementation("com.google.accompanist:accompanist-permissions:0.37.0")

    // PDF parser
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    // EPUB parser
    implementation("org.jsoup:jsoup:1.22.2")

    // FB2 parser
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.0")

    // Language Switcher
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.appcompat:appcompat-resources:1.7.1")

    // Coil for loading images
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Open source libraries
    implementation("com.mikepenz:aboutlibraries-core:11.4.0")
    implementation("com.mikepenz:aboutlibraries-compose-m3:11.4.0")

    // Drag & Drop
    implementation("sh.calvin.reorderable:reorderable:2.5.1")

    // Scrollbar
    implementation("com.github.nanihadesuka:LazyColumnScrollbar:2.2.0")

    // Gson
    implementation("com.google.code.gson:gson:2.11.0")

    // Markdown
    implementation("org.commonmark:commonmark:0.24.0")
}
