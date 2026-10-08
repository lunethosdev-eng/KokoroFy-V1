package com.kokorofy.music

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/** AGSL refraction + Fresnel + specular (API 33+). Fallback layered glass otherwise. */
private val LIQUID_GLASS_AGSL = """
uniform shader composable;
uniform float2 resolution;
uniform float cornerRadius;
uniform float ior;
uniform float thickness;
uniform float specularPower;
uniform float edgeBoost;
uniform float lightBoost;
uniform float2 lightDir;
uniform float time;

float sdRoundBox(float2 p, float2 b, float r) {
    float2 q = abs(p) - b + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / resolution;
    float2 p = fragCoord - resolution * 0.5;
    float2 halfSize = resolution * 0.5 - 1.0;
    float r = min(cornerRadius, min(halfSize.x, halfSize.y));
    float d = sdRoundBox(p, halfSize, r);
    if (d > 0.5) {
        return half4(0.0);
    }
    float e = 1.5;
    float2 n = float2(
        sdRoundBox(p + float2(e, 0.0), halfSize, r) - sdRoundBox(p - float2(e, 0.0), halfSize, r),
        sdRoundBox(p + float2(0.0, e), halfSize, r) - sdRoundBox(p - float2(0.0, e), halfSize, r)
    );
    n = normalize(n + 1e-5);
    float maxD = min(halfSize.x, halfSize.y);
    float edge = smoothstep(-maxD * 0.55, 0.0, d);
    float rim = smoothstep(-8.0, 0.0, d);
    float2 refractOffset = n * (thickness * (0.35 + edge * 1.4) * (resolution.x * 0.012));
    float2 sampleUv = clamp(fragCoord - refractOffset, float2(1.0), resolution - 1.0);
    half4 bg = composable.eval(sampleUv);
    float bodyA = mix(0.08, 0.22, lightBoost) * (1.0 - rim * 0.55);
    half3 glassTint = half3(0.92, 0.95, 1.0);
    half3 body = mix(bg.rgb, glassTint, bodyA);
    float fresnel = pow(1.0 - abs(n.y) * 0.35 - abs(n.x) * 0.25, 2.2);
    fresnel = clamp(fresnel * (0.55 + edge * 1.6), 0.0, 1.0);
    float2 L = normalize(lightDir);
    float spec = pow(max(dot(n, L), 0.0), specularPower);
    float rimSpec = pow(rim, 1.4) * edgeBoost;
    half3 specular = half3(1.0) * (spec * 0.55 + rimSpec * 0.85 + fresnel * 0.4);
    specular *= mix(0.55, 1.15, edgeBoost);
    float sweep = 0.5 + 0.5 * sin(uv.x * 6.0 + uv.y * 3.0 + time * 0.8);
    half3 surface = half3(1.0) * (sweep * 0.06 * (0.4 + lightBoost));
    half3 col = body + specular + surface;
    float alpha = mix(0.55, 0.92, rim) * bg.a + (1.0 - bg.a) * mix(0.35, 0.7, lightBoost);
    alpha = clamp(alpha, 0.25, 0.95);
    return half4(col, alpha);
}
"""

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
    val density = LocalDensity.current

    val deformX = remember { Animatable(0f) }
    val deformY = remember { Animatable(0f) }
    val press = remember { Animatable(1f) }
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val safeIntensity = intensity.coerceIn(0.25f, 1f)
    val useAgsl = glassEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val cornerRadiusPx = with(density) { 22.dp.toPx() }

    val deformableShape = remember(deformX.value, deformY.value, cornerRadiusPx) {
        GenericShape { size, _ ->
            val w = size.width
            val h = size.height
            val r = min(cornerRadiusPx, min(w, h) / 2f)
            val maxPull = min(w, h) * 0.18f
            val pullX = deformX.value.coerceIn(-maxPull, maxPull)
            val pullY = deformY.value.coerceIn(-maxPull, maxPull)
            buildDeformedRoundedRect(this, w, h, r, pullX, pullY)
        }
    }

    val bodyAlpha = if (glassEnabled) {
        (if (dark) 0.10f else 0.42f) * safeIntensity
    } else {
        if (dark) 0.82f else 0.92f
    }
    val edgeAlpha = if (dark) 0.38f else 0.24f
    val body = Color.White.copy(alpha = bodyAlpha)
    val edge = Color.White.copy(alpha = edgeAlpha)

    Box(
        modifier
            .graphicsLayer {
                scaleX = press.value
                scaleY = press.value
                shadowElevation = with(density) { 10.dp.toPx() } *
                    (1f + hypot(deformX.value, deformY.value) / 80f)
            }
            .then(
                if (interactive) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                scope.launch { press.animateTo(0.97f, springSpec) }
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                val nextX = (deformX.value + amount.x * 0.55f).coerceIn(-72f, 72f)
                                val nextY = (deformY.value + amount.y * 0.55f).coerceIn(-72f, 72f)
                                scope.launch {
                                    deformX.snapTo(nextX)
                                    deformY.snapTo(nextY)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    launch { deformX.animateTo(0f, springSpec) }
                                    launch { deformY.animateTo(0f, springSpec) }
                                    launch { press.animateTo(1f, springSpec) }
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    launch { deformX.animateTo(0f, springSpec) }
                                    launch { deformY.animateTo(0f, springSpec) }
                                    launch { press.animateTo(1f, springSpec) }
                                }
                            }
                        )
                    }
                } else Modifier
            )
            .clip(deformableShape)
            .shadow(10.dp, deformableShape, clip = false)
            .then(
                if (useAgsl) {
                    Modifier.liquidGlassAgsl(
                        dark = dark,
                        intensity = safeIntensity,
                        cornerRadiusPx = cornerRadiusPx
                    )
                } else {
                    Modifier
                        .background(body)
                        .border(1.dp, edge, deformableShape)
                }
            )
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    val stroke = Stroke(width = 1.2f)
                    val rimColor = Color.White.copy(
                        alpha = if (dark) 0.28f * safeIntensity else 0.18f * safeIntensity
                    )
                    val r = min(cornerRadiusPx, min(size.width, size.height) / 2f)
                    drawRoundRect(
                        color = rimColor,
                        cornerRadius = CornerRadius(r, r),
                        style = stroke
                    )
                }
            }
    ) {
        if (!useAgsl && glassEnabled) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = if (dark) 0.20f else 0.48f),
                                Color.White.copy(alpha = if (dark) 0.05f else 0.12f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = if (dark) 0.18f else 0.28f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                Modifier
                    .matchParentSize()
                    .border(
                        0.5.dp,
                        Color.White.copy(alpha = if (dark) 0.18f else 0.28f),
                        deformableShape
                    )
            )
        }
        content()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Modifier.liquidGlassAgsl(
    dark: Boolean,
    intensity: Float,
    cornerRadiusPx: Float
): Modifier = this.then(
    Modifier.graphicsLayer {
        val shader = RuntimeShader(LIQUID_GLASS_AGSL)
        val w = size.width.coerceAtLeast(1f)
        val h = size.height.coerceAtLeast(1f)
        val r = min(cornerRadiusPx, min(w, h) / 2f)
        shader.setFloatUniform("resolution", w, h)
        shader.setFloatUniform("cornerRadius", r)
        shader.setFloatUniform("ior", 1.45f)
        shader.setFloatUniform("thickness", 1.15f * intensity)
        shader.setFloatUniform("specularPower", if (dark) 28f else 18f)
        shader.setFloatUniform("edgeBoost", if (dark) 1.35f else 0.55f)
        shader.setFloatUniform("lightBoost", if (dark) 0.25f else 1.0f)
        shader.setFloatUniform("lightDir", -0.55f, -0.75f)
        shader.setFloatUniform("time", (System.nanoTime() % 10_000_000_000L) / 1e9f)
        renderEffect = RenderEffect
            .createRuntimeShaderEffect(shader, "composable")
            .asComposeRenderEffect()
    }
)

