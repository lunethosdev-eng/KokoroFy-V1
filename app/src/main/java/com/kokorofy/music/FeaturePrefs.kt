package com.kokorofy.music

import android.content.Context

data class FeatureSetting(
    val key: String,
    val title: String,
    val description: String,
    val default: Boolean = false
)

object FeaturePrefs {
    private const val NAME = "kokorofy_features_v1"
    private fun sp(c: Context) = c.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun get(c: Context, key: String, default: Boolean = false) =
        sp(c).getBoolean(key, default)

    fun set(c: Context, key: String, value: Boolean) =
        sp(c).edit().putBoolean(key, value).apply()

    fun clear(c: Context) = sp(c).edit().clear().apply()

    val privacy = listOf(
        Triple("privacy.incognito", "Sesión privada", "No guardar actividad de escucha"),
        Triple("privacy.history", "Historial de escucha", "Mostrar canciones recientes en Inicio"),
        Triple("privacy.analytics", "Analítica anónima", "Métricas técnicas anónimas"),
        Triple("privacy.screenshots", "Bloquear capturas", "FLAG_SECURE en pantallas sensibles"),
        Triple("privacy.notification_art", "Ocultar portada en notificación", "Sin artwork en MediaSession"),
        Triple("privacy.cloud_backup", "Copia en la nube", "Permitir respaldo de preferencias"),
        Triple("privacy.metered", "Limitar datos móviles", "Sin descargas en redes medidas"),
        Triple("privacy.redact_search", "Búsqueda privada", "No conservar texto de búsqueda"),
        Triple("privacy.external_share", "Compartir externo", "Permitir selector de compartir"),
        Triple("privacy.local_only", "Modo solo local", "No consultar Seki ni red"),
        Triple("privacy.hide_profile", "Ocultar perfil", "No mostrar correo en Ajustes"),
        Triple("privacy.auto_clear_cache", "Limpiar caché al salir", "Borra temporales al cerrar"),
        Triple("privacy.auto_clear_search", "Limpiar búsquedas", "Vacía consulta al salir de Buscar"),
        Triple("privacy.secure_links", "Enlaces seguros", "No abrir links externos auto"),
        Triple("privacy.hide_lyrics", "Ocultar letras en historial", "No asociar letras a actividad"),
        Triple("privacy.private_downloads", "Descargas privadas", "Metadatos de descargas ocultos"),
        Triple("privacy.disable_share_metadata", "Ocultar metadatos al compartir", "Solo enlace si es posible"),
        Triple("privacy.no_personalization", "Sin personalización", "Sin recomendaciones por actividad"),
        Triple("privacy.reset_session", "Restablecer sesión al salir", "Limpia token local"),
        Triple("privacy.protect_library", "Proteger biblioteca", "Oculta biblioteca sin auth"),
        Triple("privacy.blur_recents", "Difuminar recientes", "Enmascara títulos en multitarea"),
        Triple("privacy.lock_downloads", "Bloquear descargas", "Requiere confirmación extra"),
        Triple("privacy.hide_listening", "Ocultar ahora suena", "No publicar estado de reproducción"),
        Triple("privacy.strip_id3", "Quitar ID3 al exportar", "Exporta audio sin tags personales"),
        Triple("privacy.anonymous_search", "Búsqueda anónima", "No envía user-agent personalizado"),
    )

    val customization = listOf(
        Triple("ui.glass", "Liquid Glass", "Superficies translúcidas con refracción"),
        Triple("ui.glass_strong", "Cristal intenso", "Más contraste y profundidad"),
        Triple("ui.dynamic_background", "Fondo dinámico", "Portada como fondo ambiental"),
        Triple("ui.specular", "Reflejo especular", "Highlight superior en cristal"),
        Triple("ui.large_titles", "Títulos grandes", "Jerarquía tipográfica grande"),
        Triple("ui.compact", "Listas compactas", "Menos espacio vertical"),
        Triple("ui.grid", "Biblioteca en cuadrícula", "Tarjetas con portadas"),
        Triple("ui.animated_covers", "Portadas animadas", "Microanimaciones en covers"),
        Triple("ui.cover_parallax", "Parallax de portada", "Movimiento sutil con scroll"),
        Triple("ui.gyro_3d", "Portada 3D con giroscopio", "Inclinación con sensor"),
        Triple("ui.nav_glass", "Nav Liquid Glass", "Barra inferior de cristal"),
        Triple("ui.nav_compact", "Nav compacto", "Barra inferior más baja"),
        Triple("ui.mini_progress", "Progreso del mini-player", "Barra de progreso en mini"),
        Triple("ui.blur", "Blur ambiental", "Fondo difuminado tras cristal"),
        Triple("ui.keep_screen", "Pantalla encendida", "No apagar durante reproducción"),
        Triple("ui.mini_blur", "Mini-player translúcido", "Profundidad extra en mini"),
        Triple("ui.player_dim", "Oscurecer fondo del player", "Scrim detrás de portada"),
        Triple("ui.player_radius", "Radio grande de portada", "Bordes suaves tipo Apple Music"),
        Triple("ui.haptics", "Háptica", "Vibración en gestos clave"),
        Triple("ui.reduce_motion", "Reducir movimiento", "Menos animaciones"),
        Triple("ui.accent_from_cover", "Acento desde portada", "Color dominante del artwork"),
        Triple("ui.rounded_lists", "Listas redondeadas", "Filas con esquinas suaves"),
        Triple("ui.section_headers", "Encabezados de sección", "Títulos sticky en listas"),
        Triple("ui.show_duration", "Mostrar duración", "Tiempo en filas de canción"),
        Triple("ui.show_explicit", "Marca explicit", "Badge en tracks explícitos"),
        Triple("ui.waveform_mini", "Waveform en mini", "Forma de onda decorativa"),
        Triple("ui.now_playing_pulse", "Pulso now playing", "Animación en pista activa"),
        Triple("ui.swipe_actions", "Acciones al deslizar", "Deslizar fila para opciones"),
        Triple("ui.page_transitions", "Transiciones de página", "Animar cambio de pestaña"),
        Triple("ui.splash_logo", "Logo en arranque", "Mostrar animación de marca"),
    )

