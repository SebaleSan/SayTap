package com.saytap.app.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.saytap.app.ui.components.SayTapBottomBar
import com.saytap.app.ui.components.SayTapTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class DispositivoSimulado(val nombre: String, val distancia: Float)

private val dispositivosSimulados = listOf(
    DispositivoSimulado("Audífono izquierdo", 3f),
    DispositivoSimulado("Audífono derecho", 12f),
    DispositivoSimulado("Mi celular", 25f)
)

private val verdeCerca = Color(0xFF2E7D32)
private val amarilloMedio = Color(0xFFF9A825)
private val grisLejos = Color(0xFF9E9E9E)

/** Etiqueta, color y cantidad de barras de señal según la distancia simulada. */
private fun proximidad(distancia: Float): Triple<String, Color, Int> = when {
    distancia < 5f -> Triple("Cerca", verdeCerca, 3)
    distancia < 15f -> Triple("Medio", amarilloMedio, 2)
    else -> Triple("Lejos", grisLejos, 1)
}

@Composable
fun BuscarDispositivoScreen(
    navController: NavController,
    textScale: Float,
    onTextScaleChange: (Float) -> Unit
) {
    val context = LocalContext.current
    val vibrador = remember { context.getSystemService(Vibrator::class.java) }
    val scope = rememberCoroutineScope()

    var buscando by remember { mutableStateOf(false) }
    var encontrados by remember { mutableStateOf<List<DispositivoSimulado>>(emptyList()) }
    var objetivo by remember { mutableStateOf<DispositivoSimulado?>(null) }
    var distancia by remember { mutableFloatStateOf(0f) }

    fun vibrar(vararg tiempos: Long) {
        val patron = longArrayOf(0L, *tiempos)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrador?.vibrate(VibrationEffect.createWaveform(patron, -1))
        } else {
            vibrador?.vibrate(patron, -1)
        }
    }

    // Simula el acercamiento: la distancia baja y la vibración se acelera hasta encontrarlo.
    LaunchedEffect(objetivo) {
        val dispositivo = objetivo ?: return@LaunchedEffect
        distancia = dispositivo.distancia
        while (distancia > 0.5f) {
            vibrar(60)
            delay((distancia * 60).toLong().coerceIn(250L, 1500L))
            distancia = (distancia - maxOf(0.3f, distancia * 0.15f)).coerceAtLeast(0.5f)
        }
        vibrar(600)
    }

    Scaffold(
        topBar = { SayTapTopBar(textScale = textScale, onScaleChange = onTextScaleChange) },
        bottomBar = { SayTapBottomBar(navController) }
    ) { padding ->
        val actual = objetivo
        if (actual != null) {
            ModoEncontrar(
                nombre = actual.nombre,
                distancia = distancia,
                distanciaInicial = actual.distancia,
                onTerminar = { objetivo = null },
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    "Buscar mi audífono",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Simulación: se muestran dispositivos de ejemplo. Te guiaremos con vibración y colores.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                if (buscando) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Radar()
                        Spacer(Modifier.height(12.dp))
                        Text("Buscando dispositivos cercanos...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                buscando = true
                                encontrados = emptyList()
                                vibrar(40)
                                delay(3000)
                                encontrados = dispositivosSimulados
                                buscando = false
                                vibrar(80, 120, 80)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(if (encontrados.isEmpty()) "Buscar" else "Buscar de nuevo")
                    }
                }

                Spacer(Modifier.height(20.dp))

                encontrados.forEach { dispositivo ->
                    val (etiqueta, color, barras) = proximidad(dispositivo.distancia)
                    Card(
                        onClick = { objetivo = dispositivo },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(dispositivo.nombre, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(6.dp))
                                Box(modifier = Modifier.background(color.copy(alpha = 0.18f), RoundedCornerShape(50))) {
                                    Text(
                                        etiqueta,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = color,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            BarrasSenal(barras = barras, color = color)
                            Spacer(Modifier.width(16.dp))
                            Icon(Icons.Filled.ArrowForward, contentDescription = "Encontrar")
                        }
                    }
                }
            }
        }
    }
}

/** Ondas circulares que se expanden en bucle mientras dura la búsqueda. */
@Composable
private fun Radar() {
    val color = MaterialTheme.colorScheme.primary
    val progreso by rememberInfiniteTransition(label = "radar").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "onda"
    )
    Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            listOf(0f, 0.33f, 0.66f).forEach { desfase ->
                val p = (progreso + desfase) % 1f
                drawCircle(
                    color = color.copy(alpha = 1f - p),
                    radius = size.minDimension / 2 * p,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Hearing,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/** Tres barras de señal; se rellenan según la proximidad. */
@Composable
private fun BarrasSenal(barras: Int, color: Color) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        listOf(8.dp, 14.dp, 20.dp).forEachIndexed { i, alto ->
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(alto)
                    .background(if (i < barras) color else color.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
            )
        }
    }
}

/** Pantalla de guiado: distancia en grande, círculo que crece y fondo que se vuelve verde al acercarse. */
@Composable
private fun ModoEncontrar(
    nombre: String,
    distancia: Float,
    distanciaInicial: Float,
    onTerminar: () -> Unit,
    modifier: Modifier
) {
    val cercania = if (distanciaInicial <= 0.5f) 1f
    else ((distanciaInicial - distancia) / (distanciaInicial - 0.5f)).coerceIn(0f, 1f)
    val encontrado = distancia <= 0.5f

    val fondo by animateColorAsState(
        lerp(MaterialTheme.colorScheme.surfaceVariant, verdeCerca, cercania),
        label = "fondo"
    )
    val tamano by animateDpAsState(100.dp + 140.dp * cercania, label = "tamano")
    val pulso by rememberInfiniteTransition(label = "pulso").animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "escala"
    )
    val colorTexto = if (cercania > 0.5f) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(fondo)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(nombre, style = MaterialTheme.typography.titleLarge, color = colorTexto)

        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(tamano)
                .scale(if (encontrado) 1f else pulso)
                .background(colorTexto.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (encontrado) Icons.Filled.CheckCircle else Icons.Filled.Hearing,
                contentDescription = null,
                tint = colorTexto,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            if (encontrado) "¡Aquí está!" else "%.1f m".format(distancia),
            style = MaterialTheme.typography.displayMedium,
            color = colorTexto
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (encontrado) "Encontraste tu dispositivo" else "Sigue avanzando: la vibración se acelera al acercarte",
            style = MaterialTheme.typography.bodyMedium,
            color = colorTexto,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        Button(onClick = onTerminar) {
            Text(if (encontrado) "Listo" else "Cancelar búsqueda")
        }
    }
}