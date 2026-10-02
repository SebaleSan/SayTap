package com.saytap.app.data

import kotlinx.coroutines.tasks.await
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import org.json.JSONArray

object FraseRepository {

    private fun frasesRef(uid: String) = sayTapDatabase.reference.child("frases").child(uid)

    /**
     * Carga las 9 frases base para un usuario recién registrado.
     * Se llama una sola vez, inmediatamente después de crear la cuenta.
     */
    suspend fun sembrarFrasesPredeterminadas(uid: String, categorias: List<Categoria>): Result<Unit> {
        return try {
            fun idDe(nombre: String) = categorias.first { it.nombre == nombre }.id

            val frasesBase = listOf(
                Frase(texto = "Hola, ¿cómo estás?", categoriaId = idDe("Saludos")),
                Frase(texto = "Mucho gusto", categoriaId = idDe("Saludos")),
                Frase(texto = "Nos vemos luego", categoriaId = idDe("Saludos")),
                Frase(texto = "Necesito ayuda, por favor", categoriaId = idDe("Emergencia")),
                Frase(texto = "Llamen a una ambulancia", categoriaId = idDe("Emergencia")),
                Frase(texto = "Tengo dificultad para escuchar, ¿puede hablar en mi celular para yo leer lo que dice?", categoriaId = idDe("Emergencia")),
                Frase(texto = "Sí", categoriaId = idDe("Cotidiano")),
                Frase(texto = "No", categoriaId = idDe("Cotidiano")),
                Frase(texto = "Un momento, por favor", categoriaId = idDe("Cotidiano"))
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
    suspend fun crearFrase(uid: String, texto: String, categoriaId: String): Result<Frase> {
        return try {
            val id = frasesRef(uid).push().key
                ?: throw IllegalStateException("No se pudo generar id de frase")
            val frase = Frase(id = id, texto = texto, categoriaId = categoriaId)
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

/**
 * Content Provider de solo lectura que expone las frases del usuario a otras apps.
 *
 * Se accede mediante la URI `content://com.saytap.app.provider`. Las consultas devuelven un
 * Cursor con las columnas id, texto, categoriaId y vecesUsada, leídas desde una copia local
 * (SharedPreferences) que EscribirScreen actualiza cada vez que cambia la lista de frases.
 * No consulta Firebase. Las operaciones insert, delete y update no están soportadas.
 */
class FrasesProvider : ContentProvider() {
    override fun onCreate() = true

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor {
        val cursor = MatrixCursor(arrayOf("id", "texto", "categoriaId", "vecesUsada"))
        val json = context!!.getSharedPreferences("saytap_frases", Context.MODE_PRIVATE).getString("frases", "[]")
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val f = array.getJSONObject(i)
            cursor.addRow(arrayOf(f.getString("id"), f.getString("texto"), f.getString("categoriaId"), f.getInt("vecesUsada")))
        }
        return cursor
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.dir/vnd.com.saytap.app.frase"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?) = 0
}