    val playback = listOf(
        Triple("play.gapless", "Gapless", "Sin silencio entre pistas"),
        Triple("play.crossfade", "Crossfade", "Fundido entre canciones"),
        Triple("play.normalize", "Normalizar volumen", "Loudness relativo"),
        Triple("play.mono", "Audio mono", "Mezcla a un canal"),
        Triple("play.skip_silence", "Saltar silencios", "Omite pausas largas"),
        Triple("play.remember_speed", "Recordar velocidad", "Conserva speed entre sesiones"),
        Triple("play.resume_position", "Reanudar posición", "Continúa donde lo dejaste"),
        Triple("play.auto_play_next", "Auto siguiente", "Reproduce la siguiente al terminar"),
        Triple("play.shuffle_smart", "Shuffle inteligente", "Evita repeticiones cercanas"),
        Triple("play.queue_persist", "Cola persistente", "Guarda la cola al cerrar"),
        Triple("play.hardware_buttons", "Botones de auriculares", "Responde a controles externos"),
        Triple("play.bluetooth_pause", "Pausa al desconectar BT", "Pausa si se pierde Bluetooth"),
        Triple("play.car_mode", "Modo coche", "Controles grandes y simplificados"),
        Triple("play.sleep_timer_ui", "Temporizador de sueño", "Apagado programado"),
        Triple("play.loudness_boost", "Boost de loudness", "Ganancia suave en pasajes bajos"),
        Triple("play.bass_boost", "Bass boost", "Refuerzo de graves"),
        Triple("play.virtualizer", "Virtualizer", "Espacialización ligera"),
        Triple("play.equalizer", "Ecualizador", "Curvas EQ activas"),
        Triple("play.replay_gain", "ReplayGain", "Ajuste por pista si hay tags"),
        Triple("play.fade_on_pause", "Fade al pausar", "Baja volumen suavemente"),
    )

    val library = listOf(
        Triple("lib.auto_download", "Descarga automática", "Descarga el primer resultado de Seki"),
        Triple("lib.wifi_only_dl", "Descargas solo Wi‑Fi", "Evita datos móviles"),
        Triple("lib.high_quality", "Alta calidad de audio", "Prefiere streams de mayor bitrate"),
        Triple("lib.cache_covers", "Cachear portadas", "Guarda artwork localmente"),
        Triple("lib.scan_local", "Escanear almacenamiento", "Incluye MP3 locales"),
        Triple("lib.hidden_folders", "Incluir carpetas ocultas", "Escanea dot-folders"),
        Triple("lib.dedupe", "Eliminar duplicados", "Detecta tracks repetidos"),
        Triple("lib.sort_recent", "Orden por reciente", "Más nuevas primero"),
        Triple("lib.sort_alpha", "Orden alfabético", "Por título"),
        Triple("lib.group_album", "Agrupar por álbum", "Secciones de álbum"),
        Triple("lib.favorites_top", "Favoritos arriba", "Pinned likes primero"),
        Triple("lib.show_unplayable", "Mostrar no reproducibles", "Lista URLs rotas"),
        Triple("lib.auto_lyrics", "Letras automáticas", "Busca LRC al reproducir"),
        Triple("lib.lyrics_offline", "Letras offline", "Cachea letras descargadas"),
        Triple("lib.import_m3u", "Importar M3U", "Soporta playlists M3U"),
        Triple("lib.export_m3u", "Exportar M3U", "Exporta playlists locales"),
        Triple("lib.backup_json", "Backup JSON", "Exporta metadatos"),
        Triple("lib.restore_json", "Restaurar JSON", "Importa metadatos"),
        Triple("lib.cloud_sync_playlists", "Sync playlists", "Sincroniza listas con cuenta"),
        Triple("lib.smart_playlists", "Playlists inteligentes", "Reglas automáticas"),
    )

