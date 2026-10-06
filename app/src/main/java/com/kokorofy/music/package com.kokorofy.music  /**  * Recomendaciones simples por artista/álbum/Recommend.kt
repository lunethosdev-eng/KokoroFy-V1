package com.kokorofy.music

/**
 * Recomendaciones simples por artista/álbum/género a partir del historial actual.
 */
object Recommend {
    fun similar(current: Song?, catalog: List<Song>, limit: Int = 12): List<Song> {
        if (current == null) return catalog.shuffled().take(limit)
        val artist = current.artist.lowercase()
        val album = current.album.lowercase()
        val scored = catalog
            .filter { it.id != current.id }
            .map { s ->
                var score = 0
                if (s.artist.equals(current.artist, true)) score += 5
                else if (s.artist.lowercase().contains(artist.take(4)) && artist.length > 3) score += 2
                if (album.isNotBlank() && s.album.equals(current.album, true)) score += 3
                score to s
            }
            .filter { it.first > 0 }
            .sortedByDescending { it.first }
            .map { it.second }
        val rest = catalog.filter { it.id != current.id && it !in scored }.shuffled()
        return (scored + rest).distinctBy { it.id }.take(limit)
    }
}
