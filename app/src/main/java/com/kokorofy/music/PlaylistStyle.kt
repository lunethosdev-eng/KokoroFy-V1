package com.kokorofy.music

import android.content.Context
import androidx.compose.ui.graphics.Color

/**
 * Per-playlist visual + playback customization.
 * Color accent, shuffle-on-open, description, pinned.
 */
object PlaylistStyle {
    private const val N = "kokorofy_playlist_style_v1"
    private fun sp(c: Context) = c.getSharedPreferences(N, Context.MODE_PRIVATE)

    val accentPalette = listOf(
        0xFF1ED760L, // Spotify green
        0xFFFA2D48L, // Apple Music red
        0xFFFC3C44L,
        0xFF007AFFL, // iOS blue
        0xFFAF52DEL, // purple
        0xFFFF9500L, // orange
        0xFF5856D6L, // indigo
        0xFFFF2D55L, // pink
        0xFF34C759L, // system green
        0xFF64D2FFL, // cyan
        0xFFFFD60AL, // yellow
        0xFF8E8E93L, // gray
    )

    fun accent(c: Context, playlistId: String): Long =
        sp(c).getLong("accent_$playlistId", accentPalette[0])

    fun setAccent(c: Context, playlistId: String, color: Long) =
        sp(c).edit().putLong("accent_$playlistId", color).apply()

    fun accentColor(c: Context, playlistId: String): Color =
        Color(accent(c, playlistId))

    fun shuffleOnOpen(c: Context, playlistId: String) =
        sp(c).getBoolean("shuffle_$playlistId", false)

    fun setShuffleOnOpen(c: Context, playlistId: String, v: Boolean) =
        sp(c).edit().putBoolean("shuffle_$playlistId", v).apply()

    fun description(c: Context, playlistId: String) =
        sp(c).getString("desc_$playlistId", "") ?: ""

    fun setDescription(c: Context, playlistId: String, v: String) =
        sp(c).edit().putString("desc_$playlistId", v).apply()

    fun pinned(c: Context, playlistId: String) =
        sp(c).getBoolean("pin_$playlistId", false)

    fun setPinned(c: Context, playlistId: String, v: Boolean) =
        sp(c).edit().putBoolean("pin_$playlistId", v).apply()

    fun coverOverride(c: Context, playlistId: String): String? =
        sp(c).getString("cover_$playlistId", null)?.takeIf { it.isNotBlank() }

    fun setCoverOverride(c: Context, playlistId: String, uri: String?) =
        sp(c).edit().putString("cover_$playlistId", uri).apply()
}
