package com.kokorofy.music

import android.content.Context
import org.json.JSONObject

object TasteManager {
    private const val NAME = "kokorofy_taste_v1"
    private fun sp(c: Context) = c.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun recordPlay(c: Context, songId: String) {
        val counts = JSONObject(sp(c).getString("plays", "{}") ?: "{}")
        counts.put(songId, counts.optInt(songId, 0) + 1)
        sp(c).edit().putString("plays", counts.toString()).apply()
    }

    fun playCount(c: Context, songId: String): Int =
        runCatching { JSONObject(sp(c).getString("plays", "{}") ?: "{}").optInt(songId, 0) }.getOrDefault(0)

    fun isFavorite(c: Context, songId: String): Boolean =
        sp(c).getStringSet("favorites", emptySet())?.contains(songId) == true

    fun toggleFavorite(c: Context, songId: String): Boolean {
        val current = sp(c).getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()
        val added = current.add(songId)
        if (!added) current.remove(songId)
        sp(c).edit().putStringSet("favorites", current).apply()
        return added
    }

    fun favoriteIds(c: Context): Set<String> =
        sp(c).getStringSet("favorites", emptySet())?.toSet() ?: emptySet()

    fun clear(c: Context) = sp(c).edit().clear().apply()
}
