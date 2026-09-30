package com.saytap.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.saytap.app.data.Transcripcion
import com.saytap.app.data.TranscripcionRepository
import com.saytap.app.ui.components.SayTapTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HablarScreen(
    navController: NavController,
    textScale: Float,
    onTextScaleChange: (Float) -> Unit
) {
    val uid = Firebase.auth.currentUser?.uid
    val context = LocalContext.current

    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var escuchando by remember { mutableStateOf(false) }
    var textoReconocido by remember { mutableStateOf("") }
    var transcripciones by remember { mutableStateOf<List<Transcripcion>>(emptyList()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Lanzador para pedir el permiso de micrófono en tiempo de ejecución.
    val solicitarPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido
        if (!concedido) {
            scope.launch {
                snackbarHostState.showSnackbar("Necesitas dar permiso de micrófono para usar esta función")
            }
        }
    }

    fun recargarTranscripciones() {
        if (uid == null) return
        scope.launch {
            TranscripcionRepository.obtenerTranscripciones(uid)
                .onSuccess { transcripciones = it }
                .onFailure { snackbarHostState.showSnackbar("No se pudieron cargar las transcripciones") }
        }
    }

    LaunchedEffect(uid) {
        recargarTranscripciones()
    }

    Scaffold(
        topBar = { SayTapTopBar(textScale = textScale, onScaleChange = onTextScaleChange) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // el contenido (botón de micrófono, texto reconocido, lista) va en el siguiente paso
    }
}