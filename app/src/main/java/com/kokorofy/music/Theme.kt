package com.kokorofy.music

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Paleta Spotify — sin púrpura, sin blancos agresivos. */
object KColors {
    val Green = Color(0xFF1ED760)
    val GreenDark = Color(0xFF1DB954)

    val Bg = Color(0xFF000000)
    val BgElevated = Color(0xFF0A0A0C)
    val Surface = Color(0xFF121214)
    val Surface2 = Color(0xFF1A1A1E)

    val LightBg = Color(0xFFF5F5F7)
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
 * Liquid Glass sutil — dark mode real.
 * Sin gradientes blancos pesados. Solo un velo oscuro + borde fino.
 * Animación: al arrastrar se deforma (offset + scale) y al soltar vuelve con spring bouncy.
 */
@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    dark: Boolean = true,
    corner: RoundedCornerShape = RoundedCornerShape(22.dp),
    interactive: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    val body = if (dark) Color(0xFF1C1C1E).copy(alpha = 0.72f) else Color.White.copy(alpha = 0.78f)
    val borderColor = if (dark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    Box(
        modifier
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                scaleX = scale.value
                scaleY = scale.value
            }
            .then(
                if (interactive) Modifier.pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            scope.launch { scale.animateTo(1.04f, springSpec) }
                        },
                        onDragEnd = {
                            scope.launch {
                                launch { offsetX.animateTo(0f, springSpec) }
                                launch { offsetY.animateTo(0f, springSpec) }
                                launch { scale.animateTo(1f, springSpec) }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                launch { offsetX.animateTo(0f, springSpec) }
                                launch { offsetY.animateTo(0f, springSpec) }
                                launch { scale.animateTo(1f, springSpec) }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // efecto "gota": el desplazamiento se amortigua (líquido)
                            scope.launch {
                                offsetX.snapTo((offsetX.value + dragAmount.x * 0.35f).coerceIn(-28f, 28f))
                                offsetY.snapTo((offsetY.value + dragAmount.y * 0.35f).coerceIn(-28f, 28f))
                            }
                        }
                    )
                } else Modifier
            )
            .clip(corner)
            .background(body)
            .border(width = 0.8.dp, color = borderColor, shape = corner)
    ) {
        content()
    }
}

/** Alias */
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
        corner = RoundedCornerShape(16.dp),
        interactive = false
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}
