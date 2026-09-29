
package com.saytap.app.util

import android.util.Patterns

/**
  Funciones de validacinn reutilizables para los formularios de la app.
 */

/** Valida que el texto tenga formato de correo electrónico válido. */
fun String.esCorreoValido(): Boolean {
    return this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this.trim()).matches()
}

/** Devuelve la lista de requisitos de contraseña que NO se cumplen */
fun String.requisitosFaltantesPassword(): List<String> {
    val faltantes = mutableListOf<String>()
    if (this.length < 8) faltantes.add("mínimo 8 caracteres")
    if (this.none { it.isUpperCase() }) faltantes.add("al menos una mayúscula")
    if (this.none { it.isDigit() }) faltantes.add("al menos un número")
    return faltantes
}

/** true si la contraseña cumple todos los requisitos mínimos. */
fun String.esPasswordValida(): Boolean = this.requisitosFaltantesPassword().isEmpty()