package com.saytap.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.saytap.app.data.Usuario
import com.saytap.app.data.UsuariosStore
import com.saytap.app.ui.components.SayTapTopBar
import com.saytap.app.ui.theme.SayTapTheme
import kotlinx.coroutines.launch

private val gradosAuditivos = listOf("Leve", "Moderada", "Severa")
//se selecciona un genero de voz para el texto a voz
private val generosVoz = listOf("Voz femenina", "Voz masculina")


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    navController: NavController,
    textScale: Float,
    onTextScaleChange: (Float) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }


    var gradoExpandido by remember { mutableStateOf(false) }
    var gradoSeleccionado by remember { mutableStateOf(gradosAuditivos[1]) }

    var alertasVibracion by remember { mutableStateOf(true) }

    var generoVozSeleccionado by remember { mutableStateOf(generosVoz[0]) }
    var aceptaTerminos by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { SayTapTopBar(textScale = textScale, onScaleChange = onTextScaleChange) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                "Crea tu cuenta",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Cuéntanos un poco de ti para personalizar tu experiencia.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmarContrasena,
                onValueChange = { confirmarContrasena = it },
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))

// se registra el grado de perdida auditiva para implementacion de futuras funcionalidades de al app
            Text("Grado de pérdida auditiva", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded = gradoExpandido,
                onExpandedChange = { gradoExpandido = !gradoExpandido }
            ) {
                OutlinedTextField(
                    value = gradoSeleccionado,
                    onValueChange = {},
                    readOnly = true,
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
                                gradoSeleccionado = opcion
                                gradoExpandido = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))


            Text("Preferencias de accesibilidad", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
// Permitira feedback haptico al utilizar la app
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = alertasVibracion, onCheckedChange = { alertasVibracion = it })
                Text("Feedback por vibración al realizar acciones", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))


            Text("Voz para reproducir tus frases en voz alta", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            generosVoz.forEach { opcion ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = generoVozSeleccionado == opcion,
                        onClick = { generoVozSeleccionado = opcion }
                    )
                    Text(opcion, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(16.dp))
//simulacion de TyC
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = aceptaTerminos, onCheckedChange = { aceptaTerminos = it })
                Text("Acepto los términos y condiciones", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(18.dp))
// se comprueba datos requeridos para registro
            Button(
                onClick = {
                    scope.launch {
                        when {
                            nombre.isBlank() || correo.isBlank() || contrasena.isBlank() -> {
                                snackbarHostState.showSnackbar("Completa todos los campos obligatorios")
                            }
                            contrasena != confirmarContrasena -> {
                                snackbarHostState.showSnackbar("Las contraseñas no coinciden")
                            }
                            UsuariosStore.existeCorreo(correo) -> {
                                snackbarHostState.showSnackbar("Ese correo ya está registrado")
                            }
                            !aceptaTerminos -> {
                                snackbarHostState.showSnackbar("Debes aceptar los términos y condiciones")
                            }
                            else -> {
                                UsuariosStore.usuarios.add(
                                    Usuario(
                                        nombre = nombre.trim(),
                                        correo = correo.trim(),
                                        contrasena = contrasena,
                                        gradoAuditivo = gradoSeleccionado,
                                        generoVoz = generoVozSeleccionado
                                    )
                                )
                                snackbarHostState.showSnackbar("Cuenta creada. Ahora puedes iniciar sesión")
                                navController.popBackStack()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Crear cuenta", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroPreview() {
    SayTapTheme {
        RegistroScreen(
            navController = rememberNavController(),
            textScale = 1f,
            onTextScaleChange = {}
        )
    }
}