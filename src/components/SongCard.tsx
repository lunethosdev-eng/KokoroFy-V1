import { Play } from 'lucide-react'
import { Song } from '../lib/supabase'
import { usePlayerStore } from '../hooks/usePlayerStore'

interface SongCardProps {
  song: Song
  queue: Song[]
  index: number
  variant?: 'list' | 'grid'
}

export function SongCard({ song, queue, index, variant = 'list' }: SongCardProps) {
  const playSong = usePlayerStore(s => s.playSong)
  const currentSong = usePlayerStore(s => s.currentSong)
  const isPlaying = usePlayerStore(s => s.isPlaying)
  const isCurrent = currentSong?.id === song.id

  if (variant === 'grid') {
    return (
      <button
        onClick={() => playSong(song, queue)}
        className="flex flex-col gap-2 text-left pressable group"
      >
        <div className="relative aspect-square rounded-2xl overflow-hidden cover-shadow">
          {song.cover_url ? (
            <img src={song.cover_url} alt="" className="w-full h-full object-cover" />
          ) : (
            <div className="w-full h-full bg-purple-deep/40 flex items-center justify-center text-3xl">
              ♪
            </div>
          )}
          <div className="absolute inset-0 bg-black/30 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
            <div className="w-12 h-12 rounded-full bg-white/90 flex items-center justify-center">
              <Play size={20} fill="black" className="ml-0.5" />
            </div>
          </div>
          {isCurrent && isPlaying && (
            <div className="absolute bottom-2 right-2 w-6 h-6 rounded-full bg-purple-mid flex items-center justify-center">
              <div className="flex gap-0.5 items-end h-3">
                <span className="w-0.5 bg-white animate-equalizer" style={{ animationDelay: '0s' }} />
                <span className="w-0.5 bg-white animate-equalizer" style={{ animationDelay: '0.2s' }} />
                <span className="w-0.5 bg-white animate-equalizer" style={{ animationDelay: '0.4s' }} />
              </div>
            </div>
          )}
        </div>
        <div>
          <p className={`text-sm font-medium truncate ${isCurrent ? 'text-purple-mid' : ''}`}>
            {song.title}
          </p>
          <p className="text-xs text-white/50 truncate">{song.artist}</p>
        </div>
      </button>
    )
  }

  // List variant (Apple Music style)
  return (
    <button
      onClick={() => playSong(song, queue)}
      className="w-full flex items-center gap-3 px-4 py-2.5 hover:bg-white/5 rounded-xl pressable text-left"
    >
      <span className="w-6 text-center text-sm text-white/40 tabular-nums">
        {isCurrent && isPlaying ? (
          <span className="text-purple-mid flex gap-0.5 justify-center items-end h-4">
            <span className="w-0.5 bg-current animate-equalizer" style={{ height: '40%' }} />
            <span className="w-0.5 bg-current animate-equalizer" style={{ height: '80%', animationDelay: '0.15s' }} />
            <span className="w-0.5 bg-current animate-equalizer" style={{ height: '55%', animationDelay: '0.3s' }} />
          </span>
        ) : (
          index + 1
        )}
      </span>
      <div className="w-12 h-12 rounded-lg overflow-hidden flex-shrink-0">
        {song.cover_url ? (
          <img src={song.cover_url} alt="" className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full bg-purple-deep/50 flex items-center justify-center text-lg">
            ♪
          </div>
        )}
      </div>
      <div className="flex-1 min-w-0">
        <p className={`text-sm font-medium truncate ${isCurrent ? 'text-purple-mid' : ''}`}>
          {song.title}
        </p>
        <p className="text-xs text-white/50 truncate">{song.artist}</p>
      </div>
      {song.duration ? (
        <span className="text-xs text-white/40 tabular-nums">
          {Math.floor(song.duration / 60)}:{(song.duration % 60).toString().padStart(2, '0')}
        </span>
      ) : null}
    </button>
  )
}
