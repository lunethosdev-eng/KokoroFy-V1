package com.kokorofy.music

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/** Overlay de descarga minimal — no redefine LiquidGlass. */
@Composable
fun DownloadProgressOverlay(state: OfflineManager.Progress) {
    if (!state.active) return
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .zIndex(50f)
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        val corner = RoundedCornerShape(20.dp)
        Box(
            Modifier
                .padding(36.dp)
                .widthIn(max = 260.dp)
                .fillMaxWidth()
                .clip(corner)
                .background(Color.White.copy(alpha = 0.18f))
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.08f))
                    ),
                    corner
                )
        ) {
            Column(
                Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    when {
                        state.error != null -> "Error"
                        state.done -> "Listo"
                        else -> "Descargando"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                if (state.title.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(state.title, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), maxLines = 2)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (state.error != null) state.error!! else "${state.percent}%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}
