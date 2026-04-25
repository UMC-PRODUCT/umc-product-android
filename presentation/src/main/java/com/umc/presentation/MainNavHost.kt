package com.umc.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.umc.presentation.login.LoginRoute
import com.umc.presentation.signup.SignUpRoute
import com.umc.presentation.splash.SplashRoute

@Composable
fun MainNavHost(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier.fillMaxSize(),
        navController = navHostController,
        startDestination = MainDestination.SignUp(""),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<MainDestination.Splash> {
            SplashRoute(
                navigateToLogin = { navHostController.navigate(MainDestination.Login) }
            )
        }

        composable<MainDestination.Login> {
            LoginRoute(
                navigateToSignUp = { oAuthToken ->
                    navHostController.navigate(MainDestination.SignUp(oAuthToken))
                }
            )
        }

        composable<MainDestination.SignUp> { backStackEntry ->
            val destination = backStackEntry.toRoute<MainDestination.SignUp>()
            SignUpRoute(
                oAuthVerificationToken = destination.oAuthVerificationToken,
                navigateToBack = { navHostController.popBackStack() },
            )
        }
    }
}