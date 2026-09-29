package com.saytap.app.data

import kotlinx.coroutines.tasks.await

object FraseRepository {

    private fun frasesRef(uid: String) = sayTapDatabase.reference.child("frases").child(uid)

    /**
     * Carga las 9 frases base para un usuario recién registrado.
     * Se llama una sola vez, inmediatamente después de crear la cuenta.
     */
    suspend fun sembrarFrasesPredeterminadas(uid: String): Result<Unit> {
        return try {
            val frasesBase = listOf(
                Frase(texto = "Hola, ¿cómo estás?", categoria = "Saludos"),
                Frase(texto = "Mucho gusto", categoria = "Saludos"),
                Frase(texto = "Nos vemos luego", categoria = "Saludos"),
                Frase(texto = "Necesito ayuda, por favor", categoria = "Emergencia"),
                Frase(texto = "Llamen a una ambulancia", categoria = "Emergencia"),
                Frase(texto = "Tengo dificultad para escuchar, ¿puede escribirme en su celular?", categoria = "Emergencia"),
                Frase(texto = "Sí", categoria = "Cotidiano"),
                Frase(texto = "No", categoria = "Cotidiano"),
                Frase(texto = "Un momento, por favor", categoria = "Cotidiano")
            )
            frasesBase.forEach { base ->
                val id = frasesRef(uid).push().key
                    ?: throw IllegalStateException("No se pudo generar id de frase")
                frasesRef(uid).child(id).setValue(base.copy(id = id)).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Lee todas las frases del usuario (predeterminadas + las que haya creado). */
    suspend fun obtenerFrases(uid: String): Result<List<Frase>> {
        return try {
            val snapshot = frasesRef(uid).get().await()
            val frases = snapshot.children.mapNotNull { it.getValue(Frase::class.java) }
            Result.success(frases)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Crea una frase nueva del usuario. */
    suspend fun crearFrase(uid: String, texto: String, categoria: String): Result<Frase> {
        return try {
            val id = frasesRef(uid).push().key
                ?: throw IllegalStateException("No se pudo generar id de frase")
            val frase = Frase(id = id, texto = texto, categoria = categoria)
            frasesRef(uid).child(id).setValue(frase).await()
            Result.success(frase)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Actualiza el texto/categoría de una frase existente. */
    suspend fun actualizarFrase(uid: String, frase: Frase): Result<Unit> {
        return try {
            frasesRef(uid).child(frase.id).setValue(frase).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Elimina una frase del usuario (predeterminada o propia). */
    suspend fun eliminarFrase(uid: String, fraseId: String): Result<Unit> {
        return try {
            frasesRef(uid).child(fraseId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Incrementa el contador de uso al reproducir una frase (para calcular "Frecuentes"). */
    suspend fun incrementarUso(uid: String, frase: Frase): Result<Unit> {
        return try {
            frasesRef(uid).child(frase.id).child("vecesUsada").setValue(frase.vecesUsada + 1).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}