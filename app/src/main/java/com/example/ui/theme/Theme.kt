package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = EditorialDarkBg,
    surface = EditorialDarkSurface,
    onBackground = EditorialDarkTextPrimary,
    onSurface = EditorialDarkTextPrimary,
    surfaceVariant = Color(0xFF242327),
    onSurfaceVariant = EditorialDarkTextSecondary,
    outline = EditorialDarkBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EditorialBrandPurple,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = EditorialLightBg,
    surface = EditorialLightSurface,
    onBackground = EditorialLightTextPrimary,
    onSurface = EditorialLightTextPrimary,
    surfaceVariant = Color(0xFFE8E7EB),
    onSurfaceVariant = EditorialLightTextSecondary,
    outline = EditorialLightBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Custom brand theme is highly styled, so we prioritize custom color scheme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
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

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
