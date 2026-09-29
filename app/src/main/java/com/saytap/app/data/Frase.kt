package com.saytap.app.data

data class Frase(
    val id: String = "",
    val texto: String = "",
    val categoria: String = "", // "Saludos" | "Emergencia" | "Cotidiano"
    val vecesUsada: Int = 0
)