import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
}

android {
    lint {
        abortOnError = false
    }
    namespace = "com.umc.presentation.login"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        buildConfigField(
            "String",
            "GOOGLE_LOGIN_KEY",
            "\"${getApiKey("google.login.key")}\""
        )

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
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        // 스크린샷 테스트가 Compose 미리보기를 그리려면 컴파일된 리소스(테마·폰트·드로어블)가 필요하다.
        unitTests.isIncludeAndroidResources = true
        // 리소스를 넣으면 단위 테스트가 없는 이 모듈의 testDebugUnitTest 도 '테스트 소스가 있다'고 보여 Gradle 9 가 실패시킨다.
        // 단위 테스트가 생기기 전까지는 예전처럼(NO-SOURCE) 통과시킨다.
        unitTests.all { it.failOnNoDiscoveredTests.set(false) }
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
        buildConfig = true
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
    implementation(libs.hilt.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //firebase meesage
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // KAKAO
    implementation(libs.kakao.user)

    //GOOGLE
    implementation(libs.googleid)
    implementation(libs.google.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
}

fun getApiKey(propertyKey: String): String {
    // AGP 내부 API(gradleLocalProperties) 대신 local.properties 를 직접 읽어 AGP 버전에 묶이지 않게 한다.
    val localProperties = providers.fileContents(rootProject.layout.projectDirectory.file("local.properties")).asText.orNull
        ?: return ""
    return Properties().apply { load(localProperties.reader()) }.getProperty(propertyKey) ?: ""
}