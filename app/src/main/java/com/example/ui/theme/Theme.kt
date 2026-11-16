package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val AtelierLightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = OnTerracottaContainer,
    secondary = SageSecondary,
    onSecondary = Color.White,
    secondaryContainer = SageContainer,
    onSecondaryContainer = OnSageContainer,
    tertiary = ChampagneTertiary,
    onTertiary = Color.White,
    tertiaryContainer = ChampagneContainer,
    onTertiaryContainer = OnChampagneContainer,
    background = LinenBackground,
    onBackground = EspressoInk,
    surface = CreamSurface,
    onSurface = EspressoInk,
    surfaceVariant = SandSurfaceVariant,
    onSurfaceVariant = WarmUmberMuted,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFDFBF7),
    surfaceContainer = Color(0xFFF6F1E9),
    surfaceContainerHigh = SandSurfaceVariant,
    surfaceContainerHighest = Color(0xFFE7DDD0),
    outline = WarmStoneOutline,
    outlineVariant = Color(0xFFE7DDD0),
    error = AtelierError,
    errorContainer = AtelierErrorContainer,
)

private val AtelierDarkColorScheme = darkColorScheme(
    primary = DarkTerracottaPrimary,
    onPrimary = Color(0xFF3E1308),
    primaryContainer = DarkTerracottaContainer,
    onPrimaryContainer = DarkOnTerracottaContainer,
    secondary = DarkSageSecondary,
    onSecondary = Color(0xFF1F2E1E),
    secondaryContainer = DarkSageContainer,
    onSecondaryContainer = DarkOnSageContainer,
    tertiary = DarkChampagneTertiary,
    onTertiary = Color(0xFF3B270A),
    tertiaryContainer = DarkChampagneContainer,
    onTertiaryContainer = DarkOnChampagneContainer,
    background = DarkEspressoBackground,
    onBackground = SoftLinenText,
    surface = DarkWalnutSurface,
    onSurface = SoftLinenText,
    surfaceVariant = DarkTaupeSurfaceVariant,
    onSurfaceVariant = DarkUmberMuted,
    surfaceContainerLowest = Color(0xFF050507),
    surfaceContainerLow = Color(0xFF0D0D10),
    surfaceContainer = DarkWalnutSurface,
    surfaceContainerHigh = Color(0xFF1A1A20),
    surfaceContainerHighest = DarkTaupeSurfaceVariant,
    outline = DarkStoneOutline,
    outlineVariant = Color(0xFF272730),
)

val AtelierShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun GarderobTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AtelierDarkColorScheme
        else -> AtelierLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AtelierShapes,
        content = content,
    )
}
