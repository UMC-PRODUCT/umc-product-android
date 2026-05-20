package com.umc.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.component.theme.neutral000
import com.umc.component.R
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SplashRoute(
    viewModel: SplashViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit,
    navigateToMain: () -> Unit = {},
    navigateToInputCode: () -> Unit = {},
) {
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest {
            when(it) {
                SplashEvent.MoveToLoginEvent -> navigateToLogin()
                SplashEvent.MoveToMainEvent -> navigateToMain()
                SplashEvent.MoveToInputCodeEvent -> navigateToInputCode()
            }
        }
    }

    SplashScreen()

}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral000()),
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
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}
