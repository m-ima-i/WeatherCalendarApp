import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // hilt
    alias(libs.plugins.hilt)

    // ksp
    alias(libs.plugins.ksp)

    // serialization
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.anri.weathercalendarapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.anri.weathercalendarapp"
        minSdk = 33
        targetSdk = 35
        versionCode = 22
        versionName = "1.0.21"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localPropertiesFile = rootProject.file("local.properties")
        val localProperties = Properties()
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }

        buildConfigField("String", "ONE_CALL_API_KEY", "\"${localProperties.getProperty("ONE_CALL_API_KEY", "")}\"")
        buildConfigField("String", "PLACES_API_KEY", "\"${localProperties.getProperty("PLACES_API_KEY", "")}\"")
    }

    buildTypes {
        debug {
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    // Android Lint は IncompatibleClassChangeError でクラッシュするため無効化
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // gson
    implementation(libs.gson)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)



    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Location
    implementation(libs.play.services.location)

    // Google Auth
    implementation(libs.play.services.auth)

    // data store
    implementation(libs.androidx.datastore.preferences)

    // coil
    implementation(libs.coil.compose)

    // splash screen api
    implementation(libs.androidx.core.splashscreen)

    // Worker Manager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // serialization
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // icons
    implementation(libs.androidx.material.icons.extended)

    // google place
    implementation(libs.places)

    // task
    implementation(libs.kotlinx.coroutines.play.services)

    // Glance (Widget)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
}