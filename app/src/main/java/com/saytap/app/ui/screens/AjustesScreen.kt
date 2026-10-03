package com.saytap.app.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.saytap.app.data.AuthRepository
import com.saytap.app.navigation.Login
import com.saytap.app.ui.components.SayTapBottomBar
import com.saytap.app.ui.components.SayTapTopBar
import com.saytap.app.util.mensajeAmigable
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.Switch

private val gradosAuditivos = listOf("Leve", "Moderada", "Severa")
private val generosVoz = listOf("Voz femenina", "Voz masculina")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    navController: NavController,
    textScale: Float,
    onTextScaleChange: (Float) -> Unit
) {
    val uid = Firebase.auth.currentUser?.uid
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var nombre by remember { mutableStateOf("") }
    var grado by remember { mutableStateOf(gradosAuditivos[1]) }
    var gradoExpandido by remember { mutableStateOf(false) }
    var genero by remember { mutableStateOf(generosVoz[0]) }
    var vibracion by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }

    var mostrarEliminar by remember { mutableStateOf(false) }
    var contrasena by remember { mutableStateOf("") }
    var eliminando by remember { mutableStateOf(false) }
    var errorEliminar by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        if (uid != null) {
            AuthRepository.obtenerUsuario(uid).onSuccess {
                nombre = it.nombre
                grado = it.gradoAuditivo
                genero = it.generoVoz
                vibracion = it.vibracion
            }
        }
    }

    fun volverALogin() {
        context.getSharedPreferences("saytap_sesion", Context.MODE_PRIVATE).edit { clear() }
        context.getSharedPreferences("saytap_frases", Context.MODE_PRIVATE).edit { clear() }
        navController.navigate(Login) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    Scaffold(
        topBar = { SayTapTopBar(textScale = textScale, onScaleChange = onTextScaleChange) },
        bottomBar = { SayTapBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text("Ajustes", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Aqui podras editar tus datos personales y preferencias de voz.")

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = gradoExpandido,
                onExpandedChange = { gradoExpandido = !gradoExpandido }
            ) {
                OutlinedTextField(
                    value = grado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Grado auditivo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradoExpandido) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = gradoExpandido,
                    onDismissRequest = { gradoExpandido = false }
                ) {
                    gradosAuditivos.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                grado = opcion
                                gradoExpandido = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Voz para reproducir tus frases en voz alta", style = MaterialTheme.typography.labelLarge)
            generosVoz.forEach { opcion ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = genero == opcion, onClick = { genero = opcion })
                    Text(opcion)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Vibrar al reproducir una frase", modifier = Modifier.weight(1f))
                Switch(checked = vibracion, onCheckedChange = { vibracion = it })
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (uid != null) {
                        scope.launch {
                            guardando = true
                            AuthRepository.actualizarPerfil(uid, nombre.trim(), grado, genero, vibracion)
                                .onSuccess {
                                    val prefs = context.getSharedPreferences("saytap_sesion", Context.MODE_PRIVATE)
                                    if (prefs.contains("nombre")) prefs.edit { putString("nombre", nombre.trim()) }
                                    snackbarHostState.showSnackbar("Cambios guardados")
                                }
                                .onFailure { snackbarHostState.showSnackbar(it.mensajeAmigable()) }
                            guardando = false
                        }
                    }
                },
                enabled = !guardando && nombre.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(if (guardando) "Guardando..." else "Guardar cambios")
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))


            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    AuthRepository.cerrarSesion()
                    volverALogin()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    contrasena = ""
                    errorEliminar = null
                    mostrarEliminar = true
                },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar cuenta")
            }
        }
    }

    if (mostrarEliminar) {
        AlertDialog(
            onDismissRequest = { if (!eliminando) mostrarEliminar = false },
            title = { Text("Eliminar cuenta") },
            text = {
                Column {
                    Text("Se eliminará tu cuenta por completo: perfil, frases, categorías y transcripciones. Esta acción no se puede deshacer.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = { contrasena = it },
                        label = { Text("Confirma tu contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    errorEliminar?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            eliminando = true
                            errorEliminar = null
                            AuthRepository.eliminarCuenta(contrasena)
                                .onSuccess { volverALogin() }
                                .onFailure { errorEliminar = it.mensajeAmigable() }
                            eliminando = false
                        }
                    },
                    enabled = !eliminando && contrasena.isNotBlank()
                ) {
                    Text(if (eliminando) "Eliminando..." else "Eliminar definitivamente")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarEliminar = false }, enabled = !eliminando) {
                    Text("Cancelar")
                }
            }
        )
    }
}