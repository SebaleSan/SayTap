package com.saytap.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme

import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density


private val LightColors = lightColorScheme(
    primary = Indigo700,
    onPrimary = Color.White,
    primaryContainer = Indigo500,
    onPrimaryContainer = Color.White,
    secondary = Coral500,
    onSecondary = Color.White,
    secondaryContainer = Coral600,
    onSecondaryContainer = Color.White,
    background = BgApp,
    onBackground = InkText,
    surface = SurfaceColor,
    onSurface = InkText,
    surfaceVariant = BgApp,
    onSurfaceVariant = InkSoft,
    outline = BorderColor,
    error = Coral600,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Indigo300Dark,
    onPrimary = Indigo900,
    primaryContainer = Indigo700,
    onPrimaryContainer = Indigo200Dark,
    secondary = Coral300Dark,
    onSecondary = Indigo900,
    secondaryContainer = Coral600,
    onSecondaryContainer = Color.White,
    background = BgAppDark,
    onBackground = InkTextDark,
    surface = SurfaceColorDark,
    onSurface = InkTextDark,
    surfaceVariant = BgAppDark,
    onSurfaceVariant = InkSoftDark,
    outline = BorderColorDark,
    error = Coral300Dark,
    onError = Indigo900
)



@Composable
fun SayTapTheme(
    textScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors

    val baseDensity = LocalDensity.current
    val scaledDensity = Density(
        density = baseDensity.density,
        fontScale = baseDensity.fontScale * textScale
    )

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SayTapTypography,
            content = content
        )
    }
}