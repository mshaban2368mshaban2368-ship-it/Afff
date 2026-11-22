package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class Fa97ExtendedColors(
  val keyNumeric: Color,
  val keyUtility: Color,
  val keyScientific: Color,
  val keyOperatorContainer: Color,
  val keyOperatorContent: Color,
  val equalsStart: Color,
  val equalsEnd: Color,
  val onEquals: Color,
  val scientificAccent: Color,
  val displayGlow: Color,
)

private val DarkExtendedColors =
  Fa97ExtendedColors(
    keyNumeric = DarkKeyNumeric,
    keyUtility = DarkKeyUtility,
    keyScientific = DarkKeyScientific,
    keyOperatorContainer = NeonCyanContainer,
    keyOperatorContent = NeonCyan,
    equalsStart = NeonCyan,
    equalsEnd = NeonMint,
    onEquals = Color(0xFF041816),
    scientificAccent = NeonViolet,
    displayGlow = NeonCyan.copy(alpha = 0.14f),
  )

private val LightExtendedColors =
  Fa97ExtendedColors(
    keyNumeric = LightKeyNumeric,
    keyUtility = LightKeyUtility,
    keyScientific = LightKeyScientific,
    keyOperatorContainer = LightAccentContainer,
    keyOperatorContent = LightAccentTeal,
    equalsStart = LightAccentTeal,
    equalsEnd = LightAccentTealBright,
    onEquals = Color.White,
    scientificAccent = LightAccentViolet,
    displayGlow = LightAccentTeal.copy(alpha = 0.10f),
  )

val LocalFa97Colors = staticCompositionLocalOf { DarkExtendedColors }

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF041816),
    primaryContainer = NeonCyanContainer,
    onPrimaryContainer = NeonCyan,
    secondary = NeonViolet,
    onSecondary = Color(0xFF120C2E),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = DarkOutline,
    error = NeonCoralError,
    onError = Color(0xFF28050D),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LightAccentTeal,
    onPrimary = Color.White,
    primaryContainer = LightAccentContainer,
    onPrimaryContainer = LightAccentTeal,
    secondary = LightAccentViolet,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightOnSurfaceMuted,
    outline = LightOutline,
    error = LightErrorRed,
    onError = Color.White,
  )

@Composable
fun Fa97Theme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

  CompositionLocalProvider(LocalFa97Colors provides extendedColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Fa97Typography,
      content = content,
    )
  }
}

object Fa97ThemeTokens {
  val colors: Fa97ExtendedColors
    @Composable get() = LocalFa97Colors.current
}
