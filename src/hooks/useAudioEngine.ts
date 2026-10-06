import { useEffect, useRef, useCallback } from 'react'
import { usePlayerStore } from './usePlayerStore'

/**
 * Real audio engine with Web Audio API for:
 * - Streaming playback
 * - AnalyserNode for true equalizer visualization
 * - Offline caching via Cache API
 */
export function useAudioEngine() {
  const audioRef = useRef<HTMLAudioElement | null>(null)
  const ctxRef = useRef<AudioContext | null>(null)
  const analyserRef = useRef<AnalyserNode | null>(null)
  const sourceRef = useRef<MediaElementAudioSourceNode | null>(null)
  const dataArrayRef = useRef<Uint8Array | null>(null)
  const rafRef = useRef<number>(0)

  const {
    currentSong,
    isPlaying,
    volume,
    repeatMode,
    next,
    setProgress,
    setLoading,
    markDownloaded
  } = usePlayerStore()

  // Init audio + Web Audio
  useEffect(() => {
    const audio = new Audio()
    audio.crossOrigin = 'anonymous'
    audio.preload = 'auto'
    audioRef.current = audio

    const onTimeUpdate = () => {
      if (!audio.duration || isNaN(audio.duration)) return
      setProgress(audio.currentTime / audio.duration, audio.currentTime, audio.duration)
    }
    const onEnded = () => {
      if (repeatMode === 'one') {
        audio.currentTime = 0
        audio.play()
      } else {
        next()
      }
    }
    const onWaiting = () => setLoading(true)
    const onCanPlay = () => setLoading(false)
    const onError = () => setLoading(false)

    audio.addEventListener('timeupdate', onTimeUpdate)
    audio.addEventListener('ended', onEnded)
    audio.addEventListener('waiting', onWaiting)
    audio.addEventListener('canplay', onCanPlay)
    audio.addEventListener('error', onError)

    return () => {
      audio.pause()
      audio.removeEventListener('timeupdate', onTimeUpdate)
      audio.removeEventListener('ended', onEnded)
      audio.removeEventListener('waiting', onWaiting)
      audio.removeEventListener('canplay', onCanPlay)
      audio.removeEventListener('error', onError)
      if (rafRef.current) cancelAnimationFrame(rafRef.current)
      ctxRef.current?.close()
    }
  }, [])

  // Setup Web Audio Analyser when first play
  const ensureAudioContext = useCallback(() => {
    if (ctxRef.current || !audioRef.current) return
    const ctx = new (window.AudioContext || (window as any).webkitAudioContext)()
    const analyser = ctx.createAnalyser()
    analyser.fftSize = 64
    analyser.smoothingTimeConstant = 0.75
    const source = ctx.createMediaElementSource(audioRef.current)
    source.connect(analyser)
    analyser.connect(ctx.destination)
    ctxRef.current = ctx
    analyserRef.current = analyser
    dataArrayRef.current = new Uint8Array(analyser.frequencyBinCount)
    sourceRef.current = source
  }, [])

  // Load & play song
  useEffect(() => {
    const audio = audioRef.current
    if (!audio || !currentSong?.audio_url) return

    setLoading(true)
    audio.src = currentSong.audio_url
    audio.load()

    // Try cache for offline
    if ('caches' in window) {
      caches.open('audio-cache').then(cache => {
        cache.match(currentSong.audio_url).then(res => {
          if (res) {
            // already cached
            markDownloaded(currentSong.id)
          }
        })
      })
    }

    if (isPlaying) {
      ensureAudioContext()
      audio.play().catch(() => setLoading(false))
    }
  }, [currentSong?.id])

  // Play / Pause
  useEffect(() => {
    const audio = audioRef.current
    if (!audio) return
    if (isPlaying) {
      ensureAudioContext()
      if (ctxRef.current?.state === 'suspended') {
        ctxRef.current.resume()
      }
      audio.play().catch(console.error)
    } else {
      audio.pause()
    }
  }, [isPlaying])

  // Volume
  useEffect(() => {
    if (audioRef.current) audioRef.current.volume = volume
  }, [volume])

  // Seek helper
  const seek = useCallback((time: number) => {
    if (audioRef.current) {
      audioRef.current.currentTime = time
      usePlayerStore.getState().seek(time)
    }
  }, [])

  // Get equalizer frequency data (0-255 per band)
  const getEQData = useCallback((): number[] => {
    const analyser = analyserRef.current
    const data = dataArrayRef.current
    if (!analyser || !data) return Array(16).fill(0)
    analyser.getByteFrequencyData(data)
    // downsample to 16 bars
    const bars = 16
    const step = Math.floor(data.length / bars)
    const result: number[] = []
    for (let i = 0; i < bars; i++) {
      let sum = 0
      for (let j = 0; j < step; j++) sum += data[i * step + j]
      result.push(sum / step)
    }
    return result
  }, [])

  // Cache current song for offline
  const downloadForOffline = useCallback(async () => {
    if (!currentSong?.audio_url || !('caches' in window)) return false
    try {
      const cache = await caches.open('audio-cache')
      await cache.add(currentSong.audio_url)
      if (currentSong.cover_url) {
        await cache.add(currentSong.cover_url).catch(() => {})
      }
      markDownloaded(currentSong.id)
      return true
    } catch (e) {
      console.error('Offline download failed', e)
      return false
    }
  }, [currentSong])

  return {
    seek,
    getEQData,
    downloadForOffline,
    audioRef
  }
}
