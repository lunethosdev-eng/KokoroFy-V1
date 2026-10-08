package com.kokorofy.music

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/** Result returned by Seki /api/search. */
data class SekiSearchResponse(
    val source: String,
    val count: Int,
    val results: List<Song>
)

/**
 * Client for the Express Seki API deployed on Render.
 *
 * IMPORTANT: Seki itself performs the YouTube download and, when needed,
 * stores the resulting audio in Supabase before returning 200.
 * The APK therefore MUST NOT re-upload Seki's audio_url to Supabase.
 */
object SekiClient {
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .callTimeout(190, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val api: SekiApi by lazy {
        Retrofit.Builder()
            .baseUrl(Config.SEKI_API_URL.trimEnd('/') + "/")
            .client(http)
            .build()
            .create(SekiApi::class.java)
    }

    private fun normalizedQuery(query: String): String {
        val raw = query.trim().replace(Regex("\\s+"), " ")
        extractYoutubeId(raw)?.let { id ->
            return "https://www.youtube.com/watch?v=$id"
        }
        return raw
    }

    fun extractYoutubeId(input: String): String? {
        val str = input.trim()
        val patterns = listOf(
            Regex(
                "(?:youtube\\.com/(?:watch\\?.*?v=|embed/|v/|shorts/|live/)|youtu\\.be/|music\\.youtube\\.com/watch\\?.*?v=)([\\w-]{11})",
                RegexOption.IGNORE_CASE
            ),
            Regex("[?&]v=([\\w-]{11})", RegexOption.IGNORE_CASE),
            Regex("^([\\w-]{11})$")
        )
        for (re in patterns) {
            val m = re.find(str) ?: continue
            val id = m.groupValues.getOrNull(1) ?: continue
            if (id.length == 11) return id
        }
        return null
    }

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

    suspend fun search(query: String): Result<SekiSearchResponse> = withContext(Dispatchers.IO) {
        val q = normalizedQuery(query)
        if (q.isBlank()) return@withContext Result.success(SekiSearchResponse("database", 0, emptyList()))
        if (Config.SEKI_API_URL.contains("xxxxx")) {
            return@withContext Result.failure(IllegalStateException("Seki URL no configurada"))
        }

        runCatching {
            // Always send BOTH header and query key (some deployments only check one).
            val key = Config.SEKI_API_KEY.trim().takeIf { it.isNotBlank() }
            if (key == null) {
                error("SEKI_API_KEY vacía en Config.kt")
            }

            var response = api.search(
                query = q,
                apiKey = key,
                queryKey = key
            )

            // Retry once with only query key if header path is rejected.
            if (!response.isSuccessful && (response.code() == 401 || response.code() == 403)) {
                Log.w("Seki", "auth rejected with header, retrying ?key=")
                response = api.search(
                    query = q,
                    apiKey = null,
                    queryKey = key
                )
            }

            if (!response.isSuccessful) {
                val body = response.errorBody()?.string().orEmpty()
                val code = response.code()
                val hint = when (code) {
                    401, 403 -> " (API key rechazada: revisa SEKI_API_KEY en Config.kt y API_SECRET en Render)"
                    500 -> " (el servidor falló al procesar; la key SÍ se envió. Revisa logs de Render / yt-dlp)"
                    502, 503, 504 -> " (Render frío o caído; reintenta en unos segundos)"
                    else -> ""
                }
                error("Seki HTTP $code$hint${if (body.isNotBlank()) ": $body" else ""}")
            }

            val body = response.body()?.string().orEmpty()
            parseResponse(body)
        }.onFailure {
            Log.e("Seki", "search failed for ${q.take(120)}", it)
        }
    }

    private fun parseResponse(body: String): SekiSearchResponse {
        if (body.isBlank()) return SekiSearchResponse("database", 0, emptyList())

        val root = runCatching { JSONObject(body) }.getOrNull()
            ?: error("Seki devolvió JSON inválido")

        val source = root.optString("source", "database")
        val declaredCount = root.optInt("count", -1)
        val array = when {
            root.optJSONArray("results") != null -> root.optJSONArray("results")
            root.optJSONArray("songs") != null -> root.optJSONArray("songs")
            root.optJSONArray("items") != null -> root.optJSONArray("items")
            root.optJSONArray("data") != null -> root.optJSONArray("data")
            else -> null
        }

        val out = ArrayList<Song>(array?.length() ?: 0)
        if (array != null) {
            for (i in 0 until array.length()) {
                val value = array.opt(i)
                if (value !is JSONObject || !looksLikeTrack(value)) continue
                val song = parseSong(value)
                if (song.audioUrl.isNotBlank()) out += song
            }
        } else {
            // Be tolerant of older Seki deployments returning one object directly.
            val nested = listOf("result", "track", "song", "data")
                .asSequence()
                .mapNotNull { root.optJSONObject(it) }
                .firstOrNull { looksLikeTrack(it) }
            val one = nested ?: root.takeIf { looksLikeTrack(it) }
            if (one != null) {
                val song = parseSong(one)
                if (song.audioUrl.isNotBlank()) out += song
            }
        }

        return SekiSearchResponse(
            source = source,
            count = if (declaredCount >= 0) declaredCount else out.size,
            results = out
        )
    }

    private fun looksLikeTrack(o: JSONObject): Boolean =
        o.has("audio_url") || o.has("audioUrl") || o.has("stream_url") ||
            o.has("streamUrl") || o.has("download_url") || o.has("youtube_id") ||
            o.has("youtubeId") || o.has("videoId")

    private fun parseSong(o: JSONObject): Song {
        val audio = firstNonBlank(
            o.optString("audio_url"),
            o.optString("audioUrl"),
            o.optString("stream_url"),
            o.optString("streamUrl"),
            o.optString("download_url"),
            o.optString("audio"),
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
            o.optLong("duration_seconds") > 0L -> o.optLong("duration_seconds")
            o.optLong("durationSeconds") > 0L -> o.optLong("durationSeconds")
            o.optLong("duration") > 0L -> o.optLong("duration")
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
