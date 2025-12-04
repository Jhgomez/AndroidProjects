plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.yarolegovich.mp"
    compileSdk = 36

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
//    androidxCompat: 'androidx.appcompat:appcompat:1.1.0'
//    annotations   : 'androidx.annotation:annotation:1.1.0'
//    cardView      : 'androidx.cardview:cardview:1.0.0'
//    designSupport : 'com.google.android.material:material:1.0.0'
//    colorPicker   : 'com.github.Kunzisoft:AndroidClearChroma:2.3'
//    implementation("androidx.appcompat:appcompat:1.1.0")
//    implementation("androidx.annotation:annotation:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.0.0")
    implementation("com.github.Kunzisoft:AndroidClearChroma:2.3")
    implementation(project(":lovelydialog"))
}