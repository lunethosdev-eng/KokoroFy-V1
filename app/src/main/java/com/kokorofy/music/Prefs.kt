package com.kokorofy.music

import android.content.Context

/** Core preferences for playback, appearance and privacy. Extended feature flags live in FeaturePrefs. */
object Prefs {
    private const val N = "kokorofy_prefs_v5"
    private fun sp(c: Context) = c.getSharedPreferences(N, Context.MODE_PRIVATE)

    // ── Audio (real) ──
    fun speed(c: Context) = sp(c).getFloat("speed", 1f)
    fun setSpeed(c: Context, v: Float) = sp(c).edit().putFloat("speed", v).apply()

    fun crossfadeMs(c: Context) = sp(c).getInt("crossfade", 0)
    fun setCrossfadeMs(c: Context, v: Int) = sp(c).edit().putInt("crossfade", v).apply()

    fun gapless(c: Context) = sp(c).getBoolean("gapless", true)
    fun setGapless(c: Context, v: Boolean) = sp(c).edit().putBoolean("gapless", v).apply()

    fun skipSilence(c: Context) = sp(c).getBoolean("skip_silence", false)
    fun setSkipSilence(c: Context, v: Boolean) = sp(c).edit().putBoolean("skip_silence", v).apply()

    fun normalizeVolume(c: Context) = sp(c).getBoolean("normalize", false)
    fun setNormalize(c: Context, v: Boolean) = sp(c).edit().putBoolean("normalize", v).apply()

    fun monoAudio(c: Context) = sp(c).getBoolean("mono", false)
    fun setMono(c: Context, v: Boolean) = sp(c).edit().putBoolean("mono", v).apply()

    fun defaultRepeat(c: Context) = sp(c).getInt("repeat", 0) // 0 off 1 one 2 all
    fun setDefaultRepeat(c: Context, v: Int) = sp(c).edit().putInt("repeat", v).apply()

    fun defaultShuffle(c: Context) = sp(c).getBoolean("shuffle", false)
    fun setDefaultShuffle(c: Context, v: Boolean) = sp(c).edit().putBoolean("shuffle", v).apply()

    // ── UI / Liquid Glass ──
    fun accentIndex(c: Context) = sp(c).getInt("accent", 0)
    fun setAccentIndex(c: Context, v: Int) = sp(c).edit().putInt("accent", v).apply()

    fun darkPlayer(c: Context) = sp(c).getBoolean("dark_player", true)
    fun setDarkPlayer(c: Context, v: Boolean) = sp(c).edit().putBoolean("dark_player", v).apply()

    fun glassIntensity(c: Context) = sp(c).getFloat("glass", 0.85f)
    fun setGlassIntensity(c: Context, v: Float) = sp(c).edit().putFloat("glass", v).apply()

    fun blurAmount(c: Context) = sp(c).getFloat("blur", 48f)
    fun setBlurAmount(c: Context, v: Float) = sp(c).edit().putFloat("blur", v).apply()

    fun reduceMotion(c: Context) = sp(c).getBoolean("reduce_motion", false)
    fun setReduceMotion(c: Context, v: Boolean) = sp(c).edit().putBoolean("reduce_motion", v).apply()

    fun gyro(c: Context) = sp(c).getBoolean("gyro", false)
    fun setGyro(c: Context, v: Boolean) = sp(c).edit().putBoolean("gyro", v).apply()

    fun showEq(c: Context) = sp(c).getBoolean("show_eq", true)
    fun setShowEq(c: Context, v: Boolean) = sp(c).edit().putBoolean("show_eq", v).apply()

    fun compactLists(c: Context) = sp(c).getBoolean("compact", false)
    fun setCompact(c: Context, v: Boolean) = sp(c).edit().putBoolean("compact", v).apply()

    fun showMiniProgress(c: Context) = sp(c).getBoolean("mini_progress", true)
    fun setShowMiniProgress(c: Context, v: Boolean) = sp(c).edit().putBoolean("mini_progress", v).apply()

    fun gridHome(c: Context) = sp(c).getBoolean("grid_home", false)
    fun setGridHome(c: Context, v: Boolean) = sp(c).edit().putBoolean("grid_home", v).apply()

    fun haptics(c: Context) = sp(c).getBoolean("haptics", true)
    fun setHaptics(c: Context, v: Boolean) = sp(c).edit().putBoolean("haptics", v).apply()

    fun largeTitles(c: Context) = sp(c).getBoolean("large_titles", true)
    fun setLargeTitles(c: Context, v: Boolean) = sp(c).edit().putBoolean("large_titles", v).apply()

