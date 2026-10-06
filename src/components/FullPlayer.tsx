import { useEffect, useRef, useState } from 'react'
import {
  ChevronDown,
  Play,
  Pause,
  SkipBack,
  SkipForward,
  Shuffle,
  Repeat,
  Repeat1,
  Download,
  Share2,
  MoreHorizontal,
  Mic2
} from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import { usePlayerStore } from '../hooks/usePlayerStore'
import { useAudioEngine } from '../hooks/useAudioEngine'
import { Equalizer } from './Equalizer'

function formatTime(s: number) {
  if (!s || isNaN(s)) return '0:00'
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${m}:${sec.toString().padStart(2, '0')}`
}

export function FullPlayer() {
  const {
    currentSong,
    isPlaying,
    isFullPlayerOpen,
    progress,
    currentTime,
    duration,
    isShuffle,
    repeatMode,
    isLoading,
    downloadedIds,
    closeFullPlayer,
    togglePlay,
    next,
    prev,
    toggleShuffle,
    cycleRepeat
  } = usePlayerStore()

  const { seek, downloadForOffline } = useAudioEngine()
  const [showLyrics, setShowLyrics] = useState(false)
  const [downloading, setDownloading] = useState(false)
  const progressRef = useRef<HTMLDivElement>(null)
  const lyricsRef = useRef<HTMLDivElement>(null)

  const lyrics = Array.isArray(currentSong?.lyrics) ? currentSong.lyrics : []

  // Auto-scroll lyrics
  useEffect(() => {
    if (!showLyrics || !lyrics.length || !lyricsRef.current) return
    const activeIdx = lyrics.findIndex((l, i) => {
      const next = lyrics[i + 1]
      return currentTime >= l.time && (!next || currentTime < next.time)
    })
    if (activeIdx >= 0) {
      const el = lyricsRef.current.children[activeIdx] as HTMLElement
      el?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }, [currentTime, showLyrics, lyrics])

  if (!currentSong) return null

  const isDownloaded = downloadedIds.has(currentSong.id)

  const handleProgressClick = (e: React.MouseEvent) => {
    if (!progressRef.current || !duration) return
    const rect = progressRef.current.getBoundingClientRect()
    const pct = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
    seek(pct * duration)
  }

  const handleDownload = async () => {
    setDownloading(true)
    await downloadForOffline()
    setDownloading(false)
  }

  return (
    <AnimatePresence>
      {isFullPlayerOpen && (
        <motion.div
          initial={{ y: '100%' }}
          animate={{ y: 0 }}
          exit={{ y: '100%' }}
          transition={{ type: 'spring', damping: 28, stiffness: 300 }}
          className="fixed inset-0 z-50 flex flex-col bg-gradient-to-b from-[#2a1545] via-[#1a0a2e] to-[#0f0618] safe-top safe-bottom"
        >
          {/* Header */}
          <div className="flex items-center justify-between px-5 pt-3 pb-2">
            <button
              onClick={closeFullPlayer}
              className="w-10 h-10 rounded-full liquid-glass flex items-center justify-center pressable"
            >
              <ChevronDown size={22} />
            </button>
            <div className="text-center">
              <p className="text-[11px] uppercase tracking-widest text-white/50">
                Reproduciendo
              </p>
              <p className="text-sm font-medium truncate max-w-[200px]">
                {currentSong.album || 'KokoroFy'}
              </p>
            </div>
            <button className="w-10 h-10 rounded-full liquid-glass flex items-center justify-center pressable">
              <MoreHorizontal size={20} />
            </button>
          </div>

          {/* Cover + EQ */}
          <div className="flex-1 flex flex-col items-center justify-center px-8 gap-6 min-h-0">
            <motion.div
              layout
              className="relative w-full max-w-[320px] aspect-square rounded-3xl overflow-hidden cover-shadow"
              style={{
                boxShadow: isPlaying
                  ? '0 30px 80px rgba(124, 58, 237, 0.4)'
                  : '0 20px 60px rgba(0,0,0,0.5)'
              }}
            >
              {currentSong.cover_url ? (
                <img
                  src={currentSong.cover_url}
                  alt={currentSong.title}
                  className="w-full h-full object-cover"
                />
              ) : (
                <div className="w-full h-full bg-purple-deep flex items-center justify-center text-6xl">
                  ♪
                </div>
              )}
              {/* Glass overlay on cover when loading */}
              {isLoading && (
                <div className="absolute inset-0 liquid-glass flex items-center justify-center">
                  <div className="w-10 h-10 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                </div>
              )}
            </motion.div>

            {/* Real Equalizer */}
            <Equalizer height={48} className="w-full max-w-[280px] opacity-90" />
          </div>

          {/* Info + Controls */}
          <div className="px-6 pb-6 space-y-5">
            {/* Title / Artist */}
            <div className="text-center space-y-1">
              <h1 className="text-2xl font-bold tracking-tight truncate">
                {currentSong.title}
              </h1>
              <p className="text-base text-white/60 truncate">{currentSong.artist}</p>
            </div>

            {/* Progress */}
            <div className="space-y-1.5">
              <div
                ref={progressRef}
                onClick={handleProgressClick}
                className="progress-track h-[5px] cursor-pointer group"
              >
                <div
                  className="progress-fill relative"
                  style={{ width: `${progress * 100}%` }}
                >
                  <div className="absolute right-0 top-1/2 -translate-y-1/2 w-3.5 h-3.5 rounded-full bg-white shadow-lg opacity-0 group-hover:opacity-100 transition-opacity" />
                </div>
              </div>
              <div className="flex justify-between text-[11px] text-white/50 tabular-nums">
                <span>{formatTime(currentTime)}</span>
                <span>{formatTime(duration)}</span>
              </div>
            </div>

            {/* Main controls */}
            <div className="flex items-center justify-center gap-6">
              <button
                onClick={toggleShuffle}
                className={`p-2 rounded-full pressable ${isShuffle ? 'text-purple-mid' : 'text-white/50'}`}
              >
                <Shuffle size={20} />
              </button>

              <button onClick={prev} className="p-2 pressable">
                <SkipBack size={28} fill="white" />
              </button>

              <button
                onClick={togglePlay}
                className="w-16 h-16 rounded-full bg-white text-black flex items-center justify-center pressable shadow-xl shadow-white/20"
              >
                {isPlaying ? (
                  <Pause size={28} fill="black" />
                ) : (
                  <Play size={28} fill="black" className="ml-1" />
                )}
              </button>

              <button onClick={next} className="p-2 pressable">
                <SkipForward size={28} fill="white" />
              </button>

              <button
                onClick={cycleRepeat}
                className={`p-2 rounded-full pressable ${
                  repeatMode !== 'off' ? 'text-purple-mid' : 'text-white/50'
                }`}
              >
                {repeatMode === 'one' ? <Repeat1 size={20} /> : <Repeat size={20} />}
              </button>
            </div>

            {/* Secondary actions */}
            <div className="flex items-center justify-between px-2">
              <button
                onClick={() => setShowLyrics(!showLyrics)}
                className={`flex items-center gap-1.5 text-sm pressable ${
                  showLyrics ? 'text-purple-mid' : 'text-white/60'
                }`}
              >
                <Mic2 size={18} />
                Letras
              </button>

              <button
                onClick={handleDownload}
                disabled={isDownloaded || downloading}
                className={`flex items-center gap-1.5 text-sm pressable ${
                  isDownloaded ? 'text-green-400' : 'text-white/60'
                }`}
              >
                <Download size={18} />
                {isDownloaded ? 'Offline' : downloading ? '...' : 'Descargar'}
              </button>

              <button className="text-white/60 pressable">
                <Share2 size={18} />
              </button>
            </div>
          </div>

          {/* Lyrics panel */}
          <AnimatePresence>
            {showLyrics && (
              <motion.div
                initial={{ height: 0, opacity: 0 }}
                animate={{ height: '40%', opacity: 1 }}
                exit={{ height: 0, opacity: 0 }}
                className="absolute bottom-0 left-0 right-0 liquid-glass-strong rounded-t-3xl overflow-hidden"
              >
                <div className="h-full overflow-y-auto px-6 py-5" ref={lyricsRef}>
                  {lyrics.length === 0 ? (
                    <p className="text-center text-white/40 py-10">
                      No hay letras disponibles
                    </p>
                  ) : (
                    lyrics.map((line, i) => {
                      const next = lyrics[i + 1]
                      const isActive =
                        currentTime >= line.time && (!next || currentTime < next.time)
                      const isPassed = currentTime > (next?.time ?? Infinity)
                      return (
                        <p
                          key={i}
                          className={`lyric-line py-2 text-lg leading-relaxed text-center ${
                            isActive
                              ? 'active'
                              : isPassed
                              ? 'passed'
                              : 'upcoming'
                          }`}
                          onClick={() => seek(line.time)}
                        >
                          {line.text}
                        </p>
                      )
                    })
                  )}
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
