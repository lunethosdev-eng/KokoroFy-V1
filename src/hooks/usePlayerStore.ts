import { create } from 'zustand'
import { Song } from '../lib/supabase'

interface PlayerState {
  // Queue & current
  queue: Song[]
  currentIndex: number
  currentSong: Song | null
  isPlaying: boolean
  isShuffle: boolean
  repeatMode: 'off' | 'all' | 'one'
  volume: number
  progress: number // 0-1
  duration: number
  currentTime: number
  isFullPlayerOpen: boolean
  isLoading: boolean
  offlineMode: boolean
  downloadedIds: Set<string>

  // Actions
  setQueue: (songs: Song[], startIndex?: number) => void
  playSong: (song: Song, queue?: Song[]) => void
  togglePlay: () => void
  next: () => void
  prev: () => void
  seek: (time: number) => void
  setVolume: (v: number) => void
  toggleShuffle: () => void
  cycleRepeat: () => void
  setProgress: (p: number, time: number, dur: number) => void
  openFullPlayer: () => void
  closeFullPlayer: () => void
  setLoading: (l: boolean) => void
  markDownloaded: (id: string) => void
  setOfflineMode: (v: boolean) => void
}

export const usePlayerStore = create<PlayerState>((set, get) => ({
  queue: [],
  currentIndex: -1,
  currentSong: null,
  isPlaying: false,
  isShuffle: false,
  repeatMode: 'off',
  volume: 0.85,
  progress: 0,
  duration: 0,
  currentTime: 0,
  isFullPlayerOpen: false,
  isLoading: false,
  offlineMode: false,
  downloadedIds: new Set(),

  setQueue: (songs, startIndex = 0) => {
    set({
      queue: songs,
      currentIndex: startIndex,
      currentSong: songs[startIndex] || null
    })
  },

  playSong: (song, queue) => {
    const q = queue || get().queue
    const idx = q.findIndex(s => s.id === song.id)
    set({
      currentSong: song,
      currentIndex: idx >= 0 ? idx : 0,
      queue: q.length ? q : [song],
      isPlaying: true,
      isFullPlayerOpen: true
    })
  },

  togglePlay: () => set(s => ({ isPlaying: !s.isPlaying })),

  next: () => {
    const { queue, currentIndex, isShuffle, repeatMode } = get()
    if (!queue.length) return
    let nextIdx: number
    if (isShuffle) {
      nextIdx = Math.floor(Math.random() * queue.length)
    } else {
      nextIdx = currentIndex + 1
      if (nextIdx >= queue.length) {
        if (repeatMode === 'all') nextIdx = 0
        else return
      }
    }
    set({
      currentIndex: nextIdx,
      currentSong: queue[nextIdx],
      isPlaying: true,
      progress: 0,
      currentTime: 0
    })
  },

  prev: () => {
    const { queue, currentIndex, currentTime } = get()
    if (currentTime > 3) {
      // restart current
      set({ progress: 0, currentTime: 0 })
      return
    }
    if (!queue.length) return
    let prevIdx = currentIndex - 1
    if (prevIdx < 0) prevIdx = queue.length - 1
    set({
      currentIndex: prevIdx,
      currentSong: queue[prevIdx],
      isPlaying: true,
      progress: 0,
      currentTime: 0
    })
  },

  seek: (time) => set({ currentTime: time }),
  setVolume: (v) => set({ volume: Math.max(0, Math.min(1, v)) }),
  toggleShuffle: () => set(s => ({ isShuffle: !s.isShuffle })),
  cycleRepeat: () => set(s => ({
    repeatMode: s.repeatMode === 'off' ? 'all' : s.repeatMode === 'all' ? 'one' : 'off'
  })),
  setProgress: (p, time, dur) => set({ progress: p, currentTime: time, duration: dur }),
  openFullPlayer: () => set({ isFullPlayerOpen: true }),
  closeFullPlayer: () => set({ isFullPlayerOpen: false }),
  setLoading: (l) => set({ isLoading: l }),
  markDownloaded: (id) => set(s => {
    const next = new Set(s.downloadedIds)
    next.add(id)
    return { downloadedIds: next }
  }),
  setOfflineMode: (v) => set({ offlineMode: v })
}))
