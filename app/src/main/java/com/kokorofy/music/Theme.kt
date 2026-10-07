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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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
 * Liquid Glass REAL — translúcido, no sólido negro.
 * Capas: velo semitransparente + borde luminoso fino.
 * Si interactive=true: al arrastrar se deforma (gota) y al soltar rebota con spring.
 */
@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    dark: Boolean = true,
    corner: RoundedCornerShape = RoundedCornerShape(22.dp),
    interactive: Boolean = false,
    intensity: Float = 0.55f, // 0..1 controlable desde settings
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    // Translúcido real: en dark es blanco muy suave, en light es blanco semi
    val bodyAlpha = (if (dark) 0.14f else 0.55f) * intensity.coerceIn(0.2f, 1f)
    val body = Color.White.copy(alpha = bodyAlpha)
    val borderAlpha = if (dark) 0.22f else 0.12f
    val borderColor = Color.White.copy(alpha = borderAlpha)

    val springSpec = spring<Float>(
        dampingRatio = 0.42f, // más bouncy
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
                            scope.launch { scale.animateTo(1.06f, springSpec) }
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
                        onDrag = { change, amount ->
                            change.consume()
                            // gota: amortigua el movimiento
                            scope.launch {
                                offsetX.snapTo((offsetX.value + amount.x * 0.4f).coerceIn(-36f, 36f))
                                offsetY.snapTo((offsetY.value + amount.y * 0.4f).coerceIn(-36f, 36f))
                            }
                        }
                    )
                } else Modifier
            )
            .clip(corner)
            .background(body)
            .border(width = 0.9.dp, color = borderColor, shape = corner)
    ) {
        // highlight superior sutil (reflejo de cristal)
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (dark) 0.10f else 0.35f),
                            Color.Transparent
                        )
                    )
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
