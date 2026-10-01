package com.saytap.app.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.saytap.app.R
import com.saytap.app.data.Categoria
import com.saytap.app.data.CategoriaRepository
import com.saytap.app.data.Frase
import com.saytap.app.data.FraseRepository
import com.saytap.app.data.SIN_CATEGORIA_ID
import com.saytap.app.ui.components.SayTapTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.saytap.app.data.AuthRepository

/** Identificador de la pestaña calculada "Frecuentes" (no es una Categoria real en Firebase). */
private const val FRECUENTES_ID = "frecuentes"

/** Ilustración de las categorías base; null para las que no tienen una. */
private fun imagenDeCategoria(nombre: String): Int? = when (nombre.trim().lowercase()) {
    "saludos" -> R.drawable.banner_saludos
    "emergencia" -> R.drawable.banner_emergencia
    "cotidiano" -> R.drawable.banner_cotidiano
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscribirScreen(
    navController: NavController,
    textScale: Float,
    onTextScaleChange: (Float) -> Unit
) {
    val enPreview = LocalInspectionMode.current
    val uid = if (enPreview) null else Firebase.auth.currentUser?.uid
    val context = LocalContext.current

    var categorias by remember {
        mutableStateOf(
            if (enPreview) {
                listOf(Categoria(id = "1", nombre = "Saludos"), Categoria(id = "2", nombre = "Emergencia"))
            } else emptyList()
        )
    }
    var frases by remember {
        mutableStateOf(
            if (enPreview) {
                listOf(
                    Frase(id = "1", texto = "Hola, ¿cómo estás?", categoriaId = "1", vecesUsada = 5),
                    Frase(id = "2", texto = "Necesito ayuda, por favor", categoriaId = "2", vecesUsada = 2)
                )
            } else emptyList()
        )
    }
    var cargando by remember { mutableStateOf(!enPreview) }
    var categoriaSeleccionadaId by remember { mutableStateOf(FRECUENTES_ID) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var generoVoz by remember { mutableStateOf("") }

    // Estado del diálogo de crear/editar frase
    var mostrarDialogoFrase by remember { mutableStateOf(false) }
    var fraseEnEdicion by remember { mutableStateOf<Frase?>(null) }
    var textoDialogo by remember { mutableStateOf("") }
    var categoriaDialogoId by remember { mutableStateOf(SIN_CATEGORIA_ID) }
    var categoriaDialogoExpandido by remember { mutableStateOf(false) }
    var guardandoFrase by remember { mutableStateOf(false) }

    // Estado de confirmación de borrado de frase
    var fraseAEliminar by remember { mutableStateOf<Frase?>(null) }

    // Estado del diálogo de gestión de categorías
    var mostrarGestionCategorias by remember { mutableStateOf(false) }
    var nuevaCategoriaNombre by remember { mutableStateOf("") }
    var categoriaEnEdicionId by remember { mutableStateOf<String?>(null) }
    var nombreEdicionCategoria by remember { mutableStateOf("") }
    var categoriaAEliminar by remember { mutableStateOf<Categoria?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        if (enPreview) {
            onDispose { }
        } else {
            val instancia = TextToSpeech(context) { estado ->
                if (estado == TextToSpeech.SUCCESS) {
                    tts?.language = Locale("es", "CL")
                }
            }
            instancia.language = Locale("es", "CL")
            tts = instancia
            onDispose {
                instancia.stop()
                instancia.shutdown()
            }
        }
    }

    fun cargarCategorias() {
        if (uid == null) return
        scope.launch {
            CategoriaRepository.obtenerCategorias(uid)
                .onSuccess { categorias = it }
                .onFailure { snackbarHostState.showSnackbar("No se pudieron cargar las categorías") }
        }
    }

    fun cargarFrases() {
        if (uid == null) return
        scope.launch {
            FraseRepository.obtenerFrases(uid)
                .onSuccess { frases = it }
                .onFailure { snackbarHostState.showSnackbar("No se pudieron cargar tus frases") }
        }
    }

    LaunchedEffect(uid) {
        if (uid != null) {
            cargando = true
            CategoriaRepository.obtenerCategorias(uid)
                .onSuccess { categorias = it }
                .onFailure { snackbarHostState.showSnackbar("No se pudieron cargar las categorías") }
            FraseRepository.obtenerFrases(uid)
                .onSuccess { frases = it }
                .onFailure { snackbarHostState.showSnackbar("No se pudieron cargar tus frases") }
            AuthRepository.obtenerUsuario(uid)
                .onSuccess { generoVoz = it.generoVoz }
            cargando = false
        }
    }

    // Ajusta el tono de la voz según la preferencia guardada al registrarse.
    LaunchedEffect(tts, generoVoz) {
        when (generoVoz) {
            "Voz femenina" -> tts?.setPitch(1.2f)
            "Voz masculina" -> tts?.setPitch(0.85f)
        }
    }

    fun reproducir(frase: Frase) {
        tts?.speak(frase.texto, TextToSpeech.QUEUE_FLUSH, null, frase.id)
        if (uid != null) {
            scope.launch {
                FraseRepository.incrementarUso(uid, frase).onSuccess {
                    frases = frases.map {
                        if (it.id == frase.id) it.copy(vecesUsada = it.vecesUsada + 1) else it
                    }
                }
            }
        }
    }

    fun abrirDialogoCrear() {
        fraseEnEdicion = null
        textoDialogo = ""
        categoriaDialogoId = categorias.firstOrNull()?.id ?: SIN_CATEGORIA_ID
        mostrarDialogoFrase = true
    }

    fun abrirDialogoEditar(frase: Frase) {
        fraseEnEdicion = frase
        textoDialogo = frase.texto
        categoriaDialogoId = frase.categoriaId
        mostrarDialogoFrase = true
    }

    fun guardarFrase() {
        if (uid == null || textoDialogo.isBlank()) return
        scope.launch {
            guardandoFrase = true
            val enEdicion = fraseEnEdicion
            val resultado = if (enEdicion != null) {
                FraseRepository.actualizarFrase(
                    uid,
                    enEdicion.copy(texto = textoDialogo.trim(), categoriaId = categoriaDialogoId)
                )
            } else {
                FraseRepository.crearFrase(uid, textoDialogo.trim(), categoriaDialogoId)
            }
            resultado.onSuccess {
                mostrarDialogoFrase = false
                cargarFrases()
            }.onFailure {
                snackbarHostState.showSnackbar("No se pudo guardar la frase")
            }
            guardandoFrase = false
        }
    }

    fun eliminarFrase(frase: Frase) {
        if (uid == null) return
        scope.launch {
            FraseRepository.eliminarFrase(uid, frase.id)
                .onSuccess { cargarFrases() }
                .onFailure { snackbarHostState.showSnackbar("No se pudo eliminar la frase") }
            fraseAEliminar = null
        }
    }

    fun crearCategoriaNueva() {
        if (uid == null || nuevaCategoriaNombre.isBlank()) return
        scope.launch {
            CategoriaRepository.crearCategoria(uid, nuevaCategoriaNombre.trim())
                .onSuccess {
                    nuevaCategoriaNombre = ""
                    cargarCategorias()
                }
                .onFailure { snackbarHostState.showSnackbar("No se pudo crear la categoría") }
        }
    }

    fun guardarRenombreCategoria(categoria: Categoria) {
        if (uid == null || nombreEdicionCategoria.isBlank()) return
        scope.launch {
            CategoriaRepository.renombrarCategoria(uid, categoria.copy(nombre = nombreEdicionCategoria.trim()))
                .onSuccess {
                    categoriaEnEdicionId = null
                    cargarCategorias()
                }
                .onFailure { snackbarHostState.showSnackbar("No se pudo renombrar la categoría") }
        }
    }

    fun eliminarCategoriaConfirmada(categoria: Categoria) {
        if (uid == null) return
        scope.launch {
            CategoriaRepository.eliminarCategoria(uid, categoria.id)
                .onSuccess {
                    categoriaAEliminar = null
                    if (categoriaSeleccionadaId == categoria.id) categoriaSeleccionadaId = FRECUENTES_ID
                    cargarCategorias()
                    cargarFrases()
                }
                .onFailure { snackbarHostState.showSnackbar("No se pudo eliminar la categoría") }
        }
    }

    val pestanas = listOf(
        Categoria(id = FRECUENTES_ID, nombre = "Frecuentes"),
        Categoria(id = SIN_CATEGORIA_ID, nombre = "Sin categoría")
    ) + categorias

    val frasesMostradas = if (categoriaSeleccionadaId == FRECUENTES_ID) {
        frases.sortedByDescending { it.vecesUsada }.take(3)
    } else {
        frases.filter { it.categoriaId == categoriaSeleccionadaId }
    }

    val opcionesCategoriaDialogo = listOf(Categoria(id = SIN_CATEGORIA_ID, nombre = "Sin categoría")) + categorias
    val nombreCategoriaDialogo = opcionesCategoriaDialogo.firstOrNull { it.id == categoriaDialogoId }?.nombre ?: "Sin categoría"

    val nombreCategoria = pestanas.firstOrNull { it.id == categoriaSeleccionadaId }?.nombre ?: ""
    val imagenCategoria = imagenDeCategoria(nombreCategoria)
    var muestra by remember { mutableStateOf<Palette.Swatch?>(null) }

    // Extrae con Palette el color dominante de la ilustración de la categoría seleccionada.
    LaunchedEffect(imagenCategoria) {
        muestra = if (imagenCategoria == null || enPreview) null else withContext(Dispatchers.Default) {
            ContextCompat.getDrawable(context, imagenCategoria)?.toBitmap(360, 140)?.let { bitmap ->
                val paleta = Palette.from(bitmap).generate()
                paleta.dominantSwatch ?: paleta.vibrantSwatch
            }
        }
    }
    val colorFondo = muestra?.let { Color(it.rgb) } ?: MaterialTheme.colorScheme.secondaryContainer
    val colorTexto = muestra?.let { Color(it.bodyTextColor) } ?: MaterialTheme.colorScheme.onSecondaryContainer

    Scaffold(
        topBar = { SayTapTopBar(textScale = textScale, onScaleChange = onTextScaleChange) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { abrirDialogoCrear() }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar frase")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(pestanas, key = { it.id }) { pestana ->
                        FilterChip(
                            selected = categoriaSeleccionadaId == pestana.id,
                            onClick = { categoriaSeleccionadaId = pestana.id },
                            label = { Text(pestana.nombre) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorFondo,
                                selectedLabelColor = colorTexto
                            )
                        )
                    }
                }
                IconButton(onClick = { mostrarGestionCategorias = true }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Gestionar categorías")
                }
            }
            if (imagenCategoria != null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colorFondo),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Image(
                        painter = painterResource(imagenCategoria),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(140.dp)
                    )
                    Text(
                        nombreCategoria,
                        color = colorTexto,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            when {
                cargando -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(40.dp))
                        CircularProgressIndicator()
                    }
                }
                frasesMostradas.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (categoriaSeleccionadaId == FRECUENTES_ID)
                                "Aún no has usado ninguna frase."
                            else
                                "No hay frases en esta categoría todavía.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(frasesMostradas, key = { it.id }) { frase ->
                            Card(
                                onClick = { reproducir(frase) },
                                colors = CardDefaults.cardColors(
                                    containerColor = colorFondo,
                                    contentColor = colorTexto
                                ),
                                modifier = Modifier.fillMaxWidth().height(90.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(12.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            frase.texto,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 3
                                        )
                                    }
                                    Row(modifier = Modifier.align(Alignment.TopEnd).padding(2.dp)) {
                                        IconButton(
                                            onClick = { abrirDialogoEditar(frase) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Edit,
                                                contentDescription = "Editar frase",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { fraseAEliminar = frase },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Eliminar frase",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de crear/editar frase
    if (mostrarDialogoFrase) {
        AlertDialog(
            onDismissRequest = { if (!guardandoFrase) mostrarDialogoFrase = false },
            title = { Text(if (fraseEnEdicion != null) "Editar frase" else "Nueva frase") },
            text = {
                Column {
                    OutlinedTextField(
                        value = textoDialogo,
                        onValueChange = { textoDialogo = it },
                        label = { Text("Texto de la frase") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Categoría", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = categoriaDialogoExpandido,
                        onExpandedChange = { categoriaDialogoExpandido = !categoriaDialogoExpandido }
                    ) {
                        OutlinedTextField(
                            value = nombreCategoriaDialogo,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaDialogoExpandido) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoriaDialogoExpandido,
                            onDismissRequest = { categoriaDialogoExpandido = false }
                        ) {
                            opcionesCategoriaDialogo.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion.nombre) },
                                    onClick = {
                                        categoriaDialogoId = opcion.id
                                        categoriaDialogoExpandido = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { guardarFrase() },
                    enabled = !guardandoFrase && textoDialogo.isNotBlank()
                ) {
                    Text(if (guardandoFrase) "Guardando..." else "Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoFrase = false }, enabled = !guardandoFrase) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Confirmación de borrado de frase
    fraseAEliminar?.let { frase ->
        AlertDialog(
            onDismissRequest = { fraseAEliminar = null },
            title = { Text("Eliminar frase") },
            text = { Text("¿Seguro que quieres eliminar \"${frase.texto}\"?") },
            confirmButton = { TextButton(onClick = { eliminarFrase(frase) }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { fraseAEliminar = null }) { Text("Cancelar") } }
        )
    }

    // Diálogo de gestión de categorías
    if (mostrarGestionCategorias) {
        AlertDialog(
            onDismissRequest = { mostrarGestionCategorias = false },
            title = { Text("Gestionar categorías") },
            text = {
                Column {
                    categorias.forEach { categoria ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            if (categoriaEnEdicionId == categoria.id) {
                                OutlinedTextField(
                                    value = nombreEdicionCategoria,
                                    onValueChange = { nombreEdicionCategoria = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                IconButton(onClick = { guardarRenombreCategoria(categoria) }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Guardar nombre")
                                }
                            } else {
                                Text(categoria.nombre, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    categoriaEnEdicionId = categoria.id
                                    nombreEdicionCategoria = categoria.nombre
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Renombrar categoría")
                                }
                                IconButton(onClick = { categoriaAEliminar = categoria }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar categoría")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = nuevaCategoriaNombre,
                            onValueChange = { nuevaCategoriaNombre = it },
                            label = { Text("Nueva categoría") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { crearCategoriaNueva() }) {
                            Icon(Icons.Filled.Add, contentDescription = "Crear categoría")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarGestionCategorias = false }) { Text("Cerrar") }
            }
        )
    }

    // Confirmación de borrado de categoría
    categoriaAEliminar?.let { categoria ->
        AlertDialog(
            onDismissRequest = { categoriaAEliminar = null },
            title = { Text("Eliminar categoría") },
            text = { Text("Las frases de \"${categoria.nombre}\" pasarán a \"Sin categoría\". ¿Continuar?") },
            confirmButton = { TextButton(onClick = { eliminarCategoriaConfirmada(categoria) }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { categoriaAEliminar = null }) { Text("Cancelar") } }
        )
    }
}