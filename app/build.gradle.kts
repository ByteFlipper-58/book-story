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

// Google Services (Firebase + Crashlytics) are only needed by the playStore flavor.
// Apply the plugins unless the build is exclusively for a Google-free flavor (everbook / ruStore),
// in which case applying them would fail for the missing google-services.json.
val isGoogleFreeOnlyBuild = gradle.startParameter.taskNames
    .map { it.lowercase() }
    .let { tasks ->
        tasks.isNotEmpty() && tasks.all { it.contains("everbook") || it.contains("rustore") }
    }

if (!isGoogleFreeOnlyBuild) {
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
        versionCode = 2011
        versionName = "1.5.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Three distribution flavors:
    //   everbook  — clean build, no ads / analytics / Google services
    //   playStore — Google Play: AdMob + Firebase + Play In-App Update/Review
    //   ruStore   — RuStore: Yandex ads + RuStore In-App Update/Review/RemoteConfig
    flavorDimensions += "distribution"
    productFlavors {
        create("everbook") {
            dimension = "distribution"
        }

        create("playStore") {
            dimension = "distribution"
        }

        create("ruStore") {
            dimension = "distribution"
            buildConfigField("String", "RU_STORE_APP_ID", "\"d2b174ae-1727-4d24-b00b-88be1032d09d\"")
        }
    }

    sourceSets {
        // Contracts and on-device ML Kit translation extracted into shared source sets so the
        // ad-bearing flavors (playStore, ruStore) reuse them instead of duplicating the code.
        getByName("playStore") {
            java.srcDirs("src/sharedAds/java", "src/mlkitTranslation/java")
        }
        getByName("ruStore") {
            java.srcDirs("src/sharedAds/java", "src/mlkitTranslation/java")
        }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    signingConfigs {
        // Read signing credentials from keystore.properties, env vars, or -P flags.
        val keystoreProps = mutableMapOf<String, String>()
        val keystoreFile = rootProject.file("keystore.properties")
        if (keystoreFile.exists()) {
            keystoreFile.readLines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("#") || trimmed.isBlank()) return@forEach
                val eq = trimmed.indexOf('=')
                if (eq < 1) return@forEach
                keystoreProps[trimmed.substring(0, eq).trim()] = trimmed.substring(eq + 1).trim()
            }
        }
        fun prop(name: String): String? =
            keystoreProps[name]
                ?: findProperty(name) as? String
                ?: System.getenv(name)

        create("release") {
            storeFile = file(prop("RELEASE_STORE_FILE") ?: "release.keystore")
            storePassword = prop("RELEASE_STORE_PASSWORD") ?: ""
            keyAlias = prop("RELEASE_KEY_ALIAS") ?: ""
            keyPassword = prop("RELEASE_KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        getByName("debug") {
            manifestPlaceholders["adMobAppId"] = "ca-app-pub-3940256099942544~3347511713"
        }

        getByName("release") {
            // Use release keystore if available; otherwise leave unsigned (developer build).
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile?.exists() == true) {
                signingConfig = releaseSigning
            }

            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["adMobAppId"] = "ca-app-pub-4346225518624754~1470713545"

            proguardFiles("proguard-rules.pro")

            ndk {
                debugSymbolLevel = "FULL"
            }
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
        "playStoreDebug",
        "playStoreRelease",
        "ruStoreDebug",
        "ruStoreRelease"
    )
    excludeFields = arrayOf("generated", "funding", "description")
}

tasks.configureEach {
    // everbook / ruStore have no google-services.json, so skip any Google-services / Crashlytics
    // task generated for their variants (relevant when building all flavors at once).
    val isGoogleFreeVariant = name.contains("Everbook", true) || name.contains("RuStore", true)
    val isGoogleTask = name.contains("GoogleServices", true) || name.contains("Crashlytics", true)
    if (isGoogleFreeVariant && isGoogleTask) {
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

    // ── playStore flavor: Google services + AdMob ───────────────────────────────
    add("playStoreImplementation", platform("com.google.firebase:firebase-bom:34.11.0"))
    add("playStoreImplementation", "com.google.firebase:firebase-analytics")
    add("playStoreImplementation", "com.google.firebase:firebase-crashlytics")
    add("playStoreImplementation", "com.google.firebase:firebase-messaging")
    add("playStoreImplementation", "com.google.firebase:firebase-inappmessaging-display")
    add("playStoreImplementation", "com.google.firebase:firebase-config")
    add("playStoreImplementation", "com.android.billingclient:billing:8.0.0")
    add("playStoreImplementation", "com.google.android.play:app-update-ktx:2.1.0")
    add("playStoreImplementation", "com.google.android.play:review-ktx:2.0.2")
    add("playStoreImplementation", "com.google.android.ump:user-messaging-platform:3.1.0")
    add("playStoreImplementation", "com.google.android.gms:play-services-ads:24.9.0")

    // ── ruStore flavor: Yandex ads + RuStore SDKs (BOM 2026.06.01 → 10.5.0) ──────
    add("ruStoreImplementation", "com.yandex.android:mobileads:8.1.0")
    add("ruStoreImplementation", platform("ru.rustore.sdk:bom:2026.06.01"))
    add("ruStoreImplementation", "ru.rustore.sdk:appupdate")
    add("ruStoreImplementation", "ru.rustore.sdk:review")
    add("ruStoreImplementation", "ru.rustore.sdk:remoteconfig")

    // ── shared by both ad flavors: on-device ML Kit translation (works without GMS) ─
    listOf("playStoreImplementation", "ruStoreImplementation").forEach { config ->
        add(config, "com.google.mlkit:translate:17.0.3")
        add(config, "com.google.mlkit:language-id:17.0.6")
        add(config, "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1")
    }

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
