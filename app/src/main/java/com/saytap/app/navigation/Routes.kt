package com.saytap.app.navigation

import kotlinx.serialization.Serializable

//rutas de navegacion con kotlin serialization

@Serializable
object Login

@Serializable
object Registro

@Serializable
object Recuperar

@Serializable
data class Bienvenida(val nombre: String)

