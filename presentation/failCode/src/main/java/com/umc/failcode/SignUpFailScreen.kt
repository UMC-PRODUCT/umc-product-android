package com.umc.failcode

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UDialog
import com.umc.component.component.UText
import com.umc.component.component.UToastData
import com.umc.component.component.UToastHost
import com.umc.component.component.UToastState
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.red500
import com.umc.component.theme.grey000
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.indigo500
import com.umc.component.theme.indigo700
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.tasks.await
import com.umc.component.base.CollectUiEvents

@Composable
fun SignUpFailRoute(
    viewModel: SignUpFailViewModel = hiltViewModel(),
    navigateToCode: () -> Unit = {},
    navigateToLogin: () -> Unit = {},
) {
    val context = LocalContext.current
    var toastData by remember { mutableStateOf<UToastData?>(null) }

    LaunchedEffect(Unit) {
        viewModel.setGoogleToken(requestGoogleAccessToken(context))
    }

    CollectUiEvents(viewModel.uiEvent) { event ->
        when (event) {
            SignUpFailEvent.MoveToBack -> context.findActivity()?.finish()
            SignUpFailEvent.MoveToHomePage -> {
                val intent = Intent(Intent.ACTION_VIEW, "https://umc.it.kr".toUri())
                context.startActivity(intent)
            }
            SignUpFailEvent.MoveToCode -> navigateToCode()
            SignUpFailEvent.MoveToKakaoInquiry -> {
                val intent = Intent(Intent.ACTION_VIEW, KAKAO_INQUIRY_URL.toUri())
                context.startActivity(intent)
            }
            SignUpFailEvent.MoveToLogin -> navigateToLogin()
            is SignUpFailEvent.ShowErrorToast ->
                toastData = UToastData(event.message, UToastState.ERROR)
        }
    }

    BackHandler { context.findActivity()?.finish() }

    Box(modifier = Modifier.fillMaxSize()) {
        SignUpFailScreen(
            onClickBack = viewModel::onClickBack,
            onClickNext = viewModel::onClickNext,
            onClickHomePage = viewModel::onClickHomePage,
            onClickLogout = viewModel::onClickLogout,
            onClickKakaoInquiry = viewModel::onClickKakaoInquiry,
            onDeleteUserConfirm = viewModel::deleteUser,
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
fun SignUpFailScreen(
    onClickBack: () -> Unit = {},
    onClickNext: () -> Unit = {},
    onClickHomePage: () -> Unit = {},
    onClickLogout: () -> Unit = {},
    onClickKakaoInquiry: () -> Unit = {},
    onDeleteUserConfirm: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Icon(
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 4.dp)
                .padding(12.dp)
                .clickable { onClickBack() },
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = null,
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(125.dp))

        Icon(
            painter = painterResource(R.drawable.ic_error_large),
            contentDescription = null,
            tint = Color.Unspecified,
        )

        Spacer(modifier = Modifier.height(40.dp))

        UText(
            text = AppStrings.SIGN_UP_FAIL_TITLE,
            style = UmcTypographyTokens.Title2Bold,
            color = grey800(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        UText(
            text = AppStrings.SIGN_UP_FAIL_CONTENT,
            style = UmcTypographyTokens.Subheadline,
            color = grey600(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        SignUpFailAccountLinks(
            onClickLogout = onClickLogout,
            onDeleteUserConfirm = onDeleteUserConfirm,
        )

        Spacer(modifier = Modifier.height(80.dp))

        UButton(
            text = AppStrings.SIGN_UP_FAIL_PREV_USER,
            onClick = onClickNext,
            backgroundColor = indigo500(),
            pressedColor = indigo700(),
            textColor = grey000(),
            textStyle = UmcTypographyTokens.HeadlineBold,
            contentPadding = PaddingValues(vertical = 15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        UButton(
            text = AppStrings.SIGN_UP_FAIL_HOMEPAGE,
            onClick = onClickHomePage,
            backgroundColor = grey000(),
            textColor = grey800(),
            textStyle = UmcTypographyTokens.HeadlineBold,
            borderWidth = 1.dp,
            borderColor = grey300(),
            contentPadding = PaddingValues(vertical = 15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        SignUpFailInquiryLink(
            onClick = onClickKakaoInquiry,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

/** 카카오톡 채널 문의 URL */
internal const val KAKAO_INQUIRY_URL = "https://pf.kakao.com/_MDxhqX/chat"

/**
 * '로그아웃하기 | 회원탈퇴하기' 링크 행과 탈퇴 확인 다이얼로그.
 * 인증 대기 화면(SignUpFailScreen)과 코드 입력 화면(SignUpFailCodeScreen)이 함께 쓴다
 */
@Composable
internal fun SignUpFailAccountLinks(
    onClickLogout: () -> Unit,
    onDeleteUserConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        UDialog(
            title = AppStrings.SIGN_UP_FAIL_DELETE_USER_DIALOG_TITLE,
            content = AppStrings.SIGN_UP_FAIL_DELETE_USER_DIALOG_CONTENT,
            isTwoButton = true,
            positiveText = AppStrings.DELETE,
            negativeText = AppStrings.CANCEL,
            onPositive = {
                showDeleteDialog = false
                onDeleteUserConfirm()
            },
            onNegative = { showDeleteDialog = false },
            onDismissRequest = { showDeleteDialog = false },
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        UText(
            text = AppStrings.SIGN_UP_FAIL_LOGOUT,
            style = UmcTypographyTokens
                .Footnote.copy(textDecoration = TextDecoration.Underline),
            color = grey600(),
            modifier = Modifier.clickable {
                onClickLogout()
            }
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .width(1.dp)
                .height(12.dp)
                .background(grey400())
        )

        UText(
            text = AppStrings.SIGN_UP_FAIL_DELETE_USER,
            style = UmcTypographyTokens.Footnote
                .copy(textDecoration = TextDecoration.Underline),
            color = red500(),
            modifier = Modifier.clickable {
                showDeleteDialog = true
            }
        )
    }
}

/** 카카오톡 채널 문의 링크 */
@Composable
internal fun SignUpFailInquiryLink(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    UText(
        text = AppStrings.SIGN_UP_FAIL_QA_EMAIL,
        style = UmcTypographyTokens.Footnote.copy(textDecoration = TextDecoration.Underline),
        color = grey600(),
        modifier = modifier.clickable { onClick() },
    )
}

/**
 * 회원탈퇴 API 에 쓸 Google accessToken 을 Google Identity API 로 획득한다.
 * 실패하거나 Activity 를 찾지 못하면 빈 문자열을 반환한다
 */
internal suspend fun requestGoogleAccessToken(context: Context): String = runCatching {
    val activity = context.findActivity() ?: error("Activity를 찾을 수 없습니다.")
    val authorizationRequest = AuthorizationRequest.builder()
        .setRequestedScopes(listOf(Scope(Scopes.PROFILE), Scope(Scopes.EMAIL)))
        .build()
    val result = Identity.getAuthorizationClient(activity)
        .authorize(authorizationRequest)
        .await()
    result.accessToken ?: ""
}.getOrDefault("")

@Preview(showBackground = true)
@Composable
private fun SignUpFailScreenPreview() {
    SignUpFailScreen()
}

/**
 * LocalContext 는 ContextWrapper(예: Hilt 의 FragmentContextWrapper, ContextThemeWrapper)로
 * 감싸져 있을 수 있어 단순 `context as Activity` 는 ClassCastException 위험이 있다.
 * baseContext 체인을 거슬러 올라가며 실제 Activity 를 찾고, 없으면 null 을 반환한다.
 */
internal fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
