package com.kokorofy.music

object Config {
    const val SUPABASE_URL = "https://esjoifsjljvymttinyhj.supabase.co"
    const val SUPABASE_PUBLISHABLE_KEY = "PEGA_AQUI_TU_PUBLISHABLE_KEY"
    const val SONGS_TABLE = "songs"

    // Fallback de letras. Se consulta solo cuando Supabase no trae lyrics/lyrics_url.
    const val LRCLIB_BASE = "https://lrclib.net/api/get"
}
