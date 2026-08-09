package com.umc.domain.model.enums

/**
 * OAuth 로그인 시 디바이스 유형 트래픽 분석을 위해 서버로 전달하는 클라이언트 타입.
 * 서버가 발급하는 Access Token claim 에 포함되어 이후 API 호출 통계 라벨로 사용된다.
 * 본 앱은 Android 네이티브이므로 항상 ANDROID 를 사용한다.
 */
enum class ClientType { ANDROID, IOS, WEB }
