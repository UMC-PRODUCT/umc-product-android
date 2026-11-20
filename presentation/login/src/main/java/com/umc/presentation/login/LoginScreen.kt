package com.umc.presentation.login

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import com.umc.component.base.CollectUiEvents
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.common.model.AuthError
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.umc.component.component.UText
import com.umc.component.component.UToastData
import com.umc.component.component.UToastHost
import com.umc.component.component.UToastState
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey50
import com.umc.component.theme.grey200
import com.umc.component.theme.grey500
import com.umc.component.theme.grey900
import com.umc.component.theme.main600
import com.umc.component.theme.mint100
import com.umc.component.theme.teal600
import com.umc.component.util.ULog
import com.umc.domain.model.enums.LoginType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.umc.component.R

// UMC 카카오톡 채널 문의 주소 (SignUpFailScreen 과 동일)
private const val KAKAO_INQUIRY_URL = "https://pf.kakao.com/_MDxhqX/chat"

@Composable
fun LoginRoute(
    viewModel: LoginViewModel = hiltViewModel(),
    navigateToSignUp: (String) -> Unit = {},
    navigateToEmailLogin: () -> Unit = {},
    navigateToMain: () -> Unit = {},
    navigateToInputCode: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var toastData by remember { mutableStateOf<UToastData?>(null) }

    CollectUiEvents(viewModel.uiEvent) { event ->
            when (event) {
                is LoginEvent.MoveToSignUpEvent -> navigateToSignUp(event.oAuthToken)
                is LoginEvent.MoveToMainEvent -> navigateToMain()
                is LoginEvent.MoveToInputCodeEvent -> navigateToInputCode()
                is LoginEvent.ShowErrorToast ->
                    // 서버가 빈 메시지를 주면 빈 토스트가 뜨지 않게 공용 문구로 대신한다
                    toastData = UToastData(
                        event.message.ifBlank { AppStrings.COMMON_REQUEST_FAILED },
                        UToastState.ERROR,
                    )
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LoginScreen(
            onClickKakaoLogin = {
                signInKakao(
                    context = context,
                    onLoginSuccess = { token -> viewModel.login(token, LoginType.KAKAO) },
                    onFailure = { message -> toastData = UToastData(message, UToastState.ERROR) }
                )
            },
            onClickGoogleLogin = {
                signInGoogle(
                    scope = scope,
                    context = context,
                    onLoginSuccess = { token -> viewModel.login(token, LoginType.GOOGLE) },
                    onFailure = { message -> toastData = UToastData(message, UToastState.ERROR) }
                )
            },
            onClickEmailLogin = navigateToEmailLogin,
            onClickHelpCenter = {
                if (!context.openExternalUrl(KAKAO_INQUIRY_URL)) {
                    toastData = UToastData(AppStrings.COMMON_LINK_OPEN_FAIL, UToastState.ERROR)
                }
            },
        )

        UToastHost(
            data = toastData,
            onDismiss = { toastData = null },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
        )
    }
}

@Composable
fun LoginScreen(
    onClickKakaoLogin: () -> Unit = {},
    onClickGoogleLogin: () -> Unit = {},
    onClickEmailLogin: () -> Unit = {},
    onClickHelpCenter: () -> Unit = {},
) {
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationStarted = true
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(grey50()),
    ) {
        // ic_logo + 22dp spacer + 타이틀/서브타이틀 높이 추정값
        val logoBlockHeight = 120.dp
        val centerOffset = (maxHeight - logoBlockHeight) / 2

        val logoTopOffset by animateDpAsState(
            targetValue = if (animationStarted) 210.dp else centerOffset,
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

            Spacer(modifier = Modifier.height(16.dp))

            UText(
                text = AppStrings.LOGIN_TITLE,
                style = UmcTypographyTokens.HeadlineBold,
                color = grey900(),
            )

            Spacer(modifier = Modifier.height(4.dp))

            UText(
                text = AppStrings.LOGIN_SUBTITLE,
                style = UmcTypographyTokens.Callout,
                color = grey500(),
            )

            Column(
                modifier = Modifier.alpha(contentAlpha),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(104.dp))

                // 버튼 문구가 이미지 안에 그려져 있어 스크린리더용 설명과 버튼 역할을 따로 붙인다
                Image(
                    painter = painterResource(id = R.drawable.ic_kakao_login),
                    contentDescription = AppStrings.LOGIN_KAKAO_BUTTON,
                    modifier = Modifier.clickable(role = Role.Button) {
                        onClickKakaoLogin()
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.ic_google_login),
                    contentDescription = AppStrings.LOGIN_GOOGLE_BUTTON,
                    modifier = Modifier.clickable(role = Role.Button) {
                        onClickGoogleLogin()
                    },
                )

                Spacer(modifier = Modifier.height(32.dp))

                HorizontalDivider(
                    // 위 카카오·구글 버튼(폭 350dp 벡터 이미지)과 같은 폭 규칙을 따른다. 창이 더 좁으면 창 폭에 맞춰 줄어든다.
                    modifier = Modifier
                        .widthIn(max = 350.dp)
                        .fillMaxWidth(),
                    thickness = 1.dp,
                    color = grey200(),
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .widthIn(max = 350.dp)
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(mint100())
                        .clickable {
                            onClickEmailLogin()
                        },
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_email),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 56.dp),
                    )

                    UText(
                        text = AppStrings.LOGIN_UMC_ACCOUNT,
                        style = UmcTypographyTokens.HeadlineBold,
                        color = teal600(),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                UText(
                    text = AppStrings.LOGIN_HELP_QNA,
                    style = UmcTypographyTokens.Footnote,
                    color = grey500(),
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    UText(
                        text = AppStrings.LOGIN_HELP_CENTER,
                        style = UmcTypographyTokens.Footnote,
                        color = main600(),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onClickHelpCenter() },
                    )

                    UText(
                        text = AppStrings.LOGIN_HELP_SUFFIX,
                        style = UmcTypographyTokens.Footnote,
                        color = grey500(),
                    )
                }
            }
        }
    }
}

