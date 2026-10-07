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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

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
 * Native Compose Liquid Glass:
 * - translucent body
 * - specular highlight
 * - thin inner/outer edge
 * - bounded drag with a droplet/stretch effect
 * - spring return to the exact origin
 *
 * Android/Compose does not expose iOS's private backdrop-refraction shader,
 * so this uses native GPU layers and transparency rather than pretending a
 * CSS backdrop-filter is available.
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
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val stretch = remember { Animatable(0f) }
    val press = remember { Animatable(1f) }
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val safeIntensity = intensity.coerceIn(0.25f, 1f)
    val glassEnabled = FeaturePrefs.get(LocalContext.current, "ui.glass", true)
    val bodyAlpha = if (glassEnabled) {
        (if (dark) 0.12f else 0.48f) * safeIntensity
    } else {
        if (dark) 0.82f else 0.92f
    }
    val edgeAlpha = if (dark) 0.30f else 0.18f
    val body = Color.White.copy(alpha = bodyAlpha)
    val edge = Color.White.copy(alpha = edgeAlpha)

    Box(
        modifier
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                val s = stretch.value
                scaleX = press.value * (1f + s * 0.045f)
                scaleY = press.value * (1f - s * 0.020f)
                shadowElevation = 7.dp.toPx() * (1f + abs(s))
            }
            .then(
                if (interactive) Modifier.pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            scope.launch { press.animateTo(0.985f, springSpec) }
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            val nextX = (offsetX.value + amount.x * 0.62f).coerceIn(-48f, 48f)
                            val nextY = (offsetY.value + amount.y * 0.62f).coerceIn(-48f, 48f)
                            offsetX.snapTo(nextX)
                            offsetY.snapTo(nextY)
                            val edgeX = abs(nextX) / 48f
                            val edgeY = abs(nextY) / 48f
                            stretch.snapTo(maxOf(edgeX, edgeY).coerceIn(0f, 1f))
                        },
                        onDragEnd = {
                            scope.launch {
                                launch { offsetX.animateTo(0f, springSpec) }
                                launch { offsetY.animateTo(0f, springSpec) }
                                launch { stretch.animateTo(0f, springSpec) }
                                launch { press.animateTo(1f, springSpec) }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                launch { offsetX.animateTo(0f, springSpec) }
                                launch { offsetY.animateTo(0f, springSpec) }
                                launch { stretch.animateTo(0f, springSpec) }
                                launch { press.animateTo(1f, springSpec) }
                            }
                        }
                    )
                } else Modifier
            )
            .clip(corner)
            .shadow(8.dp, corner, clip = false)
            .background(body)
            .border(1.dp, edge, corner)
    ) {
        // Refraction-like highlights are layered inside the glass so the surface
        // keeps its depth on devices where true backdrop blur is unavailable.
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (dark) 0.16f else 0.42f),
                            Color.White.copy(alpha = if (dark) 0.035f else 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            Modifier
                .matchParentSize()
                .border(0.5.dp, Color.White.copy(alpha = if (dark) 0.16f else 0.26f), corner)
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
