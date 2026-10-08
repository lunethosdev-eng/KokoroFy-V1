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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * AGSL shader: refractivo + Fresnel + specular.
 *
 * - Uniform `composable`: capa de contenido (backdrop / contenido del glass).
 * - SDF de caja redondeada → normal N → distorsión UV (índice ~1.45).
 * - Fresnel en bordes + highlight especular adaptativo claro/oscuro.
 *
 * Requiere API 33+. En APIs inferiores se usa el fallback multicapa.
 */
private const val LIQUID_GLASS_AGSL = """
uniform shader composable;
uniform float2 resolution;
uniform float cornerRadius;
uniform float ior;          // ~1.45
uniform float thickness;    // intensidad de refracción
uniform float specularPower;
uniform float edgeBoost;    // más alto en dark mode
uniform float lightBoost;   // más alto en light mode
uniform float2 lightDir;    // dirección de luz especular normalizada
uniform float time;

// SDF rounded box (centro en 0, mitad half-size)
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

    // Dentro del cristal
    if (d > 0.5) {
        return half4(0.0);
    }

    // Normal aproximada vía gradiente del SDF
    float e = 1.5;
    float2 n = float2(
        sdRoundBox(p + float2(e, 0.0), halfSize, r) - sdRoundBox(p - float2(e, 0.0), halfSize, r),
        sdRoundBox(p + float2(0.0, e), halfSize, r) - sdRoundBox(p - float2(0.0, e), halfSize, r)
    );
    n = normalize(n + 1e-5);

    // Distancia normalizada al borde (0 centro → 1 borde)
    float maxD = min(halfSize.x, halfSize.y);
    float edge = smoothstep(-maxD * 0.55, 0.0, d);
    float rim = smoothstep(-8.0, 0.0, d);

    // Refracción tipo Snell simplificada (n_air=1, n_glass=ior)
    float eta = 1.0 / ior;
    float2 view = float2(0.0, 0.0); // ortográfico
   
... 
