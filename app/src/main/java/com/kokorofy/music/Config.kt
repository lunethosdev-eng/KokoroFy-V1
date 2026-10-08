package com.kokorofy.music

object Config {
    const val SUPABASE_URL = "https://esjoifsjljvymttinyhj.supabase.co"
    const val SUPABASE_PUBLISHABLE_KEY = "sb_publishable_N5me2DAs7TrngbvqkkbqsA_UGGakGAL"
    const val SONGS_TABLE = "songs"
    // Seki owns server-side persistence. The APK only reads the returned public URL.
    const val SUPABASE_BUCKET = "audio"

    const val SEKI_API_URL = "https://sekii-1.onrender.com"

    /** Must match API_SECRET on https://sekii-1.onrender.com. */
    const val SEKI_API_KEY = "kokoro-seki-2026"

    const val LRCLIB_BASE = "https://lrclib.net/api/get"
}
