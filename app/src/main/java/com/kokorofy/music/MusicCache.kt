package com.kokorofy.music

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

object MusicCache {
    @Volatile private var cache: SimpleCache? = null
    fun get(context: Context): SimpleCache = synchronized(this) {
        cache ?: SimpleCache(
            File(context.filesDir, "music_cache"),
            LeastRecentlyUsedCacheEvictor(2L * 1024L * 1024L * 1024L),
            StandaloneDatabaseProvider(context)
        ).also { cache = it }
    }
}
