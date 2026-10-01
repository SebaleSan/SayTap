package com.saytap.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.saytap.app.data.Transcripcion
import com.saytap.app.data.TranscripcionRepository
import com.saytap.app.ui.components.SayTapTopBar
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun HablarScreen(
    navController: NavController,
    textScale: Float,
    onTextScaleChange: (Float) -> Unit
) {
    val enPreview = LocalInspectionMode.current
    val uid = if (enPreview) null else Firebase.auth.currentUser?.uid
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var textoReconocido by remember { mutableStateOf("") }
    var escuchando by remember { mutableStateOf(false) }
    var transcripciones by remember {
        mutableStateOf(
            if (enPreview) {
                listOf(
                    Transcripcion(id = "1", texto = "Hola, ¿cómo estás?", timestamp = 1L),
                    Transcripcion(id = "2", texto = "Nos vemos mañana", timestamp = 2L)
                )
            } else emptyList()
        )
    }

    var tienePermiso by remember {
        mutableStateOf(
            enPreview || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val solicitarPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> tienePermiso = concedido }

    fun cargarTranscripciones() {
        if (uid == null) return
        scope.launch {
            TranscripcionRepository.obtenerTranscripciones(uid)
                .onSuccess { transcripciones = it }
        }
    }

    LaunchedEffect(uid) {
        cargarTranscripciones()
    }

    val speechRecognizer = remember {
        if (enPreview) null else SpeechRecognizer.createSpeechRecognizer(context)
    }

    DisposableEffect(Unit) {
        onDispose { speechRecognizer?.destroy() }
    }

    fun iniciarEscucha() {
        if (speechRecognizer == null) return
        val idioma = Locale("es", "CL").toLanguageTag()
        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, idioma)
        }
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                escuchando = false
            }

            override fun onError(error: Int) {
                escuchando = false
                scope.launch {
                    snackbarHostState.showSnackbar("No se pudo reconocer el audio, intenta de nuevo")
                }
            }

            override fun onResults(results: Bundle?) {
                escuchando = false
                val texto = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!texto.isNullOrBlank()) {
                    textoReconocido = texto
                    if (uid != null) {
                        scope.launch {
                            TranscripcionRepository.guardarTranscripcion(uid, texto)
                                .onSuccess { cargarTranscripciones() }
                        }
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        escuchando = true
        textoReconocido = ""
        speechRecognizer.startListening(intent)
    }

    fun detenerEscucha() {
        speechRecognizer?.stopListening()
        escuchando = false
    }

    Scaffold(
        topBar = { SayTapTopBar(textScale = textScale, onScaleChange = onTextScaleChange) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                if (escuchando) "Escuchando..." else "Toca el micrófono para reconocer voz",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        if (escuchando) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
                    .clickable {
                        when {
                            !tienePermiso -> solicitarPermiso.launch(Manifest.permission.RECORD_AUDIO)
                            escuchando -> detenerEscucha()
                            else -> iniciarEscucha()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (escuchando) Icons.Filled.MicOff else Icons.Filled.Mic,
                    contentDescription = if (escuchando) "Detener" else "Hablar",
                    tint = if (escuchando) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            if (!tienePermiso) {
                Text(
                    "SayTap necesita permiso del micrófono para transcribir voz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
            }

            if (textoReconocido.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        textoReconocido,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            Text(
                "Últimas transcripciones",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            if (transcripciones.isEmpty()) {
                Text(
                    "Aún no has transcrito nada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(transcripciones, key = { it.id }) { transcripcion ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                transcripcion.texto,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}