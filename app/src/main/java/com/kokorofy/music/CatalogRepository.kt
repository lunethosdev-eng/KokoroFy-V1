package com.kokorofy.music

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

class CatalogRepository(context: Context) {
    private val dao = AppDatabase.get(context).songDao()
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Flow reactivo de todas las canciones en Room. */
    val songs: Flow<List<Song>> = dao.observeSongs()

    /**
     * Upsert manual (usado por búsqueda Seki on-demand).
     */
    suspend fun upsertAll(list: List<Song>) = withContext(Dispatchers.IO) {
        if (list.isNotEmpty()) dao.upsertAll(list)
    }

    /**
     * Siempre vuelve a pedir el catálogo a Supabase.
     * Cache-Control: no-cache para no quedarse con datos viejos.
     * upsertAll → las canciones nuevas aparecen sin borrar datos de la app.
     */
    suspend fun refresh(): Int = withContext(Dispatchers.IO) {
        val key = Config.SUPABASE_PUBLISHABLE_KEY
        if (key.contains("PEGA_AQUI") || key.isBlank()) {
            Log.w("Catalog", "Supabase key vacía")
            return@withContext 0
        }
        val url =
            "${Config.SUPABASE_URL}/rest/v1/${Config.SONGS_TABLE}?select=*&order=created_at.desc"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", key)
            .addHeader("Authorization", "Bearer $key")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Pragma", "no-cache")
            .addHeader("Prefer", "count=exact")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("Catalog", "Supabase HTTP ${response.code}: ${response.body?.string()}")
                error("Supabase ${response.code}")
            }
            val body = response.body?.string() ?: "[]"
            val arr = JSONArray(body)
            val list = buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val id = o.optString("id").ifBlank { o.optString("uuid") }
                    if (id.isBlank()) continue
                    val audio = o.optString("audio_url")
                        .ifBlank { o.optString("stream_url") }
                        .ifBlank { o.optString("url") }
                    if (audio.isBlank()) continue
                    add(
                        Song(
                            id = id,
                            title = o.optString("title", "Sin título"),
                            artist = o.optString("artist", "Artista desconocido"),
                            album = o.optString("album", ""),
                            duration = when {
                                o.has("duration") -> o.optLong("duration", 0)
                                else -> 0L
                            },
                            audioUrl = audio,
                            coverUrl = o.optString("cover_url").ifBlank { null },
                            lyrics = o.optString("lyrics").ifBlank { null },
                            lyricsUrl = o.optString("lyrics_url").ifBlank { null },
                            createdAt = o.optString("created_at").ifBlank { null }
                        )
                    )
                }
            }
            if (list.isNotEmpty()) {
                dao.upsertAll(list)
            }
            Log.i("Catalog", "Refresh OK: ${list.size} canciones")
            list.size
        }
    }
}
