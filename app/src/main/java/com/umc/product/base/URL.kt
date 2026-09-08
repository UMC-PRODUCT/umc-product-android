package com.umc.product.base

import com.umc.product.BuildConfig

/**
 * 서버 주소는 빌드 변형(product flavor)으로 결정된다.
 *
 * - `prod` → https://api.university.neordinary.com/      (main / 프로덕션 트랙)
 * - `dev`  → https://api-dev.university.neordinary.com/  (develop-compose / 내부 테스트 트랙)
 *
 * 로컬에서는 Android Studio 좌측 Build Variants 패널에서
 * devDebug / prodDebug 를 골라 전환한다.
 */
val BASE_URL: String = BuildConfig.BASE_URL
