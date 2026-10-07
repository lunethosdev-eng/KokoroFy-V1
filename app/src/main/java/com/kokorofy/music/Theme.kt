package com.kokorofy.music

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
 * Liquid Glass sutil + bouncy press.
 * Sin drag continuo (evita freezes). Solo escala al tocar y vuelve con spring.
 * Color sólido semitransparente, sin gradientes blancos.
 */
@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    dark: Boolean = true,
    corner: RoundedCornerShape = RoundedCornerShape(22.dp),
    interactive: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val body = if (dark) Color(0xFF1C1C1E).copy(alpha = 0.78f) else Color.White.copy(alpha = 0.82f)
    val borderColor = if (dark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f)
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    Box(
        modifier
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .then(
                if (interactive) Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            scope.launch { scale.animateTo(0.96f, springSpec) }
                            tryAwaitRelease()
                            scope.launch { scale.animateTo(1f, springSpec) }
                        }
                    )
                } else Modifier
            )
            .clip(corner)
            .background(body)
            .border(width = 0.7.dp, color = borderColor, shape = corner)
    ) {
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
