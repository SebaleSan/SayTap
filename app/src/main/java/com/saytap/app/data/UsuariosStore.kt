package com.saytap.app.data

import androidx.compose.runtime.mutableStateListOf

object UsuariosStore {

    val usuarios = mutableStateListOf(
        Usuario(
            nombre = "María Torres",
            correo = "maria.t@correo.com",
            contrasena = "clave1234",
            gradoAuditivo = "Moderada",
            generoVoz = "Voz femenina"
        ),
        Usuario(
            nombre = "Diego Fuentes",
            correo = "diego.f@correo.com",
            contrasena = "clave1234",
            gradoAuditivo = "Severa",
            generoVoz = "Voz masculina"
        ),
        Usuario(
            nombre = "Camila Reyes",
            correo = "camila.r@correo.com",
            contrasena = "clave1234",
            gradoAuditivo = "Leve",
            generoVoz = "Voz femenina"
        ),
        Usuario(
            nombre = "Ignacio Soto",
            correo = "ignacio.s@correo.com",
            contrasena = "clave1234",
            gradoAuditivo = "Moderada",
            generoVoz = "Voz masculina"
        ),
        Usuario(
            nombre = "Valentina Paz",
            correo = "valentina.p@correo.com",
            contrasena = "clave1234",
            gradoAuditivo = "Severa",
            generoVoz = "Voz femenina"
        )
    )

    fun existeCorreo(correo: String): Boolean =
        usuarios.any { it.correo.equals(correo.trim(), ignoreCase = true) }

    fun autenticar(correo: String, contrasena: String): Usuario? =
        usuarios.find {
            it.correo.equals(correo.trim(), ignoreCase = true) && it.contrasena == contrasena
        }
}