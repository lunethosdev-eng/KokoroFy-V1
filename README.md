# KokoroFy – Liquid Glass Music App

App de música inspirada en **Apple Music** + **Spotify**, con efecto **Liquid Glass** real, equalizer con Web Audio API, letras sincronizadas, reproducción offline y listo para convertir a APK con Capacitor + GitHub Actions.

## Características

- **Bootloader** animado (el video pixel moon → KokoroFy)
- **Liquid Glass** CSS (blur + saturación + specular + filtro SVG de refracción)
- **Full Player** profesional estilo Apple Music (gestos, sheet, letras, EQ real)
- **Equalizer real** con `AnalyserNode` de Web Audio API (barras animadas según frecuencias)
- **Letras sincronizadas** (usa el campo `lyrics` jsonb de Supabase)
- **Offline**: descarga canciones al Cache API / Service Worker (PWA)
- **Mini-player** flotante con progress ring
- **Navegación** inferior glass (Inicio / Buscar / Biblioteca / Radio)
- **PWA** instalable + Service Worker para cache de audio y covers
- **Capacitor** listo para APK nativo
- **GitHub Actions** workflow para generar el APK automáticamente

## Setup rápido

```bash
cd kokorofy-music-app
cp .env.example .env
# Edita .env y pon tu VITE_SUPABASE_ANON_KEY (anon key pública de Supabase)

npm install
npm run dev
```

Abre http://localhost:5173

## Supabase

La app intenta primero la tabla `songs` y si no existe usa `tracks` (schema Kokoro).

Campos esperados:
- `id`, `title`, `artist`, `audio_url`, `cover_url`, `duration`, `album`
- `lyrics` → array de `{ text: string, time: number }` (segundos)

URL por defecto: `https://esjoifsjljvymttinyhj.supabase.co`  
(cámbiala en `src/lib/supabase.ts` si usas otro proyecto)

## Build producción (web / PWA)

```bash
npm run build
npm run preview
```

## Convertir a APK (nativo)

```bash
# 1. Build web
npm run build

# 2. Inicializar Capacitor (solo la primera vez)
npx cap init KokoroFy com.kokorofy.music --web-dir dist
npx cap add android

# 3. Sync y abrir Android Studio
npx cap sync android
npx cap open android
```

O usa el workflow de GitHub Actions (`.github/workflows/build-apk.yml`):
1. Sube el repo a GitHub
2. Añade el secret `VITE_SUPABASE_ANON_KEY`
3. Push a `main` → se genera el APK como artifact

## Widgets nativos

Para widgets de home screen (Android):
- En el proyecto Android generado por Capacitor añade un `AppWidgetProvider`
- Usa el MediaSession API (ya soportado por el `<audio>` + Web Audio) para controles de notificación/lockscreen
- Capacitor Community tiene plugins de Media Session y Background Mode

## Iconos

- Logo: `/public/logo.png` (el pixel moon que enviaste)
- Bootloader video: `/public/bootloader.mp4`

## Estructura

```
src/
  components/   # MiniPlayer, FullPlayer, Equalizer, SongCard, BottomNav
  hooks/        # usePlayerStore (Zustand), useAudioEngine (Web Audio + offline)
  lib/          # supabase client
  styles/       # Liquid Glass CSS
public/         # logo + bootloader video
.github/workflows/build-apk.yml
```

## Notas

- El equalizer solo funciona cuando el usuario interactúa (autoplay policies)
- Offline requiere que el usuario pulse “Descargar” en el full player
- Liquid Glass se ve mejor en Chrome/Edge (filtro SVG + backdrop-filter)
- En iOS Safari el blur funciona, la refracción SVG es limitada

Hecho con ❤️ para KokoroFy
