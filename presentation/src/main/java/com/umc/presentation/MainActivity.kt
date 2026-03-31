package com.umc.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.umc.presentation.ui.theme.UmcTheme
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
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            val navController = rememberNavController()
            MainNavHost(
                navHostController = navController,
            )
        }
    }
}
