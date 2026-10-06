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

data class LyricsResult(val synced: String?, val plain: String?)
