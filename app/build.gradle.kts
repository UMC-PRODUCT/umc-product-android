import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.android.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
}

/**
 * API 서버 환경 스위치.
 *
 * - `-PapiEnv=dev`  → dev 서버 (develop-compose / 테스트 트랙)
 * - 미지정 또는 `-PapiEnv=prod` → 운영 서버 (main / 프로덕션 트랙)
 */
val apiEnv = (project.findProperty("apiEnv") as String?) ?: "prod"
val apiBaseUrl = when (apiEnv) {
    "dev" -> "https://dev.api.university.neordinary.com/"
    "prod" -> "https://api.university.neordinary.com/"
    else -> throw GradleException("apiEnv 는 dev 또는 prod 여야 합니다 (입력값: $apiEnv)")
}

android {
    lint {
        abortOnError = false
        // :lint-rules 커스텀 규칙을 presentation/data/domain 모듈까지 적용.
        // PR 축약 검사에서는 각 모듈이 자기 lintDebug 를 직접 돌리므로
        // -PlintCheckDependencies=false 로 꺼서 중복 검사를 피한다.
        checkDependencies =
            (project.findProperty("lintCheckDependencies") as String?)?.toBoolean() ?: true
        xmlReport = true
    }
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
        buildConfigField("String", "BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "API_ENV", "\"$apiEnv\"")
        manifestPlaceholders["KAKAO_APP_KEY"] = getApiKey("kakao.app.key")
        applicationId = "com.umc.product"
        minSdk = 24
        targetSdk = 36
        // CI 는 VERSION_CODE 를 주입한다. Play 는 동일 versionCode 재업로드를 거부한다.
        versionCode = (System.getenv("VERSION_CODE") ?: "21").toInt()
        // 릴리스 버전의 단일 출처는 git 태그다. CI 가 VERSION_NAME 을 주입한다.
        //   main 은 v* 태그를 푸시할 때만 프로덕션 배포 (예: v3.1.0 -> "3.1.0")
        //   develop-compose 는 "<최신태그>-dev.<실행번호>" (예: "3.0.0-dev.12")
        // 아래 literal 은 로컬 빌드 기본값일 뿐이다.
        versionName = System.getenv("VERSION_NAME") ?: "3.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "../umc_release_key.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: getApiKey("keystore.password")
            keyAlias = System.getenv("KEY_ALIAS") ?: getApiKey("key.alias")
            keyPassword = System.getenv("KEY_PASSWORD") ?: getApiKey("key.password")
        }
    }

    buildTypes {
        // Macrobenchmark 전용 빌드타입 — 릴리즈에 준하되 프로파일 수집이 가능하도록 debuggable
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            // R8 코드 축소·난독화. 직렬화 경계는 proguard-rules.pro 의 keep 규칙이 지킨다.
            isMinifyEnabled = true
            isShrinkResources = true
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
    lint {
        disable += "Instantiatable"
    }
    buildFeatures {
        dataBinding = true
        buildConfig = true
    }
}


dependencies {
    // 프로젝트 자체 Lint 규칙(단발성 이벤트 유실·수명주기 미고려 수집·요청 경로 블로킹)
    lintChecks(project(":lint-rules"))
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

    //opencsv
    implementation(libs.opencsv)

    //firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    //coli (이미지)
    implementation(libs.coil)

    // Play In-App Update
    implementation(libs.play.update)
    implementation(libs.play.update.ktx)

    //naver maps
    implementation(libs.naver.maps.sdk)
    implementation(libs.naver.maps.compose)
    implementation(libs.googleplay.services.location)

    // Android 16 16KB page-size compatible native path implementation
    implementation(libs.androidx.graphics.path)
}

fun getApiKey(propertyKey: String): String {
    return gradleLocalProperties(rootDir, providers).getProperty(propertyKey) ?: ""
}

