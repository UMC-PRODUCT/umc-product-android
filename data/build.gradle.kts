plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.android.ksp)
}

android {
    lint {
        abortOnError = false
    }
    namespace = "com.umc.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        create("benchmark") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
        }
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
}

dependencies {
    implementation(project(":domain"))
    // PR 축약 검사(모듈별 lint)에서도 커스텀 규칙이 data 소스를 보도록 등록한다.
    lintChecks(project(":lint-rules"))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.installations)

    implementation(libs.hilt.android)

    // 온디바이스 AI (ML Kit GenAI Prompt API / Gemini Nano)
    implementation(libs.mlkit.genai.prompt)
    implementation(libs.kotlinx.coroutines.guava)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.hilt.core)

    // RETROFIT
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.android)

    // OKHTTP
    implementation(libs.okhttp.android)
    implementation(libs.okhttp.log)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.google.code.gson)
    implementation(libs.kotlinx.serialization.json)
}
