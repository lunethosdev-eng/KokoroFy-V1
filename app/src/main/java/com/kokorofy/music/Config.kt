package com.kokorofy.music

object Config {
    const val SUPABASE_URL = "https://esjoifsjljvymttinyhj.supabase.co"
    const val SUPABASE_PUBLISHABLE_KEY = "sb_publishable_N5me2DAs7TrngbvqkkbqsA_UGGakGAL"
    const val SONGS_TABLE = "songs"

    // Fallback de letras. Se consulta solo cuando Supabase no trae lyrics/lyrics_url.
    const val LRCLIB_BASE = "https://lrclib.net/api/get"
}
