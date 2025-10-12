plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // ✅ plugin Google Services placé ici
}

android {
    namespace = "com.equipe7.eductrack"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.equipe7.eductrack"
        minSdk = 26
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // 🔹 AndroidX / Material
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // 🔹 Librairies externes
    implementation("com.github.lzyzsd:circleprogress:1.2.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")   // Graphiques
    implementation("com.squareup.picasso:picasso:2.8")           // Images
    implementation("com.squareup.okhttp3:okhttp:4.12.0")         // HTTP client
    implementation("com.tbuonomo:dotsindicator:4.3")             // Dots Indicator

    // ✅ Firebase BoM - centralise les versions
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))

    // ✅ Firebase services (versions gérées par le BoM)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-functions")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")

    // ✅ Google Mobile Ads (AdMob)
    implementation("com.google.android.gms:play-services-ads:22.6.0")

    // ✅ Google Identity Services (nouvelle API login Google)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // ✅ Jetpack Compose (optionnel)
    implementation("androidx.compose.ui:ui-text-google-fonts:1.9.0")
    implementation(libs.ui.text)

    // 🔹 Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
