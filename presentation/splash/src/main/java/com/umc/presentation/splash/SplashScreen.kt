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
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SplashRoute(
    viewModel: SplashViewModel = hiltViewModel(),
) {
    // TODO uiState 처리는 생명주기에 맞춰서 아래 코드처럼 사용
    // val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest {
            // TODO 이벤트 처리는 여기서
        }
    }

    SplashScreen(
        // uiState나 event 등 StateHoisting 사용
    )

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
                painter = painterResource(id = com.umc.component.R.drawable.ic_logo),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.height(22.dp))

            Image(
                painter = painterResource(id = com.umc.component.R.drawable.ic_logo_text),
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
