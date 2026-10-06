# Arquitectura nativa

KokoroFy usa Media3/ExoPlayer dentro de `MusicService`, publicado mediante `MediaSessionService`.

Esto permite:
- reproducción en segundo plano
- controles del sistema Android
- controles de auriculares
- bloqueo/pantalla apagada
- Android Auto/Wear OS mediante MediaSession
- reproducción desde la caché offline

La UI se conecta con `MediaController`.

`OfflineManager` crea descargas con `DownloadService`.

`MusicCache` es compartida entre DownloadManager y ExoPlayer para que una pista descargada pueda reproducirse sin red.
