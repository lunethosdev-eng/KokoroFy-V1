package com.kokorofy.music

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Cliente Seki API.
 *
 * - GET /api/search?q=texto
 * - acepta nombres, artistas y enlaces directos de YouTube
 * - autentica con x-api-key y hace fallback a ?key= si el deployment antiguo
 *   no acepta el header.
 *
 * Todo el trabajo de red corre en Dispatchers.IO.
 */
object SekiClient {
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val api: SekiApi by lazy {
        Retrofit.Builder()
            .baseUrl(Config.SEKI_API_URL.trimEnd('/') + "/")
            .client(http)
            .build()
            .create(SekiApi::class.java)
    }

    private fun normalizedQuery(query: String): String =
        query.trim().replace(Regex("\\s+"), " ")

    suspend fun health(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val request = okhttp3.Request.Builder()
                .url(Config.SEKI_API_URL.trimEnd('/') + "/health")
                .header("x-api-key", Config.SEKI_API_KEY)
                .get()
                .build()
            http.newCall(request).execute().use { it.isSuccessful }
        }.getOrDefault(false)
    }

    suspend fun search(query: String): List<Song> = withContext(Dispatchers.IO) {
        val q = normalizedQuery(query)
        if (q.isBlank()) return@withContext emptyList()
        if (Config.SEKI_API_URL.contains("xxxxx")) return@withContext emptyList()

        runCatching {
            var response = api.search(
                query = q,
                apiKey = Config.SEKI_API_KEY.takeIf { it.isNotBlank() }
            )

            // Backward compatibility with Render deployments that only read ?key=.
            if (!response.isSuccessful && (response.code() == 401 || response.code() == 403)) {
                response = api.search(
                    query = q,
                    queryKey = Config.SEKI_API_KEY.takeIf { it.isNotBlank() }
                )
            }

            if (!response.isSuccessful) {
                Log.e("Seki", "search HTTP ${response.code()}")
                return@runCatching emptyList()
            }

            val body = response.body()?.string().orEmpty()
            parseResults(body)
        }.onFailure {
            Log.e("Seki", "search failed for ${q.take(80)}", it)
        }.getOrDefault(emptyList())
    }

    private fun parseResults(body: String): List<Song> {
        if (body.isBlank()) return emptyList()

        val root = runCatching { JSONObject(body) }.getOrNull()
        val array = when {
            root?.optJSONArray("results") != null -> root.optJSONArray("results")
            root?.optJSONArray("songs") != null -> root.optJSONArray("songs")
            root?.optJSONArray("items") != null -> root.optJSONArray("items")
            root?.optJSONArray("data") != null -> root.optJSONArray("data")
            else -> runCatching { JSONArray(body) }.getOrNull()
        }

        // Some Seki versions return a single result object for a direct URL.
        if (array == null && root != null && looksLikeTrack(root)) {
            return listOf(parseSong(root)).filter { it.audioUrl.isNotBlank() }
        }

        if (array == null) return emptyList()

        val out = ArrayList<Song>(array.length())
        for (i in 0 until array.length()) {
            val value = array.opt(i)
            if (value !is JSONObject || !looksLikeTrack(value)) continue
            val song = parseSong(value)
            if (song.audioUrl.isNotBlank()) out += song
        }
        return out
    }

    private fun looksLikeTrack(o: JSONObject): Boolean =
        o.has("audio_url") || o.has("audioUrl") || o.has("stream_url") ||
            o.has("url") || o.has("youtube_id") || o.has("videoId")

    private fun parseSong(o: JSONObject): Song {
        val audio = firstNonBlank(
            o.optString("audio_url"),
            o.optString("audioUrl"),
            o.optString("stream_url"),
            o.optString("streamUrl"),
            o.optString("url")
        )

        val id = firstNonBlank(
            o.optString("id"),
            o.optString("youtube_id"),
            o.optString("youtubeId"),
            o.optString("videoId"),
            audio.hashCode().toString()
        )

        val duration = when {
            o.optLong("duration") > 0L -> o.optLong("duration")
            o.optLong("duration_seconds") > 0L -> o.optLong("duration_seconds")
            o.optLong("durationSeconds") > 0L -> o.optLong("durationSeconds")
            else -> 0L
        }

        return Song(
            id = id,
            title = firstNonBlank(o.optString("title"), o.optString("name"), "Sin título"),
            artist = firstNonBlank(o.optString("artist"), o.optString("author"), "Desconocido"),
            album = firstNonBlank(o.optString("album"), ""),
            duration = duration,
            audioUrl = audio,
            coverUrl = firstNonBlankOrNull(
                o.optString("cover_url"),
                o.optString("coverUrl"),
                o.optString("thumbnail"),
                o.optString("thumbnail_url")
            ),
            lyrics = firstNonBlankOrNull(o.optString("lyrics")),
            lyricsUrl = firstNonBlankOrNull(o.optString("lyrics_url"), o.optString("lyricsUrl")),
            createdAt = firstNonBlankOrNull(o.optString("created_at"), o.optString("createdAt"))
        )
    }

    private fun firstNonBlank(vararg values: String): String =
        values.firstOrNull { it.isNotBlank() } ?: ""

    private fun firstNonBlankOrNull(vararg values: String): String? =
        firstNonBlank(*values).ifBlank { null }
}
