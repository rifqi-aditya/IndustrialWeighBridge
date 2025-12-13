package com.rifqi.industrialweighbridge.presentation.theme

import androidx.compose.ui.graphics.Color

// ============================================
// Industrial Color Palette
// ============================================

// Primary - Industrial Orange
val Orange500 = Color(0xFFFF6B00)
val Orange600 = Color(0xFFE65C00)
val Orange700 = Color(0xFFCC5200)
val Orange400 = Color(0xFFFF8C00)
val Orange300 = Color(0xFFFFAB40)

// Secondary - Industrial Blue
val Blue500 = Color(0xFF0066CC)
val Blue400 = Color(0xFF4D94FF)
val Blue600 = Color(0xFF0052A3)

// Neutral - Gray Scale
val Gray50 = Color(0xFFFAFAFA)
val Gray100 = Color(0xFFF5F5F5)
val Gray200 = Color(0xFFEEEEEE)
val Gray300 = Color(0xFFE0E0E0)
val Gray400 = Color(0xFFBDBDBD)
val Gray500 = Color(0xFF9E9E9E)
val Gray600 = Color(0xFF757575)
val Gray700 = Color(0xFF616161)
val Gray800 = Color(0xFF424242)
val Gray900 = Color(0xFF212121)

// Dark Mode Specific
val DarkSurface = Color(0xFF1E1E1E)
val DarkBackground = Color(0xFF121212)
val DarkSurfaceVariant = Color(0xFF2D2D2D)

// Status Colors
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFFC107)
val Error = Color(0xFFE53935)
val Info = Color(0xFF2196F3)

// ============================================
// Light Theme Colors
// ============================================
val LightPrimary = Orange500
val LightOnPrimary = Color.White
val LightPrimaryContainer = Orange300
val LightOnPrimaryContainer = Orange700

val LightSecondary = Blue500
val LightOnSecondary = Color.White
val LightSecondaryContainer = Blue400.copy(alpha = 0.2f)
val LightOnSecondaryContainer = Blue600

val LightBackground = Gray100
val LightOnBackground = Gray900
val LightSurface = Color.White
val LightOnSurface = Gray900
val LightSurfaceVariant = Gray200
val LightOnSurfaceVariant = Gray700

val LightError = Error
val LightOnError = Color.White

// ============================================
// Dark Theme Colors
// ============================================
val DarkPrimary = Orange400
val DarkOnPrimary = Gray900
val DarkPrimaryContainer = Orange700
val DarkOnPrimaryContainer = Orange300

val DarkSecondary = Blue400
val DarkOnSecondary = Gray900
val DarkSecondaryContainer = Blue600.copy(alpha = 0.3f)
val DarkOnSecondaryContainer = Blue400

val DarkBackgroundColor = DarkBackground
val DarkOnBackground = Gray100
val DarkSurfaceColor = DarkSurface
val DarkOnSurface = Gray100
val DarkSurfaceVariantColor = DarkSurfaceVariant
val DarkOnSurfaceVariant = Gray400

val DarkError = Color(0xFFCF6679)
val DarkOnError = Gray900