    fun animatedCovers(c: Context) = sp(c).getBoolean("anim_covers", true)
    fun setAnimatedCovers(c: Context, v: Boolean) = sp(c).edit().putBoolean("anim_covers", v).apply()

    fun lyricsKaraokeScale(c: Context) = sp(c).getFloat("lyrics_scale", 1.08f)
    fun setLyricsKaraokeScale(c: Context, v: Float) = sp(c).edit().putFloat("lyrics_scale", v).apply()

    fun autoLoadLyrics(c: Context) = sp(c).getBoolean("auto_lyrics", true)
    fun setAutoLoadLyrics(c: Context, v: Boolean) = sp(c).edit().putBoolean("auto_lyrics", v).apply()

    fun keepScreenOn(c: Context) = sp(c).getBoolean("screen_on", false)
    fun setKeepScreenOn(c: Context, v: Boolean) = sp(c).edit().putBoolean("screen_on", v).apply()

    fun pageSize(c: Context) = sp(c).getInt("page_size", 20)
    fun setPageSize(c: Context, v: Int) = sp(c).edit().putInt("page_size", v).apply()

    fun catalogIntervalMin(c: Context) = sp(c).getInt("catalog_min", 3)
    fun setCatalogIntervalMin(c: Context, v: Int) = sp(c).edit().putInt("catalog_min", v).apply()

    fun sortMode(c: Context) = sp(c).getInt("sort", 0) // 0 recent 1 title 2 artist
    fun setSortMode(c: Context, v: Int) = sp(c).edit().putInt("sort", v).apply()

    /** 0 = light, 1 = dark */
    fun themeMode(c: Context) = sp(c).getInt("theme", 1)
    fun setThemeMode(c: Context, v: Int) = sp(c).edit().putInt("theme", v).apply()

    fun isDark(c: Context) = themeMode(c) == 1
    fun setDark(c: Context, v: Boolean) = setThemeMode(c, if (v) 1 else 0)

    // ── Privacy (10) ──
    fun analyticsOff(c: Context) = sp(c).getBoolean("priv_analytics", true)
    fun setAnalyticsOff(c: Context, v: Boolean) = sp(c).edit().putBoolean("priv_analytics", v).apply()

    fun hideHistory(c: Context) = sp(c).getBoolean("priv_history", false)
    fun setHideHistory(c: Context, v: Boolean) = sp(c).edit().putBoolean("priv_history", v).apply()

    fun privateSession(c: Context) = sp(c).getBoolean("priv_session", false)
    fun setPrivateSession(c: Context, v: Boolean) = sp(c).edit().putBoolean("priv_session", v).apply()

    fun blockScreenshots(c: Context) = sp(c).getBoolean("priv_shots", false)
    fun setBlockScreenshots(c: Context, v: Boolean) = sp(c).edit().putBoolean("priv_shots", v).apply()

    fun clearOnExit(c: Context) = sp(c).getBoolean("priv_clear_exit", false)
    fun setClearOnExit(c: Context, v: Boolean) = sp(c).edit().putBoolean("priv_clear_exit", v).apply()

    fun noCloudBackup(c: Context) = sp(c).getBoolean("priv_no_backup", false)
    fun setNoCloudBackup(c: Context, v: Boolean) = sp(c).edit().putBoolean("priv_no_backup", v).apply()

    fun hideNotificationArt(c: Context) = sp(c).getBoolean("priv_hide_art", false)
    fun setHideNotificationArt(c: Context, v: Boolean) = sp(c).edit().putBoolean("priv_hide_art", v).apply()

    fun requireBiometric(c: Context) = sp(c).getBoolean("priv_bio", false)
    fun setRequireBiometric(c: Context, v: Boolean) = sp(c).edit().putBoolean("priv_bio", v).apply()

    fun limitNetworkMetered(c: Context) = sp(c).getBoolean("priv_metered", false)
    fun setLimitNetworkMetered(c: Context, v: Boolean) = sp(c).edit().putBoolean("priv_metered", v).apply()

    fun redactSearch(c: Context) = sp(c).getBoolean("priv_redact_search", false)
    fun setRedactSearch(c: Context, v: Boolean) = sp(c).edit().putBoolean("priv_redact_search", v).apply()

    // ── Changelog once per version ──
    fun lastChangelogVersion(c: Context) = sp(c).getString("changelog_ver", "") ?: ""
    fun setLastChangelogVersion(c: Context, v: String) =
        sp(c).edit().putString("changelog_ver", v).apply()

    fun clearPrivacyData(c: Context) {
        sp(c).edit()
            .remove("priv_session")
            .apply()
    }
}
