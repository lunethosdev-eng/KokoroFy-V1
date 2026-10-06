package com.kokorofy.music

/**
 * Se muestra UNA sola vez por versionName al actualizar.
 */
object Changelog {
    const val CURRENT = "1.3.0"

    val entries = listOf(
        "Liquid Glass rediseñado estilo Apple (blur + specular + capas)",
        "Player full con más animaciones y menú ⋮ playlists",
        "Lista de canciones con carga progresiva (más fluida)",
        "Lyrics karaoke con transiciones suaves y auto-cambio de pista",
        "Widget sincronizado con la reproducción en vivo",
        "30+ opciones de personalización reales en Ajustes",
        "10 controles de privacidad (sesión privada, borrar caché, etc.)",
        "Catálogo auto-refresh sin borrar datos",
        "Firma APK estable para actualizar sin desinstalar",
        "Velocidad de audio, grid/lista, haptics, compact mode",
        "Escaneo local con content:// reproducible",
        "Export/import backup JSON",
        "Mini player con progreso real y resume sin reinicio"
    )

    fun shouldShow(context: android.content.Context): Boolean {
        val last = Prefs.lastChangelogVersion(context)
        return last != CURRENT
    }

    fun markShown(context: android.content.Context) {
        Prefs.setLastChangelogVersion(context, CURRENT)
    }
}
