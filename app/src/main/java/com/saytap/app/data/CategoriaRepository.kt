package com.saytap.app.data

import kotlinx.coroutines.tasks.await

/** Id reservado para frases sin categoría (no es una Categoria real guardada en Firebase). */
const val SIN_CATEGORIA_ID = "sin_categoria"

object CategoriaRepository {

    private fun categoriasRef(uid: String) = sayTapDatabase.reference.child("categorias").child(uid)

    /** Crea las 3 categorías base para un usuario recién registrado. */
    suspend fun sembrarCategoriasPredeterminadas(uid: String): Result<List<Categoria>> {
        return try {
            val nombresBase = listOf("Saludos", "Emergencia", "Cotidiano")
            val creadas = nombresBase.map { nombre ->
                val id = categoriasRef(uid).push().key
                    ?: throw IllegalStateException("No se pudo generar id de categoría")
                val categoria = Categoria(id = id, nombre = nombre)
                categoriasRef(uid).child(id).setValue(categoria).await()
                categoria
            }
            Result.success(creadas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerCategorias(uid: String): Result<List<Categoria>> {
        return try {
            val snapshot = categoriasRef(uid).get().await()
            val categorias = snapshot.children.mapNotNull { it.getValue(Categoria::class.java) }
            Result.success(categorias)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearCategoria(uid: String, nombre: String): Result<Categoria> {
        return try {
            val id = categoriasRef(uid).push().key
                ?: throw IllegalStateException("No se pudo generar id de categoría")
            val categoria = Categoria(id = id, nombre = nombre)
            categoriasRef(uid).child(id).setValue(categoria).await()
            Result.success(categoria)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renombrarCategoria(uid: String, categoria: Categoria): Result<Unit> {
        return try {
            categoriasRef(uid).child(categoria.id).setValue(categoria).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una categoría. Antes de borrarla, reasigna todas sus frases
     * a "Sin categoría" para no perderlas
     */
    suspend fun eliminarCategoria(uid: String, categoriaId: String): Result<Unit> {
        return try {
            val frases = FraseRepository.obtenerFrases(uid).getOrDefault(emptyList())
            frases.filter { it.categoriaId == categoriaId }.forEach { frase ->
                FraseRepository.actualizarFrase(uid, frase.copy(categoriaId = SIN_CATEGORIA_ID))
            }
            categoriasRef(uid).child(categoriaId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}