package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ArcadeColorScheme = darkColorScheme(
  primary = NeonPurple,
  onPrimary = TextPrimary,
  primaryContainer = DarkSurfaceVariant,
  onPrimaryContainer = NeonPurple,
  secondary = NeonCyan,
  onSecondary = DarkBackground,
  secondaryContainer = DarkSurfaceVariant,
  onSecondaryContainer = NeonCyan,
  tertiary = NeonPink,
  onTertiary = TextPrimary,
  background = DarkBackground,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = DarkBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = ArcadeColorScheme,
    typography = Typography,
    content = content
  )
}

