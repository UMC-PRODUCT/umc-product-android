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
    namespace = "com.umc.presentation.community"
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
            // macOS 에서 만든 기준 이미지와 Linux(CI) 렌더링은 글꼴 안티앨리어싱 때문에 0.01~0.02% 다르다.
            // 그 차이는 넘기고 0.05% 이상 바뀐 화면만 실패로 본다(모서리·구분선 색 변경은 0.05~0.1% 였다).
            imageDifferenceThreshold = 0.0005f
            dependencies {
                implementation(libs.androidx.compose.ui.tooling)
                implementation(libs.screenshot.validation.api)
            }
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
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
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.emoji2.emojipicker)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.coil.compose)

    implementation(libs.kotlinx.collections.immutable)

    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