private fun buildDeformedRoundedRect(
    path: Path,
    w: Float,
    h: Float,
    r: Float,
    pullX: Float,
    pullY: Float
) {
    val k = 0.5522847498f
    val left = 0f
    val top = 0f
    val right = w
    val bottom = h
    val bulgeR = max(0f, pullX)
    val bulgeL = max(0f, -pullX)
    val bulgeB = max(0f, pullY)
    val bulgeT = max(0f, -pullY)
    val tl = Offset(left + r, top + r)
    val tr = Offset(right - r, top + r)
    val br = Offset(right - r, bottom - r)
    val bl = Offset(left + r, bottom - r)

    path.moveTo(tl.x, top)
    path.cubicTo(
        tl.x + (tr.x - tl.x) * 0.33f, top - bulgeT * 0.85f,
        tl.x + (tr.x - tl.x) * 0.66f, top - bulgeT * 0.85f,
        tr.x, top
    )
    path.cubicTo(tr.x + r * k, top, right, tr.y - r * k, right, tr.y)
    path.cubicTo(
        right + bulgeR * 0.85f, tr.y + (br.y - tr.y) * 0.33f,
        right + bulgeR * 0.85f, tr.y + (br.y - tr.y) * 0.66f,
        right, br.y
    )
    path.cubicTo(right, br.y + r * k, br.x + r * k, bottom, br.x, bottom)
    path.cubicTo(
        br.x - (br.x - bl.x) * 0.33f, bottom + bulgeB * 0.85f,
        br.x - (br.x - bl.x) * 0.66f, bottom + bulgeB * 0.85f,
        bl.x, bottom
    )
    path.cubicTo(bl.x - r * k, bottom, left, bl.y + r * k, left, bl.y)
    path.cubicTo(
        left - bulgeL * 0.85f, bl.y - (bl.y - tl.y) * 0.33f,
        left - bulgeL * 0.85f, bl.y - (bl.y - tl.y) * 0.66f,
        left, tl.y
    )
    path.cubicTo(left, tl.y - r * k, tl.x - r * k, top, tl.x, top)
    path.close()
}

@Composable
fun DarkGlass(
    modifier: Modifier = Modifier,
    corner: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlass(modifier = modifier, dark = true, corner = corner, content = content)
}
