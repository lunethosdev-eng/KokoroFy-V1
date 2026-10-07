package com.kokorofy.music

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Cliente del scraper Seki (Render).
 * - /api/songs → catálogo (mismo que Supabase)
 * - /api/search?q= → busca local o descarga on-demand y sube a Supabase
 */
object SekiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS) // yt-dlp puede tardar
        .build()

    private fun base(): String = Config.SEKI_API_URL.trimEnd('/')

    private fun Request.Builder.sekiHeaders(): Request.Builder {
        val key = Config.SEKI_API_KEY
        if (key.isNotBlank()) header("x-api-key", key)
        return this
    }

    suspend fun health(): Boolean = withContext(Dispatchers.IO) {
        if (base().contains("xxxxx")) return@withContext false
        runCatching {
            val req = Request.Builder().url("${base()}/health").get().build()
            client.newCall(req).execute().use { it.isSuccessful }
        }.getOrDefault(false)
    }

    /**
     * Busca en Seki. Si no existe en DB, Seki descarga y guarda en Supabase.
     * Devuelve lista de Song listas para upsert en Room.
     */
    suspend fun search(query: String): List<Song> = withContext(Dispatchers.IO) {
        if (base().contains("xxxxx") || query.isBlank()) return@withContext emptyList()
        val url = "${base()}/api/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
        val req = Request.Builder().url(url).get().sekiHeaders().build()
        runCatching {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.e("Seki", "search HTTP ${resp.code}")
                    return@use emptyList()
                }
                val body = resp.body?.string() ?: return@use emptyList()
                parseResults(body)
            }
        }.getOrElse {
            Log.e("Seki", "search fail", it)
            emptyList()
        }
    }

    private fun parseResults(body: String): List<Song> {
        val root = JSONObject(body)
        val arr = root.optJSONArray("results") ?: return emptyList()
        val out = ArrayList<Song>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.optString("id").ifBlank {
                o.optString("youtube_id").ifBlank { o.optString("audio_url").hashCode().toString() }
            }
            val audio = o.optString("audio_url").ifBlank { o.optString("audioUrl") }
            if (audio.isBlank()) continue
            out += Song(
                id = id,
                title = o.optString("title").ifBlank { "Sin título" },
                artist = o.optString("artist").ifBlank { "Desconocido" },
                album = o.optString("album", ""),
                duration = o.optLong("duration").takeIf { it > 0 }
                    ?: o.optLong("duration_seconds").takeIf { it > 0 }
                    ?: 0L,
                audioUrl = audio,
                coverUrl = o.optString("cover_url").ifBlank { o.optString("coverUrl") }.ifBlank { null },
                lyrics = o.optString("lyrics").ifBlank { null },
                lyricsUrl = o.optString("lyrics_url").ifBlank { null },
                createdAt = o.optString("created_at").ifBlank { null }
            )
        }
        return out
    }
}
