package com.kokorofy.music

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class LyricsRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .callTimeout(50, TimeUnit.SECONDS)
        .build()

    suspend fun fetch(song: Song): LyricsResult? = withContext(Dispatchers.IO) {
        // 1) Lyrics already supplied by Seki/Supabase.
        song.lyrics?.trim()?.takeIf { it.isNotEmpty() }?.let { raw ->
            return@withContext parsePossibleLyricsPayload(raw)
        }

        // 2) Explicit lyrics URL from the catalog.
        song.lyricsUrl?.trim()?.takeIf { it.isNotEmpty() }?.let { url ->
            runCatching {
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", "KokoroFy/1.7")
                    .build()
                client.newCall(req).execute().use { r ->
                    if (!r.isSuccessful) null
                    else parsePossibleLyricsPayload(r.body?.string().orEmpty())
                }
            }.getOrNull()?.let { if (!it.isEmpty()) return@withContext it }
        }

        // 3) LRCLIB. Use the encoded query and accept both synced and plain lyrics.
        val track = URLEncoder.encode(song.title.trim(), "UTF-8")
        val artist = URLEncoder.encode(song.artist.trim(), "UTF-8")
        val url = "${Config.LRCLIB_BASE}?track_name=$track&artist_name=$artist"

        runCatching {
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "KokoroFy/1.8 (Android)")
                .build()
            client.newCall(req).execute().use { r ->
                if (!r.isSuccessful) return@use null
                val body = r.body?.string().orEmpty()
                val o = JSONObject(body)
                val synced = o.optString("syncedLyrics").ifBlank { null }
                val plain = o.optString("plainLyrics").ifBlank { null }
                LyricsResult(synced = synced, plain = plain)
            }
        }.getOrNull()?.takeIf { it.isEmpty().not() }
    }

    private fun parsePossibleLyricsPayload(raw: String): LyricsResult? {
        val text = raw.trim()
        if (text.isEmpty()) return null

        // Some backends return JSON instead of raw LRC/plain text.
        if (text.startsWith("{")) {
            runCatching {
                val o = JSONObject(text)
                LyricsResult(
                    synced = o.optString("syncedLyrics")
                        .ifBlank { o.optString("synced_lyrics") }
                        .ifBlank { o.optString("lrc") }
                        .ifBlank { o.optString("lyrics") }
                        .ifBlank { null },
                    plain = o.optString("plainLyrics")
                        .ifBlank { o.optString("plain_lyrics") }
                        .ifBlank { o.optString("text") }
                        .ifBlank { null }
                )
            }.getOrNull()?.let { result ->
                if (!result.isEmpty()) return result
            }
        }

        return if (Regex("""\[\d{1,3}:\d{2}""").containsMatchIn(text)) {
            LyricsResult(synced = text, plain = null)
        } else {
            LyricsResult(synced = null, plain = text)
        }
    }

    private fun LyricsResult.isEmpty(): Boolean =
        synced.isNullOrBlank() && plain.isNullOrBlank()
}
