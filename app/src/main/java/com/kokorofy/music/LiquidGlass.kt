package com.kokorofy.music

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot

/**
 * Liquid Glass limpio estilo Apple / Spotify.
 *
 * NO usa RuntimeShader sobre el contenido (eso destruia el texto con artefactos en X).
 * Capas:
 *  1) Relleno translucido adaptativo claro/oscuro
 *  2) Highlight vertical superior (specular)
 *  3) Borde fino claro
 *  4) Sombra suave
 *  5) Deformacion elastica opcional al arrastrar
 */
@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    dark: Boolean = true,
    corner: RoundedCornerShape = RoundedCornerShape(22.dp),
    interactive: Boolean = false,
    intensity: Float = 0.82f,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val glassEnabled = FeaturePrefs.get(context, "ui.glass", true)
    val scope = rememberCoroutineScope()

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val press = remember { Animatable(1f) }
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val safe = intensity.coerceIn(0.3f, 1f)

    // Fondos legibles: nunca tan grises que maten el contraste del texto
    val fill = when {
        !glassEnabled && dark -> Color(0xFF1C1C1E)
        !glassEnabled && !dark -> Color(0xFFFFFFFF)
        dark -> Color.White.copy(alpha = 0.10f * safe + 0.04f)
        else -> Color.White.copy(alpha = 0.72f * safe + 0.12f)
    }
    val borderColor = if (dark) {
        Color.White.copy(alpha = 0.22f * safe)
    } else {
        Color.White.copy(alpha = 0.85f)
    }
    val topHighlight = if (dark) {
        Color.White.copy(alpha = 0.16f * safe)
    } else {
        Color.White.copy(alpha = 0.55f * safe)
    }

    Box(
        modifier
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                val dx = offsetX.value / 40f
                val dy = offsetY.value / 40f
                scaleX = press.value * (1f + abs(dx) * 0.03f)
                scaleY = press.value * (1f + abs(dy) * 0.03f)
                shadowElevation = 6.dp.toPx() * (1f + hypot(offsetX.value, offsetY.value) / 100f)
            }
            .then(
                if (interactive) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                scope.launch { press.animateTo(0.98f, springSpec) }
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                val nx = (offsetX.value + amount.x * 0.35f).coerceIn(-28f, 28f)
                                val ny = (offsetY.value + amount.y * 0.35f).coerceIn(-28f, 28f)
                                scope.launch {
                                    offsetX.snapTo(nx)
                                    offsetY.snapTo(ny)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    launch { offsetX.animateTo(0f, springSpec) }
                                    launch { offsetY.animateTo(0f, springSpec) }
                                    launch { press.animateTo(1f, springSpec) }
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    launch { offsetX.animateTo(0f, springSpec) }
                                    launch { offsetY.animateTo(0f, springSpec) }
                                    launch { press.animateTo(1f, springSpec) }
                                }
                            }
                        )
                    }
                } else Modifier
            )
            .shadow(if (dark) 8.dp else 4.dp, corner, clip = false, ambientColor = Color.Black.copy(0.18f), spotColor = Color.Black.copy(0.22f))
            .clip(corner)
            .background(fill)
            .border(width = 1.dp, color = borderColor, shape = corner)
    ) {
        // Specular superior (no tapa el texto: solo franja alta)
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to topHighlight,
                            0.35f to Color.Transparent,
                            1.0f to Color.Transparent
                        )
                    )
                )
        )
        // Borde interior sutil
        Box(
            Modifier
                .matchParentSize()
                .border(
                    width = 0.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (dark) 0.28f else 0.9f),
                            Color.White.copy(alpha = if (dark) 0.06f else 0.25f)
                        )
                    ),
                    shape = corner
                )
        )
        content()
    }
}

@Composable
fun DarkGlass(
    modifier: Modifier = Modifier,
    corner: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlass(modifier = modifier, dark = true, corner = corner, content = content)
}
