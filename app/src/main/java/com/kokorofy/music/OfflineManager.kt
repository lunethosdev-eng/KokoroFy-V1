package com.kokorofy.music

import android.content.Context
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService

object OfflineManager {
    fun download(context: Context, song: Song) {
        val request = DownloadRequest.Builder(song.id, android.net.Uri.parse(song.audioUrl))
            .setCustomCacheKey(song.id)
            .build()
        DownloadService.sendAddDownload(
            context,
            KokoroDownloadService::class.java,
            request,
            false
        )
    }

    fun remove(context: Context, song: Song) {
        DownloadService.sendRemoveDownload(
            context,
            KokoroDownloadService::class.java,
            song.id,
            false
        )
    }
}
