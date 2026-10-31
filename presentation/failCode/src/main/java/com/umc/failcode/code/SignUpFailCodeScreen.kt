package com.umc.failcode.code

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UDialog
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.component.UToastData
import com.umc.component.component.UToastHost
import com.umc.component.component.UToastState
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey50
import com.umc.component.theme.grey100
import com.umc.component.theme.grey300
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.main500
import com.umc.component.theme.main700
import kotlinx.coroutines.flow.collectLatest
import com.umc.component.base.CollectUiEvents
import android.content.Intent
import com.umc.failcode.KAKAO_INQUIRY_URL
import com.umc.failcode.SignUpFailAccountLinks
import com.umc.failcode.SignUpFailEvent
import com.umc.failcode.SignUpFailInquiryLink
import com.umc.failcode.SignUpFailViewModel
import com.umc.failcode.requestGoogleAccessToken

@Composable
fun SignUpFailCodeRoute(
    viewModel: SignUpFailCodeViewModel = hiltViewModel(),
    navigateToBack: () -> Unit = {},
    navigateToHome: () -> Unit = {},
    navigateToLogin: () -> Unit = {},
    accountViewModel: SignUpFailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var toastData by remember { mutableStateOf<UToastData?>(null) }

    // 회원탈퇴 API 에 쓸 Google 토큰은 인증 대기 화면과 같은 방식으로 획득한다
    LaunchedEffect(Unit) {
        accountViewModel.setGoogleToken(requestGoogleAccessToken(context))
    }

    // 로그아웃·회원탈퇴·카카오 문의는 인증 대기 화면과 같은 ViewModel 을 재사용한다
    CollectUiEvents(accountViewModel.uiEvent) { event ->
        when (event) {
            SignUpFailEvent.MoveToLogin -> navigateToLogin()
            SignUpFailEvent.MoveToKakaoInquiry -> {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, KAKAO_INQUIRY_URL.toUri()))
                }
            }
            is SignUpFailEvent.ShowErrorToast ->
                toastData = UToastData(event.message, UToastState.ERROR)
            SignUpFailEvent.MoveToBack,
            SignUpFailEvent.MoveToHomePage,
            SignUpFailEvent.MoveToCode -> Unit
        }
    }

    CollectUiEvents(viewModel.uiEvent) { event ->
        when (event) {
            SignUpFailCodeEvent.MoveToBack -> navigateToBack()
            SignUpFailCodeEvent.MoveToHome -> navigateToHome()
            is SignUpFailCodeEvent.ShowErrorDialog -> errorMessage = event.message
        }
    }

    errorMessage?.let { message ->
        UDialog(
            title = message,
            confirmText = AppStrings.COMMON_CONFIRM,
            onDismissRequest = { errorMessage = null },
            onConfirm = { errorMessage = null },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SignUpFailCodeScreen(
            uiState = uiState,
            onClickBack = viewModel::onClickBack,
            onCodeChanged = viewModel::onCodeChanged,
            onClickRegister = viewModel::register,
            onClickLogout = accountViewModel::onClickLogout,
            onDeleteUserConfirm = accountViewModel::deleteUser,
            onClickKakaoInquiry = accountViewModel::onClickKakaoInquiry,
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
fun SignUpFailCodeScreen(
    uiState: SignUpFailCodeState = SignUpFailCodeState(),
    onClickBack: () -> Unit = {},
    onCodeChanged: (String) -> Unit = {},
    onClickRegister: () -> Unit = {},
    onClickLogout: () -> Unit = {},
    onDeleteUserConfirm: () -> Unit = {},
    onClickKakaoInquiry: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey50()),
    ) {
        Icon(
            modifier = Modifier
                .padding(start = 4.dp, top = 8.dp)
                .padding(12.dp)
                .clickable { onClickBack() },
            painter = painterResource(R.drawable.ic_back),
            contentDescription = null,
            tint = Color.Unspecified,
        )

        UText(
            text = AppStrings.SIGN_UP_FAIL_PREV_USER,
            style = UmcTypographyTokens.Title1Bold,
            color = grey800(),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
        )

        UText(
            text = AppStrings.SIGN_UP_FAIL_PREV_USER_HINT,
            style = UmcTypographyTokens.Body,
            color = grey600(),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
        )

        UText(
            text = AppStrings.SIGN_UP_FAIL_CODE_LABEL,
            style = UmcTypographyTokens.HeadlineBold,
            color = grey800(),
            modifier = Modifier.padding(start = 24.dp, top = 80.dp),
        )

        UTextField(
            value = uiState.code,
            onValueChange = { if (it.length <= 6) onCodeChanged(it) },
            placeholder = AppStrings.SIGN_UP_FAIL_CODE_PLACEHOLDER,
            textStyle = UmcTypographyTokens.Body,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        UButton(
            text = AppStrings.SIGN_UP_FAIL_CODE_SUBMIT,
            onClick = onClickRegister,
            enabled = !uiState.isSubmitting,
            backgroundColor = if (uiState.isSubmitting) grey100() else main500(),
            pressedColor = main700(),
            textColor = if (uiState.isSubmitting) grey300() else grey50(),
            textStyle = UmcTypographyTokens.HeadlineBold,
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
        )

        // 인증 대기 사용자가 계정을 정리하거나 문의할 수 있는 경로
        SignUpFailAccountLinks(
            onClickLogout = onClickLogout,
            onDeleteUserConfirm = onDeleteUserConfirm,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(12.dp))

        SignUpFailInquiryLink(
            onClick = onClickKakaoInquiry,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpFailCodeScreenPreview() {
    SignUpFailCodeScreen()
}

@Preview(showBackground = true)
@Composable
private fun SignUpFailCodeScreenFilledPreview() {
    SignUpFailCodeScreen(uiState = SignUpFailCodeState(code = "123456"))
}

@Preview(showBackground = true)
@Composable
private fun SignUpFailCodeScreenSubmittingPreview() {
    SignUpFailCodeScreen(uiState = SignUpFailCodeState(code = "123456", isSubmitting = true))
}
