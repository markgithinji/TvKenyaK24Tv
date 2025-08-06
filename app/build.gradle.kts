plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    compileSdk = 36

    defaultConfig {
        namespace = "com.kenyantvlive.k24tv"
        applicationId = "com.kenyantvlive.k24tv"
        minSdk = 23
        targetSdk = 36
        versionCode = 10
        versionName = "1.2.0"
    }

    buildTypes {
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.core:core-splashscreen:1.1.0-rc01")
    implementation ("androidx.core:core-ktx:1.16.0")

    implementation("com.google.firebase:firebase-analytics:23.0.0")
    implementation("com.google.firebase:firebase-firestore:26.0.0")

    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("com.google.android.exoplayer:exoplayer-ui:2.19.1")

    implementation("com.google.android.gms:play-services-ads:24.5.0")
    implementation("com.google.ads.mediation:facebook:6.20.0.0")
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")

}
