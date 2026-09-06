package com.umc.product.base

import com.umc.product.BuildConfig

/**
 * 서버 주소는 빌드 시점에 결정된다. (app/build.gradle.kts 의 apiEnv 스위치)
 *
 * - main 브랜치 / `-PapiEnv=prod`(기본) → https://api.university.neordinary.com/
 * - develop-compose 브랜치 / `-PapiEnv=dev` → https://dev.api.university.neordinary.com/
 *
 * 로컬에서 dev 서버로 붙으려면 `./gradlew :app:assembleDebug -PapiEnv=dev` 로 빌드한다.
 */
val BASE_URL: String = BuildConfig.BASE_URL
