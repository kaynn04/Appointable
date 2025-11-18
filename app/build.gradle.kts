plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") version "4.4.4" apply false
}

android {
    namespace = "com.example.appointable"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.appointable"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies { // Single, correct block
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation(libs.fragment)

    // STEP 1: Implement the BOM as a platform
    implementation(platform(libs.firebase.bom))

    // STEP 2: Implement the individual libraries (versions are inherited from the BOM)
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.firestore)

    // ... your play services auth dependency
    implementation(libs.play.services.auth)

    apply(plugin = "com.google.gms.google-services")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}