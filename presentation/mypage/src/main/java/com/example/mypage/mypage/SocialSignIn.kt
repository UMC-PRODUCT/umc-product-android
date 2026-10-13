package com.example.mypage.mypage

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.mypage.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.umc.component.theme.AppStrings
import com.umc.component.util.ULog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 소셜 연동(카카오, 구글)에 필요한 토큰을 받아오는 함수 모음
 *
 * 로그인 화면(LoginScreen)의 토큰 획득 로직과 같다. 공용화는 후속 리팩터링에서 한다.
 */

// 카카오 로그인
internal fun signInKakao(
    context: Context,
    onLoginSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
        signInKakaoApp(context, onLoginSuccess, onFailure)
    } else {
        signInKakaoEmail(context, onLoginSuccess, onFailure)
    }
}

// 사용자가 로그인을 직접 취소한 경우인지 판별
private fun Throwable.isKakaoCancelled(): Boolean =
    this is ClientError && reason == ClientErrorCause.Cancelled

// 카카오 로그인(앱으로)
private fun signInKakaoApp(
    context: Context,
    onLoginSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
        when {
            token != null -> onLoginSuccess(token.accessToken)
            error == null -> Unit
            // 사용자가 직접 취소한 경우는 안내하지 않는다.
            error.isKakaoCancelled() -> Unit
            // 카카오톡 로그인이 취소 외의 이유로 실패하면 카카오계정 로그인으로 넘어간다(카카오 SDK 권장 흐름).
            else -> {
                ULog.d("카카오톡 로그인 실패: ${error::class.simpleName} ${error.message}")
                signInKakaoEmail(context, onLoginSuccess, onFailure)
            }
        }
    }
}

// 카카오 로그인(웹으로)
private fun signInKakaoEmail(
    context: Context,
    onLoginSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
        when {
            token != null -> onLoginSuccess(token.accessToken)
            error == null -> Unit
            error.isKakaoCancelled() -> Unit
            else -> {
                ULog.d("카카오계정 로그인 실패: ${error::class.simpleName} ${error.message}")
                onFailure(AppStrings.LOGIN_KAKAO_FAILED)
            }
        }
    }
}

// 구글 로그인
internal fun signInGoogle(
    scope: CoroutineScope,
    context: Context,
    onLoginSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    scope.launch {
        try {
            val googleSignInOption = GetSignInWithGoogleOption.Builder(
                BuildConfig.GOOGLE_LOGIN_KEY
            ).build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleSignInOption)
                .build()

            val result = CredentialManager.create(context).getCredential(
                request = request,
                context = context
            )

            handleGoogleSignIn(result, onLoginSuccess, onFailure)

        } catch (e: GetCredentialException) {
            ULog.d("Google 로그인 실패: ${e::class.simpleName} ${e.message}")
            when (e) {
                // 사용자가 계정 선택 시트를 직접 닫은 경우는 안내하지 않는다.
                is GetCredentialCancellationException -> Unit
                is NoCredentialException -> onFailure(AppStrings.LOGIN_GOOGLE_NO_ACCOUNT)
                else -> onFailure(AppStrings.LOGIN_GOOGLE_FAILED)
            }
        }
    }
}

private fun handleGoogleSignIn(
    result: GetCredentialResponse,
    onLoginSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val credential = result.credential

    // 반환된 자격 증명이 Google ID 토큰인지 확인
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

        try {
            // 서버에는 ID 토큰을 보낸다(로그인 화면과 동일).
            val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            onLoginSuccess(idToken)
        } catch (e: GoogleIdTokenParsingException) {
            ULog.d("유효하지 않은 Google ID 토큰 $e")
            onFailure(AppStrings.LOGIN_GOOGLE_FAILED)
        }
    } else {
        ULog.d("예상치 못한 자격 증명 유형")
        onFailure(AppStrings.LOGIN_GOOGLE_FAILED)
    }
}
