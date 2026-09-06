import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.android.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)
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
    kotlinOptions {
        jvmTarget = "11"
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
    return gradleLocalProperties(rootDir, providers).getProperty(propertyKey) ?: ""
}

