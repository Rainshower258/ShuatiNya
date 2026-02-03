plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.english"
    compileSdk = 35

    // M-6: 配置 Room schema 导出路径
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    defaultConfig {
        applicationId = "com.example.english"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.SUN6"  // 🥚 Easter Egg: 版本号包含创作者标识

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 🥚 Easter Egg: 自定义构建字段
        buildConfigField("String", "CREATOR_NAME", "\"sun6\"")
        buildConfigField("String", "CREATOR_GITHUB", "\"Rainshower258\"")
        buildConfigField("long", "BUILD_TIMESTAMP", "${System.currentTimeMillis()}L")
    }

    buildTypes {
        release {
            // M-5: 启用 ProGuard 代码混淆和资源压缩
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
        buildConfig = true  // 启用BuildConfig生成
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")  // 用于 ProcessLifecycleOwner
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
}