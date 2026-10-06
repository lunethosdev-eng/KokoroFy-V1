package com.kokorofy.music

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class LyricsRepository {
    private val client=OkHttpClient()

    suspend fun fetch(song: Song): LyricsResult? = withContext(Dispatchers.IO) {
        song.lyrics?.takeIf{it.isNotBlank()}?.let{return@withContext LyricsResult(it,null)}
        song.lyricsUrl?.takeIf{it.isNotBlank()}?.let{
            runCatching {
                val req=Request.Builder().url(it).build()
                client.newCall(req).execute().use { r ->
                    if(r.isSuccessful) LyricsResult(r.body?.string(),null) else null
                }
            }.getOrNull()?.let{return@withContext it}
        }

        runCatching {
            val q="https://lrclib.net/api/get?track_name=${URLEncoder.encode(song.title,"UTF-8")}&artist_name=${URLEncoder.encode(song.artist,"UTF-8")}"
            val req=Request.Builder().url(q).header("User-Agent","KokoroFy/1.0").build()
            client.newCall(req).execute().use { r ->
                if(!r.isSuccessful) return@use null
                val o=JSONObject(r.body?.string()?:"")
                LyricsResult(
                    synced=o.optString("syncedLyrics",null),
                    plain=o.optString("plainLyrics",null)
                )
            }
        }.getOrNull()
    }
}
