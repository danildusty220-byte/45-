package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

enum class ProfileGradientPreset(
    val id: String,
    val title: String,
    val startArgb: Long,
    val endArgb: Long,
) {
    CYBER_BLURPLE(
        id = "CYBER_BLURPLE",
        title = "Blurple",
        startArgb = 0xFF5865F2,
        endArgb = 0xFFEC4899,
    ),
    CRIMSON_OPIUM(
        id = "CRIMSON_OPIUM",
        title = "Crimson",
        startArgb = 0xFFDC2626,
        endArgb = 0xFF450A0A,
    ),
    EMERALD_MATRIX(
        id = "EMERALD_MATRIX",
        title = "Matrix",
        startArgb = 0xFF10B981,
        endArgb = 0xFF0F766E,
    ),
    SUNSET_CHROME(
        id = "SUNSET_CHROME",
        title = "Sunset",
        startArgb = 0xFFF59E0B,
        endArgb = 0xFF9333EA,
    ),
    MIDNIGHT_OCEAN(
        id = "MIDNIGHT_OCEAN",
        title = "Ocean",
        startArgb = 0xFF2563EB,
        endArgb = 0xFF06B6D4,
    ),
    OBSIDIAN_NITRO(
        id = "OBSIDIAN_NITRO",
        title = "Obsidian",
        startArgb = 0xFF52525B,
        endArgb = 0xFF18181B,
    );
}

object ProfilePalette {
    val swatchColors: List<Long> = listOf(
        0xFF5865F2,
        0xFF8B5CF6,
        0xFFEC4899,
        0xFFEF4444,
        0xFFF97316,
        0xFFF59E0B,
        0xFF10B981,
        0xFF06B6D4,
        0xFF2563EB,
        0xFF1E293B,
        0xFF52525B,
        0xFF09090B,
    )

    private val DarkAnchor = Color(0xFF151318)

    fun bannerColors(startArgb: Long, endArgb: Long): List<Color> {
        val start = Color(startArgb)
        val end = Color(endArgb)
        val mid = lerp(start, end, 0.5f)
        return listOf(start, mid, end)
    }

    fun surfaceTopTint(startArgb: Long): Color =
        lerp(Color(startArgb), DarkAnchor, 0.52f)

    fun surfaceBottomTint(endArgb: Long): Color =
        lerp(Color(endArgb), DarkAnchor, 0.68f)
}
