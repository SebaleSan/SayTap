package com.saytap.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.saytap.app.ui.screens.BienvenidaScreen
import com.saytap.app.ui.screens.LoginScreen
import com.saytap.app.ui.screens.RecuperarPasswordScreen
import com.saytap.app.ui.screens.RegistroScreen

@Composable
fun AppNavigation(
    textScale: Float,
    onTextScaleChange: (Float) -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Login) {

        composable<Login> {
            LoginScreen(
                navController = navController,
                textScale = textScale,
                onTextScaleChange = onTextScaleChange
            )
        }

        composable<Registro> {
            RegistroScreen(
                navController = navController,
                textScale = textScale,
                onTextScaleChange = onTextScaleChange
            )
        }

        composable<Recuperar> {
            RecuperarPasswordScreen(
                navController = navController,
                textScale = textScale,
                onTextScaleChange = onTextScaleChange
            )
        }
    //se recibe el dato de nombre para mostrarlo en la siguiente vista
        composable<Bienvenida> { backStackEntry ->
            val datos: Bienvenida = backStackEntry.toRoute()
            BienvenidaScreen(
                navController = navController,
                nombreUsuario = datos.nombre,
                textScale = textScale,
                onTextScaleChange = onTextScaleChange
            )
        }
    }
}