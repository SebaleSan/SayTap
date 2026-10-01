package com.saytap.app.data

import kotlinx.coroutines.tasks.await

object TranscripcionRepository {

    private const val MAX_TRANSCRIPCIONES = 3

    private fun transcripcionesRef(uid: String) =
        sayTapDatabase.reference.child("transcripciones").child(uid)

    /** Lee las transcripciones guardadas, más reciente primero. */
    suspend fun obtenerTranscripciones(uid: String): Result<List<Transcripcion>> {
        return try {
            val snapshot = transcripcionesRef(uid).get().await()
            val lista = snapshot.children
                .mapNotNull { it.getValue(Transcripcion::class.java) }
                .sortedByDescending { it.timestamp }
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Guarda una transcripción nueva. Si ya hay 3 o más guardadas,
     * borra la más antigua antes de agregar la nueva, para no
     * desperdiciar espacio en la base de datos.
     */
    suspend fun guardarTranscripcion(uid: String, texto: String): Result<Unit> {
        return try {
            val snapshot = transcripcionesRef(uid).get().await()
            val existentes = snapshot.children.mapNotNull { it.getValue(Transcripcion::class.java) }

            if (existentes.size >= MAX_TRANSCRIPCIONES) {
                val masAntigua = existentes.minByOrNull { it.timestamp }
                masAntigua?.let { transcripcionesRef(uid).child(it.id).removeValue().await() }
            }

            val id = transcripcionesRef(uid).push().key
                ?: throw IllegalStateException("No se pudo generar id de transcripción")
            val nueva = Transcripcion(id = id, texto = texto, timestamp = System.currentTimeMillis())
            transcripcionesRef(uid).child(id).setValue(nueva).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}