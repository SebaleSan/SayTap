package com.saytap.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.saytap.app.navigation.Escribir
import com.saytap.app.navigation.Hablar
import com.saytap.app.ui.components.SayTapTopBar
import com.saytap.app.ui.components.SayTapBottomBar
import com.saytap.app.navigation.BuscarDispositivo
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun BienvenidaScreen(
    navController: NavController,
    nombreUsuario: String,
    textScale: Float,
    onTextScaleChange: (Float) -> Unit
) {
    var mostrarAyuda by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { SayTapTopBar(textScale = textScale, onScaleChange = onTextScaleChange) },
        bottomBar = { SayTapBottomBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "¡Hola, $nombreUsuario!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { mostrarAyuda = true }) {
                    Icon(
                        Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Ayuda",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                "¿Qué quieres hacer hoy?\n" +
                        "Desde aquí podrás crear frases y escucharlas en vivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            Card(
                onClick = { navController.navigate(BuscarDispositivo) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Hearing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Buscar dispositivo",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "¿Perdiste tu audífono? Te ayudamos a encontrarlo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.weight(1f))

            AccesoDirectoCard(
                titulo = "Escribir",
                distintivo = "TTS",
                subtitulo = "Tablero de frases rápidas y texto a voz",
                icono = Icons.Filled.Keyboard,
                colorFondo = MaterialTheme.colorScheme.primary,
                colorContenido = MaterialTheme.colorScheme.onPrimary,
                onClick = { navController.navigate(Escribir) }
            )

            Spacer(Modifier.height(16.dp))

            AccesoDirectoCard(
                titulo = "Hablar",
                distintivo = "En vivo",
                subtitulo = "Dictado y transcripción de voz en tiempo real",
                icono = Icons.Filled.Mic,
                colorFondo = MaterialTheme.colorScheme.secondary,
                colorContenido = MaterialTheme.colorScheme.onSecondary,
                onClick = { navController.navigate(Hablar) }
            )


            Spacer(Modifier.height(8.dp))
        }
    }
    if (mostrarAyuda) {
        AlertDialog(
            onDismissRequest = { mostrarAyuda = false },
            title = { Text("¿Cómo usar SayTap?") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Escribir", style = MaterialTheme.typography.titleSmall)
                    Text("Toca una frase para escucharla en voz alta. Usa las categorías para filtrar y el botón + para crear tus propias frases.")
                    Spacer(Modifier.height(12.dp))
                    Text("Hablar", style = MaterialTheme.typography.titleSmall)
                    Text("Toca el micrófono y pide a la otra persona que hable. Verás lo que dice escrito en pantalla.")
                    Spacer(Modifier.height(12.dp))
                    Text("Buscar dispositivo", style = MaterialTheme.typography.titleSmall)
                    Text("Presiona Buscar, elige tu dispositivo y sigue la vibración: se acelera a medida que te acercas.")
                    Spacer(Modifier.height(12.dp))
                    Text("Ajustes", style = MaterialTheme.typography.titleSmall)
                    Text("Cambia tus datos, el tipo de voz y la vibración, o cierra sesión.")
                    Spacer(Modifier.height(12.dp))
                    Text("Tamaño de texto", style = MaterialTheme.typography.titleSmall)
                    Text("Usa el botón \"Tamaño de texto\" de la barra superior para agrandar las letras.")
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarAyuda = false }) { Text("Entendido") }
            }
        )
    }
}

/** Tarjeta de acceso directo grande usada en el menú principal. */
@Composable
private fun AccesoDirectoCard(
    titulo: String,
    distintivo: String,
    subtitulo: String,
    icono: ImageVector,
    colorFondo: Color,
    colorContenido: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(colorContenido.copy(alpha = 0.18f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icono,
                    contentDescription = null,
                    tint = colorContenido,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        titulo,
                        style = MaterialTheme.typography.titleLarge,
                        color = colorContenido
                    )

                    Spacer(Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .background(colorContenido.copy(alpha = 0.22f), RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            distintivo,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorContenido,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorContenido.copy(alpha = 0.85f)
                )
            }

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(colorContenido.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = colorContenido,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}