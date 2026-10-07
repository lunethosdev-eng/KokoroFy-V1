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
        Triple("privacy.incognito", "Sesión privada", "No guardar actividad de escucha durante esta sesión"),
        Triple("privacy.history", "Historial de escucha", "Permitir que las canciones recientes aparezcan en Inicio"),
        Triple("privacy.analytics", "Analítica anónima", "Permitir métricas técnicas anónimas; no se recopilan si se desactiva"),
        Triple("privacy.screenshots", "Bloquear capturas", "Activa FLAG_SECURE para impedir capturas y grabación de pantalla"),
        Triple("privacy.notification_art", "Ocultar portada en notificación", "Evita exponer la portada en el reproductor del sistema"),
        Triple("privacy.cloud_backup", "Copia en la nube", "Permitir respaldo de preferencias cuando se añada sincronización"),
        Triple("privacy.metered", "Limitar datos móviles", "Evita descargas automáticas cuando la red es medida"),
        Triple("privacy.redact_search", "Búsqueda privada", "No conservar el texto de búsqueda al salir de Buscar"),
        Triple("privacy.external_share", "Compartir externo", "Controla si KokoroFy puede abrir el selector de compartir"),
        Triple("privacy.local_only", "Modo solo local", "No consultar Seki ni fuentes remotas"),
        Triple("privacy.hide_profile", "Ocultar perfil", "No mostrar el correo del perfil en Ajustes"),
        Triple("privacy.auto_clear_cache", "Limpiar caché al salir", "Borra caché temporal al cerrar la app"),
        Triple("privacy.auto_clear_search", "Limpiar búsquedas", "Vacía la consulta después de abandonar Buscar"),
        Triple("privacy.secure_links", "Enlaces seguros", "No abrir enlaces externos automáticamente desde la app"),
        Triple("privacy.hide_lyrics", "Ocultar letras en historial", "No asociar letras con la actividad local"),
        Triple("privacy.private_downloads", "Descargas privadas", "No exponer metadatos de descargas en vistas públicas"),
        Triple("privacy.disable_share_metadata", "Ocultar metadatos al compartir", "Comparte solo el enlace cuando sea posible"),
        Triple("privacy.no_personalization", "Sin personalización", "No usar actividad local para recomendaciones"),
        Triple("privacy.reset_session", "Restablecer sesión al salir", "Limpia el token local de sesión al cerrar sesión"),
        Triple("privacy.protect_library", "Proteger biblioteca", "Oculta la biblioteca en vistas no autenticadas"),
    )

    val customization = listOf(
        Triple("ui.glass", "Liquid Glass", "Usar superficies translúcidas, borde especular y respuesta al gesto"),
        Triple("ui.glass_strong", "Cristal intenso", "Aumenta contraste y profundidad del cristal"),
        Triple("ui.dynamic_background", "Fondo dinámico", "Usa la portada como fondo ambiental del player"),
        Triple("ui.specular", "Reflejo especular", "Muestra un highlight superior en superficies de vidrio"),
                Triple("ui.large_titles", "Títulos grandes", "Usa jerarquía tipográfica grande en Inicio"),
        Triple("ui.compact", "Listas compactas", "Reduce el espacio vertical de canciones"),
        Triple("ui.grid", "Biblioteca en cuadrícula", "Presenta la biblioteca en tarjetas cuando hay portadas"),
        Triple("ui.animated_covers", "Portadas animadas", "Aplica microanimaciones a portadas interactivas"),
        Triple("ui.cover_parallax", "Parallax de portada", "Mueve ligeramente la portada según el dispositivo"),
        Triple("ui.gyro_3d", "Portada 3D con giroscopio", "Inclina la portada con el sensor de rotación"),
        Triple("ui.nav_glass", "Nav Liquid Glass", "Mantiene la navegación inferior como cristal flotante"),
                Triple("ui.nav_compact", "Nav compacto", "Reduce la altura de la barra inferior"),
        Triple("ui.mini_progress", "Progreso del mini-player", "Muestra progreso en la superficie mini"),
        Triple("ui.blur", "Blur ambiental", "Controla la intensidad visual del fondo translúcido"),
        Triple("ui.keep_screen", "Pantalla encendida", "Evita que la pantalla se apague durante la reproducción"),
        Triple("ui.mini_blur", "Mini-player translúcido", "Da profundidad adicional al mini-player"),
        Triple("ui.player_dim", "Oscurecer fondo del player", "Ajusta el scrim detrás de la portada"),
        Triple("ui.player_radius", "Radio grande de portada", "Usa bordes suaves tipo Apple Music"),
        Triple("ui.accent_green", "Acento verde", "Mantiene el verde KokoroFy como color principal"),
        Triple("ui.system_bars", "Barras del sistema adaptativas", "Cambia iconos de estado según el tema"),
    )

    val experimental = listOf(
        Triple("exp.edge_stretch", "Gota en el límite", "Estira el botón al alcanzar el límite del gesto"),
        Triple("exp.spring_back", "Rebote elástico", "Devuelve controles a su posición con spring"),
        Triple("exp.gooey", "Gooey glass", "Añade deformación visual durante el arrastre"),
        Triple("exp.magnetic_buttons", "Botones magnéticos", "Atrae el control hacia su centro al soltar"),
        Triple("exp.press_depth", "Profundidad al pulsar", "Escala ligeramente el control durante la pulsación"),
        Triple("exp.tilt_player", "Tilt del player", "Inclina el contenido del player con movimiento"),
        Triple("exp.cover_depth", "Profundidad de portada", "Añade perspectiva 3D a la portada"),
        Triple("exp.lyrics_reveal", "Reveal de letras", "Revela caracteres según el progreso"),
        Triple("exp.lyrics_emotion", "Tipografía emocional", "Selecciona una fuente visual según palabras clave"),
        Triple("exp.lyrics_color", "Color emocional", "Selecciona acento según emoción detectada"),
        Triple("exp.lyrics_focus", "Focus dinámico", "Amplía la línea activa y atenúa las demás"),
        Triple("exp.lyrics_snap", "Snap de letras", "Centra automáticamente la línea activa"),
        Triple("exp.player_transition", "Transición de player", "Usa entrada y salida suave del player"),
        Triple("exp.mini_transition", "Transición mini-player", "Anima la aparición y minimización"),
        Triple("exp.nav_transition", "Transición del nav", "Anima cambios de pestaña"),
        Triple("exp.dynamic_scrim", "Scrim dinámico", "Ajusta el velo según la portada"),
        Triple("exp.cover_shadow", "Sombra de portada", "Añade profundidad bajo el artwork"),
        Triple("exp.glass_highlight", "Highlight móvil", "Mueve el reflejo del cristal con el gesto"),
        Triple("exp.glass_border", "Borde dinámico", "Ajusta el brillo del borde durante la interacción"),
        Triple("exp.motion_blur", "Motion blur", "Suaviza desplazamientos rápidos compatibles"),
    )

    val extra = listOf(
        Triple("extra.autoplay", "Autoplay", "Reproduce automáticamente al seleccionar una canción"),
        Triple("extra.resume", "Continuar reproducción", "Recuerda la posición de la última canción"),
        Triple("extra.remember_tab", "Recordar pestaña", "Vuelve a la última sección abierta"),
        Triple("extra.auto_queue", "Cola automática", "Añade canciones similares cuando la cola queda vacía"),
        Triple("extra.auto_lyrics", "Letras automáticas", "Carga letras al abrir el player"),
        Triple("extra.auto_download", "Descarga automática", "Descarga el resultado principal de una búsqueda"),
        Triple("extra.gapless", "Gapless", "Mantiene reproducción continua cuando el formato lo permite"),
        Triple("extra.crossfade", "Crossfade", "Suaviza el cambio entre canciones"),
        Triple("extra.skip_silence", "Saltar silencios", "Evita silencios iniciales/finales cuando se detectan"),
        Triple("extra.normalization", "Normalización", "Mantiene niveles de reproducción consistentes"),
        Triple("extra.mono", "Mono", "Mezcla canales estéreo a mono"),
        Triple("extra.shuffle", "Aleatorio por defecto", "Inicia colas nuevas en modo aleatorio"),
        Triple("extra.repeat", "Repetir por defecto", "Conserva el modo de repetición elegido"),
        Triple("extra.playback_speed", "Velocidad persistente", "Recuerda la velocidad elegida"),
        Triple("extra.download_wifi", "Descargar solo por Wi-Fi", "Evita descargas en redes móviles"),
        Triple("extra.cache_audio", "Cachear audio", "Conserva audio reproducido para reducir tráfico"),
        Triple("extra.cache_covers", "Cachear portadas", "Conserva artwork en caché"),
        Triple("extra.prefetch", "Precargar siguiente", "Prepara la siguiente canción cuando sea posible"),
        Triple("extra.smart_search", "Búsqueda inteligente", "Busca título, artista y álbum"),
        Triple("extra.search_debounce", "Búsqueda con debounce", "Evita consultas remotas por cada tecla"),
        Triple("extra.search_history", "Historial de búsqueda", "Recuerda consultas recientes localmente"),
        Triple("extra.search_suggestions", "Sugerencias", "Sugiere términos a partir del catálogo"),
        Triple("extra.sort_recent", "Orden reciente", "Ordena por incorporación reciente"),
        Triple("extra.sort_title", "Orden por título", "Ordena alfabéticamente por canción"),
        Triple("extra.sort_artist", "Orden por artista", "Agrupa por artista"),
        Triple("extra.favorite", "Favoritos", "Permite marcar canciones como favoritas"),
        Triple("extra.double_tap_favorite", "Doble toque para favorito", "Marca una canción con doble toque"),
        Triple("extra.long_press_menu", "Menú por pulsación larga", "Abre acciones rápidas manteniendo pulsado"),
        Triple("extra.share_song", "Compartir canción", "Comparte título, artista y enlace"),
        Triple("extra.download_button", "Botón descargar", "Muestra la acción de descarga en listas"),
        Triple("extra.queue_button", "Botón cola", "Abre y gestiona la cola"),
        Triple("extra.equalizer", "Ecualizador visual", "Muestra el visualizador de barras"),
        Triple("extra.mini_controls", "Controles mini", "Muestra play/pausa y siguiente"),
        Triple("extra.fullscreen_player", "Player completo", "Abre el reproductor a pantalla completa"),
        Triple("extra.gesture_player", "Gestos del player", "Permite gestos en superficies interactivas"),
        Triple("extra.swipe_lyrics", "Deslizar letras", "Permite navegar las letras verticalmente"),
        Triple("extra.tap_lyrics_seek", "Tocar letra para buscar", "Salta al timestamp de la línea"),
        Triple("extra.share_position", "Compartir posición", "Incluye posición actual al compartir"),
        Triple("extra.sleep_timer", "Temporizador de sueño", "Detiene la reproducción tras un periodo elegido"),
        Triple("extra.volume_memory", "Recordar volumen", "Recuerda el nivel de volumen"),
        Triple("extra.brightness_memory", "Recordar brillo", "Recuerda brillo preferido del player"),
        Triple("extra.orientation_lock", "Bloqueo de orientación", "Mantiene el player en orientación actual"),
        Triple("extra.accessibility_large_touch", "Áreas táctiles grandes", "Aumenta objetivos de interacción"),
        Triple("extra.accessibility_high_contrast", "Alto contraste", "Aumenta contraste de texto y controles"),
        Triple("extra.accessibility_reduce_transparency", "Menos transparencia", "Reduce translucidez para legibilidad"),
        Triple("extra.performance_low_end", "Modo rendimiento", "Reduce efectos para dispositivos lentos"),
        Triple("extra.performance_image_size", "Imágenes ligeras", "Reduce carga de artwork"),
        Triple("extra.offline_first", "Offline primero", "Prioriza contenido cacheado antes de red"),
        Triple("extra.refresh_manual", "Actualización manual", "Evita refrescos automáticos del catálogo"),
        Triple("extra.debug_overlay", "Overlay de depuración", "Muestra estado técnico del reproductor"),
    )

    val all: List<FeatureSetting> by lazy {
        fun map(source: List<Triple<String, String, String>>) =
            source.map { FeatureSetting(it.first, it.second, it.third) }
        map(privacy) + map(customization) + map(experimental) + map(extra)
    }
}
