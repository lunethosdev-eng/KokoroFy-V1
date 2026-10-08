package com.kokorofy.music

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Emotion inferred from simple word heuristics for color/font variation. */
enum class LyricEmotion {
    Neutral, Joy, Sad, Intense, Soft, Love
}

private val emotionColor = mapOf(
    LyricEmotion.Neutral to Color(0xFFE8E8ED),
    LyricEmotion.Joy to Color(0xFFFFD60A),
    LyricEmotion.Sad to Color(0xFF64D2FF),
    LyricEmotion.Intense to Color(0xFFFF453A),
    LyricEmotion.Soft to Color(0xFFBF5AF2),
    LyricEmotion.Love to Color(0xFFFF2D55),
)

private val joyWords = setOf("love", "happy", "sun", "dance", "free", "alive", "smile", "light", "amor", "feliz", "baila", "sol")
private val sadWords = setOf("cry", "tears", "alone", "gone", "miss", "hurt", "llorar", "solo", "falta", "dolor", "adios")
private val intenseWords = setOf("fire", "burn", "scream", "fight", "power", "fuego", "grita", "fuerza", "rage")
private val softWords = setOf("whisper", "dream", "night", "slow", "quiet", "suave", "noche", "sueno", "calma")
private val loveWords = setOf("heart", "kiss", "baby", "darling", "corazon", "beso", "querido", "amada")

fun detectEmotion(word: String): LyricEmotion {
    val w = word.lowercase().trim().filter { it.isLetter() }
    return when {
        w in loveWords || w.contains("love") || w.contains("amor") -> LyricEmotion.Love
        w in joyWords -> LyricEmotion.Joy
        w in sadWords -> LyricEmotion.Sad
        w in intenseWords -> LyricEmotion.Intense
        w in softWords -> LyricEmotion.Soft
        else -> LyricEmotion.Neutral
    }
}

/**
 * Karaoke lyrics:
 * - active line advances smoothly
 * - character fill at ~1.4x relative to line duration
 * - per-word emotion color + weight/style
 */
@Composable
fun KaraokeLyricsView(
    lines: List<LyricLine>,
    positionMs: Long,
    dark: Boolean,
    modifier: Modifier = Modifier,
    speedFactor: Float = 1.4f
) {
    val listState = rememberLazyListState()
    val activeIndex = remember(lines, positionMs) {
        if (lines.isEmpty()) -1
        else {
            var idx = 0
            for (i in lines.indices) {
                if (lines[i].timeMs <= positionMs) idx = i else break
            }
            idx
        }
    }

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            runCatching {
                listState.animateScrollToItem(maxOf(0, activeIndex - 1))
            }
        }
    }

    val muted = if (dark) Color.White.copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.28f)
    val upcoming = if (dark) Color.White.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.55f)

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(lines) { index, line ->
            val isActive = index == activeIndex
            val isPast = index < activeIndex
            AnimatedContent(
                targetState = isActive,
                transitionSpec = {
                    (fadeIn(tween(280, easing = FastOutSlowInEasing)) +
                        slideInVertically(tween(320)) { it / 6 }) togetherWith
                        (fadeOut(tween(200)) + slideOutVertically(tween(220)) { -it / 8 })
                },
                label = "lyric-line-$index"
            ) { active ->
                if (active) {
                    ActiveLyricLine(
                        line = line,
                        nextTimeMs = lines.getOrNull(index + 1)?.timeMs ?: (line.timeMs + 4000),
                        positionMs = positionMs,
                        speedFactor = speedFactor,
                        dark = dark
                    )
                } else {
                    Text(
                        text = line.text,
                        fontSize = if (isPast) 16.sp else 18.sp,
                        fontWeight = if (isPast) FontWeight.Normal else FontWeight.Medium,
                        color = if (isPast) muted else upcoming,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveLyricLine(
    line: LyricLine,
    nextTimeMs: Long,
    positionMs: Long,
    speedFactor: Float,
    dark: Boolean
) {
    val duration = ((nextTimeMs - line.timeMs).coerceAtLeast(800) / speedFactor).toLong()
    val elapsed = (positionMs - line.timeMs).coerceAtLeast(0L)
    val progress = (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    val text = line.text
    val totalChars = text.length.coerceAtLeast(1)
    val filled = (progress * totalChars).roundToInt().coerceIn(0, totalChars)

    val words = remember(text) { text.split(Regex("(?<=\\s)|(?=\\s)")) }
    var charCursor = 0

    val annotated = buildAnnotatedString {
        words.forEach { token ->
            val emotion = if (token.isBlank()) LyricEmotion.Neutral else detectEmotion(token)
            val baseColor = emotionColor[emotion] ?: Color.White
            val weight = when (emotion) {
                LyricEmotion.Intense, LyricEmotion.Love -> FontWeight.Bold
                LyricEmotion.Joy -> FontWeight.SemiBold
                LyricEmotion.Soft -> FontWeight.Light
                else -> FontWeight.Medium
            }
            val style = when (emotion) {
                LyricEmotion.Soft -> FontStyle.Italic
                else -> FontStyle.Normal
            }
            token.forEach { ch ->
                val on = charCursor < filled
                charCursor++
                withStyle(
                    SpanStyle(
                        color = if (on) baseColor else baseColor.copy(alpha = 0.28f),
                        fontWeight = weight,
                        fontStyle = style,
                        fontSize = 26.sp
                    )
                ) {
                    append(ch)
                }
            }
        }
    }

    Text(
        text = annotated,
        textAlign = TextAlign.Center,
        lineHeight = 34.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

/** Parse simple LRC into LyricLine list. */
fun parseLrc(raw: String?): List<LyricLine> {
    if (raw.isNullOrBlank()) return emptyList()
    val re = Regex("""\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?](.*)""")
    return raw.lineSequence().mapNotNull { line ->
        val m = re.find(line.trim()) ?: return@mapNotNull null
        val min = m.groupValues[1].toLong()
        val sec = m.groupValues[2].toLong()
        val frac = m.groupValues[3].padEnd(3, '0').take(3).toLongOrNull() ?: 0L
        val text = m.groupValues[4].trim()
        if (text.isBlank()) null else LyricLine(min * 60_000 + sec * 1000 + frac, text)
    }.sortedBy { it.timeMs }.toList()
}
