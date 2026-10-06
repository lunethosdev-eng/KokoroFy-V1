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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Paleta oscura tipo Apple Music / Spotify */
object KColors {
    val Bg = Color(0xFF0B0B0F)
    val BgElevated = Color(0xFF14141A)
    val Purple = Color(0xFF8B7CFF)
    val Text = Color(0xFFF2F2F7)
    val Muted = Color(0xFF9A9AA8)
    val GlassTop = Color(0x33FFFFFF)
    val GlassBody = Color(0x22FFFFFF)
    val GlassBorder = Color(0x44FFFFFF)
}

@Composable
fun KokoroDarkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = KColors.Bg,
            surface = KColors.BgElevated,
            primary = KColors.Purple,
            onBackground = KColors.Text,
            onSurface = KColors.Text,
            onPrimary = Color.White
        ),
        content = content
    )
}

/** Liquid Glass oscuro (botones, mini player, cards — no el texto suelto) */
@Composable
fun DarkGlass(
    modifier: Modifier = Modifier,
    corner: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier
            .clip(corner)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0x44FFFFFF),
                        Color(0x22FFFFFF),
                        Color(0x14A78BFA)
                    ),
                    start = Offset.Zero,
                    end = Offset(600f, 900f)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0x66FFFFFF),
                        Color(0x22FFFFFF),
                        Color(0x448B7CFF)
                    )
                ),
                shape = corner
            )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(28.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x33FFFFFF), Color.Transparent)
                    )
                )
        )
        content()
    }
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    DarkGlass(
        modifier = modifier.clickable(onClick = onClick),
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
