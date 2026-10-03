package com.saytap.app.data


data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val gradoAuditivo: String = "",
    val generoVoz: String = "",
    val vibracion: Boolean = true,
)