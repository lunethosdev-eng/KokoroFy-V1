package com.kokorofy.music

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class CatalogRepository(context: Context) {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()
    private val client=OkHttpClient()

    suspend fun refresh()=withContext(Dispatchers.IO){
        if(Config.SUPABASE_PUBLISHABLE_KEY.contains("PEGA_AQUI")) return@withContext
        val url="${Config.SUPABASE_URL}/rest/v1/${Config.SONGS_TABLE}?select=*&order=created_at.desc"
        val request=Request.Builder().url(url)
            .addHeader("apikey",Config.SUPABASE_PUBLISHABLE_KEY)
            .addHeader("Authorization","Bearer ${Config.SUPABASE_PUBLISHABLE_KEY}").build()
        client.newCall(request).execute().use { response ->
            if(!response.isSuccessful) error("Supabase ${response.code}")
            val arr=JSONArray(response.body?.string()?:"[]")
            val list=buildList {
                for(i in 0 until arr.length()){
                    val o=arr.getJSONObject(i)
                    add(Song(
                        id=o.optString("id"),
                        title=o.optString("title","Sin título"),
                        artist=o.optString("artist","Artista desconocido"),
                        album=o.optString("album",""),
                        duration=o.optLong("duration",0),
                        audioUrl=o.optString("audio_url"),
                        coverUrl=o.optString("cover_url",null),
                        lyrics=o.optString("lyrics",null),
                        lyricsUrl=o.optString("lyrics_url",null),
                        createdAt=o.optString("created_at",null)
                    ))
                }
            }
            _songs.value = list
        }
    }
}
