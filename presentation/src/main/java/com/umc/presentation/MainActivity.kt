package com.umc.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.presentation.ui.strings.AppStrings
import com.umc.presentation.ui.theme.UmcTheme
import com.umc.presentation.ui.theme.neutral000
import com.umc.presentation.ui.theme.neutral700
import com.umc.presentation.ui.theme.neutral900
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UmcApp()
        }
    }
}

@Composable
private fun UmcApp() {
    UmcTheme {
        val backgroundColor = neutral000()
        val titleColor = neutral900()
        val descriptionColor = neutral700()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = backgroundColor,
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = backgroundColor,
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                    ) {
                        Text(
                            text = AppStrings.COMPOSE_ROOT_TITLE,
                            color = titleColor,
                        )
                        Text(
                            text = AppStrings.COMPOSE_ROOT_DESCRIPTION,
                            color = descriptionColor,
                        )
                    }
                }
            }
        }
    }
}
