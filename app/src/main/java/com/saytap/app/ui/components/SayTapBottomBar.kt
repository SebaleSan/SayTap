package com.saytap.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.saytap.app.navigation.Bienvenida
import com.saytap.app.navigation.Escribir
import com.saytap.app.navigation.Hablar
import com.saytap.app.navigation.Ajustes

@Composable
fun SayTapBottomBar(navController: NavController) {
    val destino = navController.currentBackStackEntryAsState().value?.destination

    NavigationBar {
        NavigationBarItem(
            selected = destino?.hasRoute<Bienvenida>() == true,
            onClick = { navController.popBackStack<Bienvenida>(inclusive = false) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = destino?.hasRoute<Escribir>() == true,
            onClick = {
                navController.navigate(Escribir) {
                    popUpTo<Bienvenida>()
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Filled.Keyboard, contentDescription = null) },
            label = { Text("Escribir") }
        )
        NavigationBarItem(
            selected = destino?.hasRoute<Hablar>() == true,
            onClick = {
                navController.navigate(Hablar) {
                    popUpTo<Bienvenida>()
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Filled.Mic, contentDescription = null) },
            label = { Text("Hablar") }
        )
        NavigationBarItem(
            selected = destino?.hasRoute<Ajustes>() == true,
            onClick = {
                navController.navigate(Ajustes) {
                    popUpTo<Bienvenida>()
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text("Ajustes") }
        )
    }
}