package com.github.onlynotesswent.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
    darkColorScheme(
        primary = MainColor,
        primaryContainer = DarkContainer,
        secondary = DarkAccent,
        tertiary = LightAccent,
        background = DarkBackground,
        surface = DarkShade,
        error = Danger,
        onPrimary = DarkShade,
        onPrimaryContainer = LightShade,
        onSecondary = LightShade,
        onTertiary = DarkShade,
        onBackground = LightShade,
        onSurface = LightShade,
        surfaceVariant = DarkCards)

private val LightColorScheme =
    lightColorScheme(
        primary = MainColor,
        primaryContainer = LightContainer,
        secondary = LightAccent,
        tertiary = DarkAccent,
        background = LightBackground,
        surface = LightShade,
        error = Danger,
        onPrimary = LightShade,
        onPrimaryContainer = DarkShade,
        onSecondary = DarkShade,
        onTertiary = LightShade,
        onBackground = DarkShade,
        onSurface = DarkShade,
        surfaceVariant = LightCards)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // disabled to ensure consistent colors across devices
    content: @Composable () -> Unit
) {
  val colorScheme =
      when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
      }
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.primary.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
