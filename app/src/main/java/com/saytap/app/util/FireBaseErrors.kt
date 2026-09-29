package com.saytap.app.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/**
 * traduce las excepciones tecnicas de Firebase (en ingles) a mensajes
 * claros en español para mostrar al usuario final, en vez de exponer
 * el texto tal cual que entrega el SDK.
 */
fun Throwable.mensajeAmigable(): String {
    return when (this) {
        is FirebaseNetworkException ->
            "No hay conexión a internet. Revisa tu red e intenta nuevamente."
        is FirebaseAuthWeakPasswordException ->
            "La contraseña es demasiado débil. Usa al menos 6 caracteres."
        is FirebaseAuthUserCollisionException ->
            "Ese correo ya está registrado. Intenta iniciar sesión."
        is FirebaseAuthInvalidCredentialsException ->
            "Correo o contraseña incorrectos."
        is FirebaseAuthException -> when (this.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El formato del correo no es válido."
            "ERROR_USER_NOT_FOUND" -> "Correo o contraseña incorrectos."
            "ERROR_WRONG_PASSWORD" -> "Correo o contraseña incorrectos."
            "ERROR_USER_DISABLED" -> "Esta cuenta fue deshabilitada. Contacta soporte."
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Espera un momento e intenta de nuevo."
            else -> "Ocurrió un error al procesar tu solicitud. Intenta nuevamente."
        }
        else -> "Ocurrió un error inesperado. Intenta nuevamente."
    }
}