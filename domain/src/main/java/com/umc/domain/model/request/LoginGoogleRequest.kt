package com.umc.domain.model.request

import com.umc.domain.model.enums.ClientType
import kotlinx.serialization.Serializable

/**
 * 구글 로그인 요청
 *
 * 서버에는 OIDC `idToken` 을 보낸다. Access Token 을 보내면 서버가 토큰의 `aud` 를 서명 키(SHA-1)별
 * Android OAuth 클라이언트 ID 로 검사하게 되어, 서명 키가 늘 때마다 서버 허용 목록까지 고쳐야 한다.
 * `idToken` 의 `aud` 는 `GOOGLE_LOGIN_KEY`(Web 클라이언트 ID) 하나로 고정된다.
 */
@Serializable
data class LoginGoogleRequest(
    val idToken: String,
    val clientType: ClientType = ClientType.ANDROID,
)
