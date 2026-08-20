package com.saytap.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.saytap.app.ui.screens.BienvenidaScreen
import com.saytap.app.ui.screens.RegistroScreen
import com.saytap.app.ui.screens.LoginScreen
import com.saytap.app.ui.screens.RecuperarPasswordScreen


@Composable
fun AppNavigation(
    textScale: Float,
    onTextScaleChange: (Float) -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                navController = navController,
                textScale = textScale,
                onTextScaleChange = onTextScaleChange
            )
        }

        composable(Routes.REGISTRO) {
            RegistroScreen(
                navController = navController,
                textScale = textScale,
                onTextScaleChange = onTextScaleChange
            )
        }

        composable(Routes.RECUPERAR) {
            RecuperarPasswordScreen(
                navController = navController,
                textScale = textScale,
                onTextScaleChange = onTextScaleChange
            )
        }

        composable(Routes.BIENVENIDA + "/{nombre}") { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            BienvenidaScreen(
                navController = navController,
                nombreUsuario = nombre,
                textScale = textScale,
                onTextScaleChange = onTextScaleChange
            )
        }


    }
}