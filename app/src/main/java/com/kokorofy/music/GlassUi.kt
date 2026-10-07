package com.kokorofy.music

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/**
 * Liquid Glass estilo iOS 26 / Spotify concept:
 * - capa frosted semitransparente
 * - borde fino luminoso
 * - highlight superior suave
 * sin gradientes chillones
 */
@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    corner: RoundedCornerShape = RoundedCornerShape(24.dp),
    intense: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.3f
    val fill = if (dark) {
        if (intense) Color(0xCC1C1C22) else Color(0x991C1C22)
    } else {
        if (intense) Color(0xE6FFFFFF) else Color(0xCCFFFFFF)
    }
    val edge = if (dark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.55f)
    val topShine = if (dark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.45f)

    Box(
        modifier
            .clip(corner)
            .background(fill)
            .border(0.8.dp, edge, corner)
    ) {
        // specular top (liquid glass)
        Box(
            Modifier
                .fillMaxWidth()
                .height(28.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(listOf(topShine, Color.Transparent))
                )
        )
        content()
    }
}

private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

/** Overlay de descarga minimal: solo texto + % (sin gradientes). */
@Composable
fun DownloadProgressOverlay(state: OfflineManager.Progress) {
    if (!state.active) return
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .zIndex(40f)
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        LiquidGlass(
            modifier = Modifier
                .padding(32.dp)
                .widthIn(max = 280.dp)
                .fillMaxWidth(),
            corner = RoundedCornerShape(20.dp),
            intense = true
        ) {
            Column(
                Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (state.error != null) "Error" else if (state.done) "Listo" else "Descargando",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (state.title.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        state.title,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    if (state.error != null) state.error!! else "${state.percent}%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun GlassChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) Color(0xFF1ED760) else Color.Transparent
    val fg = if (selected) Color.Black else MaterialTheme.colorScheme.onSurface
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) bg else Color.White.copy(alpha = 0.08f))
            .border(
                0.8.dp,
                if (selected) bg else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