private fun signInKakao(
    context : Context,
    onLoginSuccess : (String) -> Unit,
    onFailure : (String) -> Unit
) {
    if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
        signInKakaoApp(context, onLoginSuccess, onFailure)
    } else {
        signInKakaoEmail(context, onLoginSuccess, onFailure)
    }
}

// 사용자가 로그인을 직접 취소한 경우인지 판별한다.
// 동의 화면에서 취소를 누르면 ClientError 가 아니라 AuthError(AccessDenied)로 오므로 함께 본다
private fun Throwable.isKakaoCancelled(): Boolean =
    (this is ClientError && reason == ClientErrorCause.Cancelled) ||
        (this is AuthError && reason == AuthErrorCause.AccessDenied)

// 카카오 로그인(앱으로)
private fun signInKakaoApp(
    context : Context,
    onLoginSuccess : (String) -> Unit,
    onFailure : (String) -> Unit
) {
    UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
        when {
            token != null -> onLoginSuccess(token.accessToken)
            error == null -> Unit
            // 사용자가 직접 취소한 경우는 안내도, 카카오계정 로그인 전환도 하지 않는다
            error.isKakaoCancelled() -> Unit
            // 카카오톡이 로그인되지 않았거나 연결에 실패하면 카카오계정 로그인으로 넘어간다(카카오 SDK 권장 흐름)
            else -> {
                ULog.d("카카오톡 로그인 실패: ${error::class.simpleName} ${error.message}")
                signInKakaoEmail(context, onLoginSuccess, onFailure)
            }
        }
    }
}

// 카카오 로그인(웹으로)
private fun signInKakaoEmail(
    context : Context,
    onLoginSuccess : (String) -> Unit,
    onFailure : (String) -> Unit
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

/**
 * 외부 링크를 연다. 링크를 열 앱(브라우저 등)이 없으면 앱이 종료되지 않게 false 를 돌려주고,
 * 안내는 호출한 화면이 한다
 */
private fun Context.openExternalUrl(url: String): Boolean =
    runCatching { startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
        .onFailure { ULog.d("외부 링크 열기 실패: ${it::class.simpleName}") }
        .isSuccess

// 구글 로그인
private fun signInGoogle(
    scope : CoroutineScope,
    context : Context,
    onLoginSuccess : (String) -> Unit,
    onFailure : (String) -> Unit
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

            handleSignIn(result, onLoginSuccess, onFailure)

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

private fun handleSignIn(
    result: GetCredentialResponse,
    onLoginSuccess : (String) -> Unit,
    onFailure : (String) -> Unit
) {
    val credential = result.credential

    // 반환된 자격 증명이 Google ID 토큰인지 확인
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

        try {
            // 서버에는 ID 토큰을 보낸다. aud 가 GOOGLE_LOGIN_KEY(Web 클라이언트) 하나로 고정돼서
            // 서명 키(SHA-1)를 추가해도 서버 허용 목록을 고칠 필요가 없다.
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

// 버튼 넷이 같은 폭으로 줄어드는지 350dp 보다 좁은 창에서도 본다.
@PreviewScreenSizes
@Preview(name = "좁은 폰(320dp)", widthDp = 320, heightDp = 640, showBackground = true)
@Composable
internal fun LoginScreenPreview() {
    LoginScreen()
}
