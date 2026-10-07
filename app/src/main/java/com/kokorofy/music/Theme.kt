package com.kokorofy.music

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Paleta Spotify-like — sin púrpura. Liquid Glass realista. */
object KColors {
    // Accent
    val Green = Color(0xFF1ED760)
    val GreenDark = Color(0xFF1DB954)

    // Dark mode (casi negro como iOS 26 concept)
    val Bg = Color(0xFF000000)
    val BgElevated = Color(0xFF0A0A0C)
    val Surface = Color(0xFF121214)
    val Surface2 = Color(0xFF1A1A1E)

    // Light mode
    val LightBg = Color(0xFFF5F5F7)
    val LightSurface = Color(0xFFFFFFFF)
    val LightElevated = Color(0xFFF0F0F2)

    // Text
    val Text = Color(0xFFF5F5F7)
    val TextDark = Color(0xFF0A0A0C)
    val Muted = Color(0xFF8E8E93)
    val MutedLight = Color(0xFF6B6B70)

    // Glass (translucent white layers — no tint)
    val GlassTop = Color(0x40FFFFFF)
    val GlassBody = Color(0x22FFFFFF)
    val GlassBodyLight = Color(0x99FFFFFF)
    val GlassBorder = Color(0x55FFFFFF)
    val GlassBorderLight = Color(0xBBFFFFFF)
    val GlassHighlight = Color(0x66FFFFFF)
}

@Composable
fun KokoroDarkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = KColors.Bg,
            surface = KColors.Surface,
            primary = KColors.Green,
            onBackground = KColors.Text,
            onSurface = KColors.Text,
            onPrimary = Color.Black,
            secondary = KColors.GreenDark,
            tertiary = KColors.Muted
        ),
        content = content
    )
}

@Composable
fun KokoroLightTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = KColors.LightBg,
            surface = KColors.LightSurface,
            primary = KColors.Green,
            onBackground = KColors.TextDark,
            onSurface = KColors.TextDark,
            onPrimary = Color.Black,
            secondary = KColors.GreenDark,
            tertiary = KColors.MutedLight
        ),
        content = content
    )
}

/**
 * Liquid Glass realista — estilo iOS 26 / Spotify concept.
 * Capas: highlight superior + cuerpo translúcido + borde luminoso + sin tintes de color.
 * Funciona en dark y light.
 */
@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    dark: Boolean = true,
    corner: RoundedCornerShape = RoundedCornerShape(22.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val bodyColors = if (dark) {
        listOf(
            Color(0x38FFFFFF),
            Color(0x1AFFFFFF),
            Color(0x12FFFFFF)
        )
    } else {
        listOf(
            Color(0xCCFFFFFF),
            Color(0xAAFFFFFF),
            Color(0x88FFFFFF)
        )
    }
    val borderBrush = if (dark) {
        Brush.linearGradient(
            listOf(
                Color(0x77FFFFFF),
                Color(0x33FFFFFF),
                Color(0x22FFFFFF)
            ),
            start = Offset.Zero,
            end = Offset(400f, 600f)
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xEEFFFFFF),
                Color(0xAAFFFFFF),
                Color(0x66FFFFFF)
            ),
            start = Offset.Zero,
            end = Offset(400f, 600f)
        )
    }

    Box(
        modifier
            .clip(corner)
            .background(
                Brush.linearGradient(
                    colors = bodyColors,
                    start = Offset.Zero,
                    end = Offset(500f, 800f)
                )
            )
            .border(width = 1.dp, brush = borderBrush, shape = corner)
    ) {
        // Highlight superior (reflejo de vidrio)
        Box(
            Modifier
                .fillMaxWidth()
                .height(32.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (dark) Color(0x40FFFFFF) else Color(0x66FFFFFF),
                            Color.Transparent
                        )
                    )
                )
        )
        content()
    }
}

/** Alias por compatibilidad */
@Composable
fun DarkGlass(
    modifier: Modifier = Modifier,
    corner: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlass(modifier = modifier, dark = true, corner = corner, content = content)
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dark: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    LiquidGlass(
        modifier = modifier.clickable(onClick = onClick),
        dark = dark,
        corner = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}
