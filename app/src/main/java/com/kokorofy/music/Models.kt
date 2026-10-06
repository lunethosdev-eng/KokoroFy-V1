package com.kokorofy.music

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val duration: Long = 0,
    val audioUrl: String,
    val coverUrl: String? = null,
    val lyrics: String? = null,
    val lyricsUrl: String? = null,
    val createdAt: String? = null
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey val id: String,
    val name: String,
    val coverUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSong(
    val playlistId: String,
    val songId: String,
    val position: Int = 0
)

data class LyricsResult(val synced: String?, val plain: String?)

/** Línea de letra sincronizada (LRC). */
data class LyricLine(val timeMs: Long, val text: String)
