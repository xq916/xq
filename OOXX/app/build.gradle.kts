plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.ooxx"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ooxx"
        minSdk = 24
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

    // 启用 View Binding
    buildFeatures {
        viewBinding = true
    }

    buildFeatures {
        dataBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // 添加 jBCrypt 依赖
    implementation(libs.jbcrypt)

    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.google.android.material:material:1.6.0")

    // 网络依赖
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // 数据库依赖
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // 图片加载
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")


}