package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ChurchGold,
    onPrimary = ChurchTealDark,
    primaryContainer = ChurchTealDarkContainer,
    onPrimaryContainer = ChurchGoldLight,
    secondary = ChurchGoldLight,
    onSecondary = ChurchTealDark,
    background = ChurchTealDarkContainer,
    onBackground = ChurchBackground,
    surface = ChurchTealDark,
    onSurface = ChurchBackground
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ChurchTealDark,
    onPrimary = ChurchGold,
    primaryContainer = ChurchTealContainer,
    onPrimaryContainer = ChurchTealDark,
    secondary = ChurchGold,
    onSecondary = ChurchTealDark,
    secondaryContainer = ChurchGoldContainer,
    onSecondaryContainer = ChurchTealDark,
    background = ChurchBackground,
    onBackground = TextPrimary,
    surface = ChurchSurface,
    onSurface = TextPrimary,
    surfaceVariant = ChurchSurfaceVariant,
    onSurfaceVariant = TextSecondary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
