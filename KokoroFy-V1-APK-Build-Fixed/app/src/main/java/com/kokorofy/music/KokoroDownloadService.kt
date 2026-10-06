package com.kokorofy.music

import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import java.util.concurrent.Executors

class KokoroDownloadService : DownloadService(
    DEFAULT_FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    "kokorofy_downloads",
    0,
    0
) {
    private lateinit var downloadManager: DownloadManager

    override fun getDownloadManager(): DownloadManager {
        if (!::downloadManager.isInitialized) {
            val cache = MusicCache.get(this)
            val upstream = DefaultHttpDataSource.Factory()
            val dataSource = CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(upstream)
            downloadManager = DownloadManager(
                this,
                StandaloneDatabaseProvider(this),
                cache,
                dataSource,
                Executors.newFixedThreadPool(3)
            )
        }
        return downloadManager
    }

    override fun getScheduler() = null
}
