package com.saytap.app.data

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await


object AuthRepository {

    private val auth = Firebase.auth
    private val usuariosRef = sayTapDatabase.reference.child("usuarios")

    /** Crea la cuenta en Firebase Auth y guarda el perfil en Realtime Database. */
    suspend fun registrarUsuario(
        correo: String,
        contrasena: String,
        nombre: String,
        gradoAuditivo: String,
        generoVoz: String
    ): Result<Usuario> {
        return try {
            val resultadoAuth = auth.createUserWithEmailAndPassword(correo, contrasena).await()
            val uid = resultadoAuth.user?.uid
                ?: throw IllegalStateException("No se pudo obtener el uid del usuario creado")

            val usuario = Usuario(
                uid = uid,
                nombre = nombre,
                correo = correo,
                gradoAuditivo = gradoAuditivo,
                generoVoz = generoVoz
            )
            usuariosRef.child(uid).setValue(usuario).await()
            FraseRepository.sembrarFrasesPredeterminadas(uid)
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Autentica contra Firebase Auth y recupera el perfil desde Realtime Database. */
    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Usuario> {
        return try {
            val resultadoAuth = auth.signInWithEmailAndPassword(correo, contrasena).await()
            val uid = resultadoAuth.user?.uid
                ?: throw IllegalStateException("No se pudo obtener el uid del usuario")

            val snapshot = usuariosRef.child(uid).get().await()
            val usuario = snapshot.getValue(Usuario::class.java)
                ?: throw IllegalStateException("No se encontró el perfil del usuario")

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** lee todos los perfiles de usuario guardados en Realtime Database. */
    suspend fun obtenerUsuarios(): Result<List<Usuario>> {
        return try {
            val snapshot = usuariosRef.get().await()
            val usuarios = snapshot.children.mapNotNull { it.getValue(Usuario::class.java) }
            Result.success(usuarios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun enviarRecuperacion(correo: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(correo).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cerrarSesion() = auth.signOut()
}