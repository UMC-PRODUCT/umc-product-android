import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    lint {
        abortOnError = false
    }
    namespace = "com.umc.presentation"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField(
            "String",
            "GOOGLE_LOGIN_KEY",
            "\"${getApiKey("google.login.key")}\""
        )
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.kotlinx.collections.immutable)
    lintChecks(project(":lint-rules"))
    implementation(project(":domain"))
    implementation(project(":presentation:act"))
    implementation(project(":presentation:splash"))
    implementation(project(":presentation:login"))
    implementation(project(":presentation:home"))
    implementation(project(":presentation:mypage"))
    implementation(project(":presentation:signUp"))
    implementation(project(":presentation:permission"))
    implementation(project(":presentation:failCode"))
    implementation(project(":presentation:notice"))
    implementation(project(":presentation:component"))
    implementation(project(":presentation:study"))
    implementation(project(":presentation:community"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // HILT
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // COROUTINE
    implementation(libs.kotlinx.coroutines.android)


    // KAKAO
    implementation(libs.kakao.user)


    //opencsv
    implementation(libs.opencsv)

    // flexboxLayout
    implementation(libs.google.flexbox)

    // NAVER MAPS & LOCATION
    implementation(libs.naver.maps.sdk)
    implementation(libs.googleplay.services.location)

    //firebase meesage
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    //coli (이미지)
    implementation(libs.coil)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.google.play.services.auth)

    // Play In-App Update
    implementation(libs.play.update)
    implementation(libs.play.update.ktx)
}

fun getApiKey(propertyKey: String): String {
    // AGP 내부 API(gradleLocalProperties) 대신 local.properties 를 직접 읽어 AGP 버전에 묶이지 않게 한다.
    val localProperties = providers.fileContents(rootProject.layout.projectDirectory.file("local.properties")).asText.orNull
        ?: return ""
    return Properties().apply { load(localProperties.reader()) }.getProperty(propertyKey) ?: ""
}

// FeatureMapTest 가 읽는 표만 바꿨을 때도 테스트가 다시 돌도록 입력으로 등록한다.
tasks.withType<Test>().configureEach {
    inputs.file(rootProject.file(".claude/skills/run-umc-app/feature-map.md")).withPathSensitivity(PathSensitivity.RELATIVE)
}
