package com.kokorofy.music

import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocalMusicScanner {

    /**
     * Lee la biblioteca de audio del dispositivo (MediaStore).
     * Requiere READ_MEDIA_AUDIO (API 33+) o READ_EXTERNAL_STORAGE.
     */
    suspend fun scanDevice(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val out = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        context.contentResolver.query(
            collection, projection, selection, null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val path = cursor.getString(dataCol) ?: continue
                if (!path.endsWith(".mp3", true) &&
                    !path.endsWith(".m4a", true) &&
                    !path.endsWith(".flac", true) &&
                    !path.endsWith(".wav", true) &&
                    !path.endsWith(".ogg", true)
                ) continue
                out += Song(
                    id = "local_$id",
                    title = cursor.getString(titleCol) ?: path.substringAfterLast('/'),
                    artist = cursor.getString(artistCol) ?: "Desconocido",
                    album = cursor.getString(albumCol) ?: "",
                    duration = cursor.getLong(durCol),
                    audioUrl = path,
                    coverUrl = null
                )
            }
        }
        out
    }
}
