# KokoroFy Android Native

Esta es la base nativa Android para la siguiente etapa de KokoroFy.

## Lo que incluye

- UI nativa en Jetpack Compose.
- Diseño minimalista inspirado en patrones de Apple Music/Spotify, sin copiar código ni assets propietarios.
- Full Player.
- Letras desde Supabase y fallback HTTP a LRCLIB.
- Media3/ExoPlayer.
- MediaSessionService para reproducción en segundo plano y controles del sistema.
- Widget de reproducción para pantalla de inicio.
- Room para catálogo local.
- Icono proporcionado por el usuario.
- Base para ecualizador/visualizador animado.
- Descargas offline mediante Media3 DownloadService/DownloadManager y reproducción cacheada con Media3.

## Offline real

La versión nativa ya incluye `OfflineManager`, `KokoroDownloadService` y una caché de Media3 compartida con el reproductor. Al pulsar Descargar, el archivo se guarda en el almacenamiento privado de la app y el reproductor usa la caché cuando existe. Para que el modo avión funcione, el usuario debe haber descargado la pista previamente y el servidor debe permitir la descarga. Debes tener los derechos necesarios para ofrecer ese contenido offline.

## Supabase

Configura `Config.kt`:

SUPABASE_URL
SUPABASE_PUBLISHABLE_KEY

La app consulta:

`public.songs`

con:
- id
- title
- artist
- album
- duration
- audio_url
- cover_url
- lyrics
- lyrics_url
- created_at

La clave service/secret nunca debe estar dentro de la APK.

## Letras

Orden de resolución:

1. `songs.lyrics`
2. `songs.lyrics_url`
3. fallback HTTP a LRCLIB

Si ya tienes las letras en Supabase, se usan primero.

## Reproducción del sistema

`MusicService` usa Media3 `MediaSessionService`. Esto permite que el audio continúe con la pantalla apagada y que Android exponga controles multimedia del sistema.

## GitHub Actions

Coloca el proyecto en GitHub y usa JDK 17 + Android SDK. El workflow puede ejecutar:

`./gradlew assembleDebug`

El APK queda en:

`app/build/outputs/apk/debug/app-debug.apk`

## Ecualizador

La interfaz incluye un visualizador animado. El procesamiento DSP de Android requiere `android.media.audiofx.Equalizer` conectado al audio session ID de ExoPlayer. Esta base deja la capa visual preparada, pero no afirma que las barras sean un EQ DSP real.

## Icono

El PNG proporcionado se incluye como `app/src/main/res/drawable/kokorofy_icon.png`.