    val search = listOf(
        Triple("search.debounce", "Debounce de búsqueda", "Espera antes de consultar Seki"),
        Triple("search.youtube_links", "Aceptar links de YouTube", "Importa por URL/ID"),
        Triple("search.history", "Historial de búsquedas", "Guarda consultas recientes"),
        Triple("search.suggestions", "Sugerencias", "Chips de artistas/álbumes"),
        Triple("search.instant", "Resultados instantáneos", "Filtra catálogo local al tipear"),
        Triple("search.remote_first", "Remoto primero", "Prioriza Seki sobre local"),
        Triple("search.timeout_long", "Timeout largo", "Hasta 60s para cold start"),
        Triple("search.retry", "Reintentar fallos", "Retry automático en error de red"),
        Triple("search.safe", "Filtro seguro", "Omite resultados explícitos si se activa"),
        Triple("search.preview", "Vista previa", "Snippet de audio si disponible"),
    )

    val social = listOf(
        Triple("social.listen_together", "Escuchar juntos", "Sesiones compartidas"),
        Triple("social.share_now", "Compartir ahora suena", "Comparte pista actual"),
        Triple("social.profile_public", "Perfil visible", "Nombre y usuario en app"),
        Triple("social.avatar", "Foto de perfil", "Avatar local personalizado"),
        Triple("social.friend_activity", "Actividad de amigos", "Feed si hay backend"),
        Triple("social.comments", "Comentarios", "Notas en playlists"),
    )

    val extra = listOf(
        Triple("extra.auto_download", "Auto-descarga Seki", "Guarda offline el primer hit"),
        Triple("extra.performance_image_size", "Imágenes ligeras", "Reduce carga de artwork"),
        Triple("extra.offline_first", "Offline primero", "Prioriza caché antes de red"),
        Triple("extra.refresh_manual", "Actualización manual", "Sin refresh automático"),
        Triple("extra.debug_overlay", "Overlay de depuración", "Estado técnico del player"),
        Triple("extra.crash_reports", "Reportes de fallo", "Envía stacktraces anónimos"),
        Triple("extra.beta_player", "Player beta", "Nuevas transiciones del player"),
        Triple("extra.beta_lyrics", "Letras beta", "Karaoke carácter a carácter"),
        Triple("extra.beta_glass", "Glass beta", "AGSL experimental"),
        Triple("extra.strict_https", "HTTPS estricto", "Bloquea http claro"),
        Triple("extra.verbose_log", "Logs verbosos", "Más detalle en logcat"),
        Triple("extra.experimental_eq", "EQ experimental", "Curvas adicionales"),
        Triple("extra.preload_next", "Precargar siguiente", "Buffer de la siguiente pista"),
        Triple("extra.low_power", "Modo bajo consumo", "Menos animaciones y red"),
        Triple("extra.high_refresh", "Alta tasa de refresco", "UI a 90/120 Hz si existe"),
        Triple("extra.smart_queue", "Cola inteligente", "Rellena cola con recomendaciones"),
        Triple("extra.history_limit", "Limitar historial", "Máximo 200 entradas recientes"),
        Triple("extra.auto_archive", "Archivar antiguas", "Oculta pistas sin reproducir en 90 días"),
        Triple("extra.cover_palette", "Paleta de portada", "Extrae colores del artwork"),
        Triple("extra.lyrics_emotion", "Emoción en letras", "Colorea palabras por sentimiento"),
        Triple("extra.lyrics_1_4x", "Letras 1.4x", "Rellena caracteres más rápido"),
        Triple("extra.smooth_line", "Transición de línea", "Cambio suave entre versos"),
        Triple("extra.playlist_colors", "Colores de playlist", "Acento personalizado por lista"),
        Triple("extra.playlist_shuffle_open", "Shuffle al abrir", "Mezcla al iniciar playlist"),
    )

    // Alias legacy
    val experimental = playback

    val all: List<FeatureSetting> by lazy {
        fun map(source: List<Triple<String, String, String>>) =
            source.map { FeatureSetting(it.first, it.second, it.third) }
        map(privacy) + map(customization) + map(playback) + map(library) + map(search) + map(social) + map(extra)
    }

    val categories: List<Pair<String, List<Triple<String, String, String>>>> = listOf(
        "Privacidad" to privacy,
        "Interfaz" to customization,
        "Reproducción" to playback,
        "Biblioteca" to library,
        "Búsqueda" to search,
        "Perfil y social" to social,
        "Avanzado" to extra,
    )
}
