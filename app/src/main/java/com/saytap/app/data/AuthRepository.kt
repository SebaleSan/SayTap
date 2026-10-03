package com.saytap.app.data

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.EmailAuthProvider


object AuthRepository {

    private val auth = Firebase.auth
    private val usuariosRef = sayTapDatabase.reference.child("usuarios")

    /** Crea la cuenta en Firebase Auth y guarda el perfil en Realtime Database. */
    suspend fun registrarUsuario(
        correo: String,
        contrasena: String,
        nombre: String,
        gradoAuditivo: String,
        generoVoz: String,
        vibracion: Boolean
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
                generoVoz = generoVoz,
                vibracion = vibracion
            )
            usuariosRef.child(uid).setValue(usuario).await()

            val categorias = CategoriaRepository.sembrarCategoriasPredeterminadas(uid).getOrDefault(emptyList())
            FraseRepository.sembrarFrasesPredeterminadas(uid, categorias)

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

    /** lee el perfil de un usuario específico desde Realtime Database. */
    suspend fun obtenerUsuario(uid: String): Result<Usuario> {
        return try {
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

    /** Actualiza nombre, grado auditivo y género de voz del perfil. */
    suspend fun actualizarPerfil(uid: String, nombre: String, gradoAuditivo: String, generoVoz: String, vibracion: Boolean): Result<Unit> {
        return try {
            usuariosRef.child(uid).updateChildren(
                mapOf("nombre" to nombre, "gradoAuditivo" to gradoAuditivo, "generoVoz" to generoVoz, "vibracion" to vibracion)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Reautentica con la contraseña, borra todos los datos del usuario y elimina la cuenta. */
    suspend fun eliminarCuenta(contrasena: String): Result<Unit> {
        return try {
            val usuario = auth.currentUser ?: throw IllegalStateException("No hay sesión activa")
            val correo = usuario.email ?: throw IllegalStateException("La cuenta no tiene correo")
            val uid = usuario.uid
            usuario.reauthenticate(EmailAuthProvider.getCredential(correo, contrasena)).await()

            val raiz = sayTapDatabase.reference
            raiz.child("usuarios").child(uid).removeValue().await()
            raiz.child("frases").child(uid).removeValue().await()
            raiz.child("categorias").child(uid).removeValue().await()
            raiz.child("transcripciones").child(uid).removeValue().await()

            usuario.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun cerrarSesion() = auth.signOut()
}