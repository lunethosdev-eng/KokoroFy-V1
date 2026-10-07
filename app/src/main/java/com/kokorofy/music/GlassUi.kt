package com.kokorofy.music

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/**
 * Overlay de descarga con porcentaje REAL en tiempo real.
 * Usa Liquid Glass y muestra barra de progreso + % grande.
 */
@Composable
fun DownloadProgressOverlay(state: OfflineManager.Progress) {
    if (!state.active) return
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .zIndex(100f)
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        val corner = RoundedCornerShape(28.dp)
        Box(
            Modifier
                .padding(40.dp)
                .widthIn(max = 300.dp)
                .fillMaxWidth()
                .clip(corner)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x40FFFFFF),
                            Color(0x22FFFFFF),
                            Color(0x18FFFFFF)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.12f))
                    ),
                    corner
                )
        ) {
            Column(
                Modifier.padding(horizontal = 28.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    when {
                        state.error != null -> "Error"
                        state.done -> "¡Listo!"
                        else -> "Descargando"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                if (state.title.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.title,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(18.dp))
                // Porcentaje grande
                Text(
                    if (state.error != null) "!" else "${state.percent}%",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.error != null) Color(0xFFFF6B6B) else Color(0xFF1ED760)
                )
                if (state.error == null && !state.done) {
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { state.percent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF1ED760),
                        trackColor = Color.White.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round
                    )
                }
                if (state.error != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        state.error!!,
                        fontSize = 12.sp,
                        color = Color(0xFFFFAAAA),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
