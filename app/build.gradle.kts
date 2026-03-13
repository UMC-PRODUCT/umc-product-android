import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.android.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.umc.product"
    compileSdk = 36

    defaultConfig {
        buildConfigField(
            "String",
            "KAKAO_APP_KEY",
            getApiKey("kakao.native.key"),
        )
        buildConfigField(
            "String",
            "KAKAO_REST_KEY",
            "\"${getApiKey("kakao.rest.key")}\""
        )
        buildConfigField(
            "String",
            "NAVER_CLIENT_ID",
            "\"${getApiKey("naver.client.id")}\""
        )
        manifestPlaceholders["KAKAO_APP_KEY"] = getApiKey("kakao.app.key")
        applicationId = "com.umc.product"
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "1.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    compileOptions {
        // 하위 버전 호환성 기능 활성화 (필수)
        isCoreLibraryDesugaringEnabled = true

        // 기존에 사용하던 11 버전 유지 (두 개 일치시킬 것)
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    hilt {
        enableAggregatingTask = false
    }
    lint {
        disable += "Instantiatable"
    }
    buildFeatures {
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":presentation"))
    implementation(project(":data"))
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // HILT
    implementation(libs.hilt.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // RETROFIT
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.android)

    // OKHTTP
    implementation(libs.okhttp.android)
    implementation(libs.okhttp.log)

    // KAKAO
    implementation(libs.kakao.user)

    // flexboxLayout
    implementation(libs.google.flexbox)

    // NAVER
    implementation("com.naver.maps:map-sdk:3.23.0")
    
    //opencsv
    implementation(libs.opencsv)

    //firebase
    implementation(platform(libs.firebase.bom))

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    //coli (이미지)
    implementation(libs.coil)

    // Play In-App Update
    implementation(libs.play.update)
    implementation(libs.play.update.ktx)
}

fun getApiKey(propertyKey: String): String {
    return gradleLocalProperties(rootDir, providers).getProperty(propertyKey) ?: ""
}

