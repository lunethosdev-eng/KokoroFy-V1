import { Play, Pause, SkipForward } from 'lucide-react'
import { usePlayerStore } from '../hooks/usePlayerStore'
import { motion } from 'framer-motion'

export function MiniPlayer() {
  const {
    currentSong,
    isPlaying,
    togglePlay,
    next,
    openFullPlayer,
    progress
  } = usePlayerStore()

  if (!currentSong) return null

  return (
    <motion.div
      initial={{ y: 80, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      className="fixed bottom-[72px] left-3 right-3 z-40 safe-bottom"
    >
      <div
        onClick={openFullPlayer}
        className="liquid-glass-strong rounded-2xl p-3 flex items-center gap-3 cursor-pointer pressable shadow-2xl"
      >
        {/* Cover */}
        <div className="relative w-12 h-12 rounded-xl overflow-hidden flex-shrink-0 cover-shadow">
          {currentSong.cover_url ? (
            <img
              src={currentSong.cover_url}
              alt=""
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="w-full h-full bg-purple-deep flex items-center justify-center text-xs">
              ♪
            </div>
          )}
          {/* tiny progress ring */}
          <svg className="absolute inset-0 w-full h-full -rotate-90 opacity-80">
            <circle
              cx="24"
              cy="24"
              r="22"
              fill="none"
              stroke="rgba(196,181,253,0.3)"
              strokeWidth="2"
            />
            <circle
              cx="24"
              cy="24"
              r="22"
              fill="none"
              stroke="#c4b5fd"
              strokeWidth="2"
              strokeDasharray={`${progress * 138} 138`}
              strokeLinecap="round"
            />
          </svg>
        </div>

        {/* Info */}
        <div className="flex-1 min-w-0">
          <p className="text-sm font-semibold truncate">{currentSong.title}</p>
          <p className="text-xs text-white/60 truncate">{currentSong.artist}</p>
        </div>

        {/* Controls */}
        <div className="flex items-center gap-1" onClick={e => e.stopPropagation()}>
          <button
            onClick={togglePlay}
            className="w-10 h-10 rounded-full flex items-center justify-center liquid-glass pressable"
          >
            {isPlaying ? (
              <Pause size={18} fill="white" />
            ) : (
              <Play size={18} fill="white" className="ml-0.5" />
            )}
          </button>
          <button
            onClick={next}
            className="w-9 h-9 rounded-full flex items-center justify-center hover:bg-white/10 pressable"
          >
            <SkipForward size={18} />
          </button>
        </div>
      </div>
    </motion.div>
  )
}
