import { Play, Pause, SkipForward } from 'lucide-react'
import { motion } from 'framer-motion'
import { usePlayerStore } from '../hooks/usePlayerStore'
import { LiquidGlass } from './LiquidGlass'

export function MiniPlayer() {
  const { currentSong, isPlaying, progress, togglePlay, next, openFullPlayer } = usePlayerStore()
  if (!currentSong) return null
  return (
    <motion.div initial={{ y: 90, opacity: 0, scale: .96 }} animate={{ y: 0, opacity: 1, scale: 1 }} exit={{ y: 90, opacity: 0 }} transition={{ type: 'spring', stiffness: 420, damping: 30 }} className="fixed bottom-[82px] left-3 right-3 z-40 safe-bottom">
      <LiquidGlass className="mini-player rounded-[22px] p-2.5 flex items-center gap-3 cursor-pointer" onClick={openFullPlayer}>
        <div className="relative w-12 h-12 rounded-[15px] overflow-hidden flex-shrink-0 cover-shadow">
          {currentSong.cover_url ? <img src={currentSong.cover_url} alt="" className="w-full h-full object-cover" /> : <div className="w-full h-full bg-purple-deep flex items-center justify-center">♪</div>}
          <div className="absolute inset-x-0 bottom-0 h-0.5 bg-white/15"><div className="h-full bg-white" style={{ width: `${progress * 100}%` }}/></div>
        </div>
        <div className="flex-1 min-w-0"><p className="text-sm font-semibold truncate">{currentSong.title}</p><p className="text-xs text-white/55 truncate">{currentSong.artist}</p></div>
        <div className="flex items-center gap-1" onClick={e=>e.stopPropagation()}>
          <button onClick={togglePlay} className="w-10 h-10 rounded-full liquid-glass flex items-center justify-center pressable">{isPlaying ? <Pause size={17} fill="white"/> : <Play size={17} fill="white"/>}</button>
          <button onClick={next} className="w-9 h-9 rounded-full flex items-center justify-center pressable"><SkipForward size={18}/></button>
        </div>
      </LiquidGlass>
    </motion.div>
  )
}
