plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.android.ksp)
}

android {
    lint {
        abortOnError = false
    }
    namespace = "com.umc.presentation.home"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
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
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    testOptions {
        // 스크린샷 테스트가 Compose 미리보기를 그리려면 컴파일된 리소스(테마·폰트·드로어블)가 필요하다.
        unitTests.isIncludeAndroidResources = true
        // Compose Preview 스크린샷 테스트(AGP test suite). 테스트는 src/screenshotTest, 기준 이미지는 src/screenshotTestDefaultDebug/reference.
        screenshotTests.create("screenshotTest") {
            engineVersion = libs.versions.screenshot.get()
            targetVariants.add("debug")
            dependencies {
                implementation(libs.androidx.compose.ui.tooling)
                implementation(libs.screenshot.validation.api)
            }
        }
    }
    buildFeatures {
        compose = true
    }

    testOptions {
        // ViewModel 의 android.util.Log 호출이 JVM 단위 테스트에서 예외 대신 기본값을 돌려주게 한다
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.kotlinx.collections.immutable)
    lintChecks(project(":lint-rules"))
    implementation(project(":domain"))
    implementation(project(":presentation:component"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)

    //Desugaring용 라이브러리 추가 -> API 24에서 상위 레벨 사용을 위함
    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    // NAVER MAPS & LOCATION
    implementation(libs.naver.maps.sdk)
    implementation(libs.naver.maps.compose)
    implementation(libs.googleplay.services.location)

    //비동기 이미지
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}