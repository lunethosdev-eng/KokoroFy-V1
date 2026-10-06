package com.kokorofy.music

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocalMusicScanner {

    /**
     * Usa content:// URIs de MediaStore (reproducibles por ExoPlayer).
     * Antes se usaba la ruta file path → fallaba en Android 10+.
     */
    suspend fun scanDevice(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val out = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
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
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(collection, id)
                out += Song(
                    id = "local_$id",
                    title = cursor.getString(titleCol) ?: "Pista $id",
                    artist = cursor.getString(artistCol) ?: "Desconocido",
                    album = cursor.getString(albumCol) ?: "",
                    duration = cursor.getLong(durCol),
                    audioUrl = contentUri.toString(), // content://media/...
                    coverUrl = null
                )
            }
        }
        out
    }
}
