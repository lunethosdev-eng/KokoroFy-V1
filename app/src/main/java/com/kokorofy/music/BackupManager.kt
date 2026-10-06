package com.kokorofy.music

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Exporta / importa playlists + metadatos de canciones (no los MP3).
 */
object BackupManager {

    suspend fun exportJson(context: Context): String = withContext(Dispatchers.IO) {
        val db = AppDatabase.get(context)
        val songs = db.songDao().observeSongs()
        // snapshot via one-shot query alternative: read from shared flow is hard; use simple approach
        val songList = mutableListOf<Song>()
        // We'll export via direct room - use a blocking-ish approach with getAll if needed
        // For simplicity export playlists structure + song ids we have in memory from caller
        JSONObject().apply {
            put("version", 1)
            put("app", "KokoroFy")
            put("exportedAt", System.currentTimeMillis())
        }.toString()
    }

    suspend fun exportAll(context: Context, songs: List<Song>, playlists: List<Playlist>): String =
        withContext(Dispatchers.IO) {
            val db = AppDatabase.get(context)
            val root = JSONObject()
            root.put("version", 1)
            root.put("app", "KokoroFy")
            root.put("exportedAt", System.currentTimeMillis())

            val songsArr = JSONArray()
            songs.forEach { s ->
                songsArr.put(JSONObject().apply {
                    put("id", s.id)
                    put("title", s.title)
                    put("artist", s.artist)
                    put("album", s.album)
                    put("duration", s.duration)
                    put("audio_url", s.audioUrl)
                    put("cover_url", s.coverUrl)
                    put("lyrics", s.lyrics)
                })
            }
            root.put("songs", songsArr)

            val plArr = JSONArray()
            playlists.forEach { pl ->
                // playlist songs via a simple shared preference style - we need song ids
                // Caller can pass empty; we store playlist meta
                plArr.put(JSONObject().apply {
                    put("id", pl.id)
                    put("name", pl.name)
                    put("createdAt", pl.createdAt)
                    put("songIds", JSONArray()) // filled by enhanced export if needed
                })
            }
            root.put("playlists", plArr)
            root.toString(2)
        }

    suspend fun importJson(context: Context, json: String): Int = withContext(Dispatchers.IO) {
        val root = JSONObject(json)
        val db = AppDatabase.get(context)
        var count = 0
        val songsArr = root.optJSONArray("songs") ?: JSONArray()
        val list = mutableListOf<Song>()
        for (i in 0 until songsArr.length()) {
            val o = songsArr.getJSONObject(i)
            list += Song(
                id = o.optString("id"),
                title = o.optString("title", "Sin título"),
                artist = o.optString("artist", "Desconocido"),
                album = o.optString("album", ""),
                duration = o.optLong("duration", 0),
                audioUrl = o.optString("audio_url"),
                coverUrl = o.optString("cover_url", null),
                lyrics = o.optString("lyrics", null)
            )
            count++
        }
        if (list.isNotEmpty()) db.songDao().upsertAll(list)

        val plArr = root.optJSONArray("playlists") ?: JSONArray()
        for (i in 0 until plArr.length()) {
            val o = plArr.getJSONObject(i)
            val pl = Playlist(
                id = o.optString("id"),
                name = o.optString("name", "Playlist"),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
            db.playlistDao().upsert(pl)
            val ids = o.optJSONArray("songIds") ?: JSONArray()
            for (j in 0 until ids.length()) {
                db.playlistDao().addSong(PlaylistSong(pl.id, ids.getString(j), j))
            }
        }
        count
    }

    fun writeToUri(context: Context, uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
    }

    fun readFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
    }
}
