package com.saytap.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.saytap.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Devuelve la ilustración asociada a una categoría base, o null si no tiene una. */
private fun imagenDeCategoria(nombre: String): Int? = when (nombre.trim().lowercase()) {
    "saludos" -> R.drawable.banner_saludos
    "emergencia" -> R.drawable.banner_emergencia
    "cotidiano" -> R.drawable.banner_cotidiano
    else -> null
}

/**
 * Encabezado con la ilustración de la categoría. El color de fondo de la franja
 * del nombre y el color del texto se obtienen de la imagen con Palette.
 * No muestra nada si la categoría no tiene ilustración.
 */
@Composable
fun CategoriaBanner(nombre: String) {
    val imagen = imagenDeCategoria(nombre) ?: return
    val context = LocalContext.current
    val enPreview = LocalInspectionMode.current
    var colores by remember(imagen) { mutableStateOf<Pair<Color, Color>?>(null) }

    LaunchedEffect(imagen) {
        if (enPreview) return@LaunchedEffect
        colores = withContext(Dispatchers.Default) {
            val drawable = ContextCompat.getDrawable(context, imagen) ?: return@withContext null
            val paleta = Palette.from(drawable.toBitmap(width = 360, height = 140)).generate()
            val muestra = paleta.dominantSwatch ?: paleta.vibrantSwatch
            muestra?.let { Color(it.rgb) to Color(it.titleTextColor) }
        }
    }

    val colorFondo = colores?.first ?: MaterialTheme.colorScheme.primary
    val colorTexto = colores?.second ?: MaterialTheme.colorScheme.onPrimary

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Image(
            painter = painterResource(imagen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        )
        Text(
            nombre,
            color = colorTexto,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}