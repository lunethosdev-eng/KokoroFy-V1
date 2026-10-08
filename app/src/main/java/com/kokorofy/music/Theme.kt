package com.kokorofy.music

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object KColors {
    val Green = Color(0xFF1ED760)
    val GreenDark = Color(0xFF1DB954)
    val Bg = Color(0xFF000000)
    val Surface = Color(0xFF121214)
    val LightBg = Color(0xFFF2F2F7)
    val LightSurface = Color(0xFFFFFFFF)
    val Text = Color(0xFFF5F5F7)
    val TextDark = Color(0xFF0A0A0C)
    val Muted = Color(0xFF8E8E93)
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
            tertiary = KColors.Muted
        ),
        content = content
    )
}

/**
 * LiquidGlass, DarkGlass → ver [LiquidGlass.kt]
 * (AGSL refraction + Fresnel + deformación elástica del borde).
 */

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
        corner = RoundedCornerShape(16.dp),
        interactive = true
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}
