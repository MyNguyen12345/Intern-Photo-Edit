plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")

}

android {
    namespace = "com.example.photoedit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.photoedit"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding= true
    }
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src/main/assets")
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    runtimeOnly("androidx.core:core-ktx:1.13.1")
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)

    val camerax_version = "1.5.0-alpha02"
//    implementation(libs.androidx.camera.core)
    implementation ("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-video:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    implementation ("androidx.viewpager2:viewpager2:1.0.0")


    implementation("com.github.krokyze:ucropnedit:2.2.6-2")
    implementation("io.reactivex.rxjava3:rxjava:3.1.10")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")

    implementation ("jp.co.cyberagent.android:gpuimage:2.1.0")

    implementation ("com.isseiaoki:simplecropview:1.1.8")



    implementation("com.github.LottieFiles:dotlottie-android:0.5.0")
    implementation("com.airbnb.android:lottie:6.1.0")




    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}