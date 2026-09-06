plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
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

// 실제 dev 서버로 네트워크를 타는 테스트는 CI 에서 제외한다.
// (서버 가동 상태에 따라 결과가 달라져 파이프라인이 불안정해진다)
tasks.withType<Test>().configureEach {
    if (providers.environmentVariable("CI").isPresent) {
        filter { excludeTestsMatching("com.umc.data.EmailLoginApiTest") }
    }
}
