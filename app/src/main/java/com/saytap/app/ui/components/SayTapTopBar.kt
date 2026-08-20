package com.saytap.app.ui.components

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val opcionesTamano = listOf(
    1f to "Normal",
    1.15f to "Grande",
    1.3f to "Muy grande"
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SayTapTopBar(
    textScale: Float,
    onScaleChange: (Float) -> Unit,
    title: String = "SayTap"
) {
    var expanded by remember { mutableStateOf(false) }
    val etiquetaActual = opcionesTamano.firstOrNull { it.first == textScale }?.second ?: "Normal"

    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        actions = {
            Box {
                AssistChip(
                onClick = { expanded = true },
                label = { Text("Tamaño de texto", color = MaterialTheme.colorScheme.onSecondary) },
                trailingIcon = {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                border = null
            )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    Text(
                        "Tamaño de texto",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    HorizontalDivider()
                    opcionesTamano.forEach { (escala, etiqueta) ->
                        DropdownMenuItem(
                            text = { Text(etiqueta) },
                            onClick = {
                                onScaleChange(escala)
                                expanded = false
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}