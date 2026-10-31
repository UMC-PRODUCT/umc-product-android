package com.umc.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey400
import com.umc.component.theme.grey800
import com.umc.component.theme.main500
import com.umc.component.theme.main600
import com.umc.component.theme.white
import com.umc.component.R
import kotlinx.coroutines.flow.collectLatest
import com.umc.component.base.CollectUiEvents

@Composable
fun SplashRoute(
    viewModel: SplashViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit,
    navigateToMain: () -> Unit = {},
    navigateToInputCode: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEvents(viewModel.uiEvent) { it ->
        when(it) {
            SplashEvent.MoveToLoginEvent -> navigateToLogin()
            SplashEvent.MoveToMainEvent -> navigateToMain()
            SplashEvent.MoveToInputCodeEvent -> navigateToInputCode()
        }
    }

    SplashScreen(
        isNetworkError = uiState.isNetworkError,
        onRetry = viewModel::retry,
    )

}

@Composable
fun SplashScreen(
    isNetworkError: Boolean = false,
    onRetry: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(white()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.height(22.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_logo_text),
                contentDescription = null,
            )

            if (isNetworkError) {
                Spacer(modifier = Modifier.height(48.dp))

                UText(
                    text = AppStrings.SPLASH_NETWORK_ERROR_TITLE,
                    color = grey800(),
                    style = UmcTypographyTokens.HeadlineBold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(10.dp))

                UText(
                    text = AppStrings.SPLASH_NETWORK_ERROR_DESCRIPTION,
                    color = grey400(),
                    style = UmcTypographyTokens.Subheadline,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                UButton(
                    text = AppStrings.SPLASH_RETRY,
                    onClick = onRetry,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .widthIn(max = 350.dp)
                        .fillMaxWidth()
                        .height(58.dp),
                    backgroundColor = main500(),
                    pressedColor = main600(),
                    textColor = white(),
                    textStyle = UmcTypographyTokens.HeadlineBold,
                    cornerRadius = 12.dp,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenNetworkErrorPreview() {
    SplashScreen(isNetworkError = true)
}
