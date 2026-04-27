package com.umc.presentation.login

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.datatransport.runtime.BuildConfig
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.user.UserApiClient
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral600
import com.umc.component.util.ULog
import com.umc.domain.model.enums.LoginType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.umc.component.R

@Composable
fun LoginRoute(
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest {
            // TODO 이벤트 처리
        }
    }

    LoginScreen(
        onClickKakaoLogin = {
            signInKakao(
                context = context,
                onLoginSuccess = { token -> viewModel.login(token, LoginType.KAKAO) }
            )
        },
        onClickGoogleLogin = {
            signInGoogle(
                scope = scope,
                context = context,
                onLoginSuccess = { token -> viewModel.login(token, LoginType.GOOGLE) }
            )
        }
    )
}

@Composable
fun LoginScreen(
    onClickKakaoLogin: () -> Unit = {},
    onClickGoogleLogin: () -> Unit = {},
) {
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationStarted = true
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral000()),
    ) {
        // ic_logo + 22dp spacer + ic_logo_text 높이 추정값
        val logoBlockHeight = 100.dp
        val centerOffset = (maxHeight - logoBlockHeight) / 2

        val logoTopOffset by animateDpAsState(
            targetValue = if (animationStarted) 248.dp else centerOffset,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            label = "logoTopOffset",
        )

        val contentAlpha by animateFloatAsState(
            targetValue = if (animationStarted) 1f else 0f,
            animationSpec = tween(durationMillis = 400, delayMillis = 400),
            label = "contentAlpha",
        )

        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(logoTopOffset))

            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.height(22.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_logo_text),
                contentDescription = null,
            )

            Column(
                modifier = Modifier.alpha(contentAlpha),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                UText(
                    text = AppStrings.LOGIN_TITLE,
                    style = UmcTypographyTokens.Headline,
                    color = neutral600(),
                )

                Spacer(modifier = Modifier.height(145.dp))

                Image(
                    painter = painterResource(id = com.umc.component.R.drawable.ic_kakao_login),
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        onClickKakaoLogin()
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = com.umc.component.R.drawable.ic_google_login),
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        onClickGoogleLogin()
                    },
                )
            }
        }
    }
}

private fun signInKakao(
    context : Context,
    onLoginSuccess : (String) -> Unit
) {
    if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
        signInKakaoApp(context, onLoginSuccess)
    } else {
        signInKakaoEmail(context, onLoginSuccess)
    }
}

// 카카오 로그인(앱으로)
private fun signInKakaoApp(
    context : Context,
    onLoginSuccess : (String) -> Unit
) {
    UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
        token?.let {
            onLoginSuccess(token.accessToken)
        }
    }
}

// 카카오 로그인(웹으로)
private fun signInKakaoEmail(
    context : Context,
    onLoginSuccess : (String) -> Unit
) {
    UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
        token?.let {
            onLoginSuccess(token.accessToken)
        }
    }
}

// 구글 로그인
private fun signInGoogle(
    scope : CoroutineScope,
    context : Context,
    onLoginSuccess : (String) -> Unit
) {
    scope.launch {
        /**인식 X**/

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

            handleSignIn(scope, context, result, onLoginSuccess)

        } catch (e: GetCredentialException) {
            ULog.d("Google 로그인 실패: ${e.message}")
        }


    }

}

private fun handleSignIn(
    scope: CoroutineScope,
    context: Context,
    result: GetCredentialResponse,
    onLoginSuccess : (String) -> Unit
) {
    val credential = result.credential

    // 반환된 자격 증명이 Google ID 토큰인지 확인
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

        try {
            // Access Token도 함께 요청
            scope.launch {
                try {
                    val accessToken = requestAccessToken(context)
                    ULog.d("Google Access Token: $accessToken")
                    onLoginSuccess(accessToken)
                } catch (e: Exception) {
                    ULog.d("Access Token 획득 실패: ${e.message}")
                }
            }

        } catch (e: GoogleIdTokenParsingException) {
            ULog.d("유효하지 않은 Google ID 토큰 $e")
        }
    } else {
        ULog.d("예상치 못한 자격 증명 유형")
    }
}

private suspend fun requestAccessToken(
    context: Context,
): String {
    val authorizationRequest = AuthorizationRequest.builder()
        .setRequestedScopes(
            listOf(Scope(Scopes.PROFILE), Scope(Scopes.EMAIL))
        )
        .build()

    val authorizationResult = Identity.getAuthorizationClient(context)
        .authorize(authorizationRequest)
        .await()

    return authorizationResult.accessToken
        ?: throw IllegalStateException("Access Token을 받을 수 없습니다")
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreen()
}
