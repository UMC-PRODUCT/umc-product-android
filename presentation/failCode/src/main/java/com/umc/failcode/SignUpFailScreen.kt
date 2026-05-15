package com.umc.failcode

import android.app.Activity
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
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.danger500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary500
import com.umc.component.theme.primary700
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.tasks.await

@Composable
fun SignUpFailRoute(
    viewModel: SignUpFailViewModel = hiltViewModel(),
    navigateToCode: () -> Unit = {},
    navigateToLogin: () -> Unit = {},
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val googleToken = runCatching {
            val authorizationRequest = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(Scopes.PROFILE), Scope(Scopes.EMAIL)))
                .build()
            val result = Identity.getAuthorizationClient(context as Activity)
                .authorize(authorizationRequest)
                .await()
            result.accessToken ?: ""
        }.getOrDefault("")
        viewModel.setGoogleToken(googleToken)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                SignUpFailEvent.MoveToBack -> (context as? Activity)?.finish()
                SignUpFailEvent.MoveToHomePage -> {
                    val intent = Intent(Intent.ACTION_VIEW, "https://umc.it.kr".toUri())
                    context.startActivity(intent)
                }
                SignUpFailEvent.MoveToCode -> navigateToCode()
                SignUpFailEvent.MoveToKakaoInquiry -> {
                    val intent = Intent(Intent.ACTION_VIEW, "https://pf.kakao.com/_MDxhqX/chat".toUri())
                    context.startActivity(intent)
                }
                SignUpFailEvent.MoveToLogin -> navigateToLogin()
                SignUpFailEvent.ShowDeleteUserDialog -> { /* dialog state is managed in SignUpFailScreen */ }
            }
        }
    }

    BackHandler { (context as? Activity)?.finish() }

    SignUpFailScreen(
        onClickBack = viewModel::onClickBack,
        onClickNext = viewModel::onClickNext,
        onClickHomePage = viewModel::onClickHomePage,
        onClickLogout = viewModel::onClickLogout,
        onClickKakaoInquiry = viewModel::onClickKakaoInquiry,
        onDeleteUserConfirm = viewModel::deleteUser,
    )
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral000()),
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
            color = neutral800(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        UText(
            text = AppStrings.SIGN_UP_FAIL_CONTENT,
            style = UmcTypographyTokens.Subheadline,
            color = neutral600(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            UText(
                text = AppStrings.SIGN_UP_FAIL_LOGOUT,
                style = UmcTypographyTokens.Footnote.copy(textDecoration = TextDecoration.Underline),
                color = neutral600(),
                modifier = Modifier.clickable { onClickLogout() },
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .width(1.dp)
                    .height(12.dp)
                    .background(neutral400())
            )

            UText(
                text = AppStrings.SIGN_UP_FAIL_DELETE_USER,
                style = UmcTypographyTokens.Footnote.copy(textDecoration = TextDecoration.Underline),
                color = danger500(),
                modifier = Modifier.clickable { showDeleteDialog = true },
            )
        }

        Spacer(modifier = Modifier.height(80.dp))

        UButton(
            text = AppStrings.SIGN_UP_FAIL_PREV_USER,
            onClick = onClickNext,
            backgroundColor = primary500(),
            pressedColor = primary700(),
            textColor = neutral000(),
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
            backgroundColor = neutral000(),
            textColor = neutral800(),
            textStyle = UmcTypographyTokens.HeadlineBold,
            borderWidth = 1.dp,
            borderColor = neutral300(),
            contentPadding = PaddingValues(vertical = 15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        UText(
            text = AppStrings.SIGN_UP_FAIL_QA_EMAIL,
            style = UmcTypographyTokens.Footnote.copy(textDecoration = TextDecoration.Underline),
            color = neutral600(),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .clickable { onClickKakaoInquiry() },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpFailScreenPreview() {
    SignUpFailScreen()
}
