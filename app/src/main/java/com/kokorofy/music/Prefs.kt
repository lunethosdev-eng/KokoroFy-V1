package com.kokorofy.music

import android.content.Context

/** Preferencias reales de audio / privacidad. */
object Prefs {
    private const val NAME = "kokorofy_prefs"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun playbackSpeed(ctx: Context): Float = sp(ctx).getFloat("speed", 1f)
    fun setPlaybackSpeed(ctx: Context, v: Float) = sp(ctx).edit().putFloat("speed", v).apply()

    fun crossfadeMs(ctx: Context): Int = sp(ctx).getInt("crossfade", 0)
    fun setCrossfadeMs(ctx: Context, v: Int) = sp(ctx).edit().putInt("crossfade", v).apply()

    fun gapless(ctx: Context): Boolean = sp(ctx).getBoolean("gapless", true)
    fun setGapless(ctx: Context, v: Boolean) = sp(ctx).edit().putBoolean("gapless", v).apply()

    fun skipSilence(ctx: Context): Boolean = sp(ctx).getBoolean("skip_silence", false)
    fun setSkipSilence(ctx: Context, v: Boolean) = sp(ctx).edit().putBoolean("skip_silence", v).apply()

    fun gyro(ctx: Context): Boolean = sp(ctx).getBoolean("gyro", false)
    fun setGyro(ctx: Context, v: Boolean) = sp(ctx).edit().putBoolean("gyro", v).apply()

    fun reduceMotion(ctx: Context): Boolean = sp(ctx).getBoolean("reduce_motion", false)
    fun setReduceMotion(ctx: Context, v: Boolean) = sp(ctx).edit().putBoolean("reduce_motion", v).apply()

    fun accentIndex(ctx: Context): Int = sp(ctx).getInt("accent", 0)
    fun setAccentIndex(ctx: Context, v: Int) = sp(ctx).edit().putInt("accent", v).apply()

    fun clearAll(ctx: Context) = sp(ctx).edit().clear().apply()
}
