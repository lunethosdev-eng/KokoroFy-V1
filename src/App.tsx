import { useEffect, useState } from 'react'
import { getCatalog, Song } from './lib/supabase'
import { usePlayerStore } from './hooks/usePlayerStore'
import { useAudioEngine } from './hooks/useAudioEngine'
import { MiniPlayer } from './components/MiniPlayer'
import { FullPlayer } from './components/FullPlayer'
import { BottomNav } from './components/BottomNav'
import { SongCard } from './components/SongCard'
import { Search, WifiOff, Settings, Plus } from 'lucide-react'
import { AccountPanel } from './components/AccountPanel'
import { LiquidGlass } from './components/LiquidGlass'
import { createPlaylist } from './lib/supabase'

export default function App() {
  const [tab, setTab] = useState('home')
  const [songs, setSongs] = useState<Song[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [isOnline, setIsOnline] = useState(navigator.onLine)
  const setQueue = usePlayerStore(s => s.setQueue)
  const setOfflineMode = usePlayerStore(s => s.setOfflineMode)
  const [accountOpen, setAccountOpen] = useState(false)
  const [playlistOpen, setPlaylistOpen] = useState(false)
  const [playlistName, setPlaylistName] = useState('')

  // Init audio engine (side effects)
  useAudioEngine()

  useEffect(() => {
    getCatalog()
      .then(data => {
        setSongs(data)
        if (data.length) setQueue(data)
      })
      .finally(() => setLoading(false))

    const onOnline = () => {
      setIsOnline(true)
      setOfflineMode(false)
    }
    const onOffline = () => {
      setIsOnline(false)
      setOfflineMode(true)
    }
    window.addEventListener('online', onOnline)
    window.addEventListener('offline', onOffline)
    return () => {
      window.removeEventListener('online', onOnline)
      window.removeEventListener('offline', onOffline)
    }
  }, [])

  const filtered = searchQuery
    ? songs.filter(
        s =>
          s.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
          s.artist.toLowerCase().includes(searchQuery.toLowerCase())
      )
    : songs

  return (
    <div className="h-full flex flex-col relative overflow-hidden">
      {/* Offline banner */}
      {!isOnline && (
        <div className="fixed top-0 left-0 right-0 z-50 bg-amber-500/90 text-black text-center text-xs py-1.5 safe-top flex items-center justify-center gap-1.5">
          <WifiOff size={12} />
          Modo offline – solo canciones descargadas
        </div>
      )}

      {/* Main content */}
      <main className="flex-1 overflow-y-auto pb-36 pt-safe">
        {/* Header */}
        <header className="sticky top-0 z-10 px-5 pt-12 pb-4 liquid-header">
          <div className="flex items-center gap-3">
            <img src="/logo.png" alt="KokoroFy" className="w-9 h-9 rounded-xl" />
            <div className="flex-1">
              <h1 className="text-xl font-bold tracking-tight">KokoroFy</h1>
              <p className="text-[11px] text-white/50">Liquid Glass Music</p>
            </div>
            <LiquidGlass className="w-10 h-10 rounded-full flex items-center justify-center cursor-pointer" onClick={() => setAccountOpen(true)}><Settings size={20}/></LiquidGlass>
          </div>
        </header>

        {tab === 'home' && (
          <div className="px-4 pt-4 space-y-6">
            <section>
              <h2 className="text-lg font-semibold mb-3 px-1">Para ti</h2>
              {loading ? (
                <div className="grid grid-cols-2 gap-3">
                  {[1, 2, 3, 4].map(i => (
                    <div key={i} className="aspect-square rounded-2xl bg-white/5 animate-pulse" />
                  ))}
                </div>
              ) : (
                <div className="grid grid-cols-2 gap-3">
                  {songs.slice(0, 6).map((s, i) => (
                    <SongCard key={s.id} song={s} queue={songs} index={i} variant="grid" />
                  ))}
                </div>
              )}
            </section>

            <section>
              <h2 className="text-lg font-semibold mb-2 px-1">Recientes</h2>
              <div className="space-y-0.5">
                {songs.slice(0, 12).map((s, i) => (
                  <SongCard key={s.id} song={s} queue={songs} index={i} />
                ))}
              </div>
            </section>
          </div>
        )}

        {tab === 'search' && (
          <div className="px-4 pt-4">
            <div className="relative mb-4">
              <Search
                size={18}
                className="absolute left-3.5 top-1/2 -translate-y-1/2 text-white/40"
              />
              <input
                type="search"
                placeholder="Artistas, canciones, álbumes..."
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                className="w-full liquid-glass rounded-2xl py-3 pl-11 pr-4 text-sm outline-none placeholder:text-white/30 focus:ring-1 focus:ring-purple-mid/50"
              />
            </div>
            <div className="space-y-0.5">
              {filtered.map((s, i) => (
                <SongCard key={s.id} song={s} queue={filtered} index={i} />
              ))}
              {!loading && filtered.length === 0 && (
                <p className="text-center text-white/40 py-12">Sin resultados</p>
              )}
            </div>
          </div>
        )}

        {tab === 'library' && (
          <div className="px-4 pt-4">
            <div className="flex items-center justify-between mb-3"><h2 className="text-lg font-semibold">Tu biblioteca</h2><LiquidGlass className="w-9 h-9 rounded-full flex items-center justify-center cursor-pointer" onClick={() => setPlaylistOpen(true)}><Plus size={18}/></LiquidGlass></div>
            <div className="space-y-0.5">
              {songs.map((s, i) => (
                <SongCard key={s.id} song={s} queue={songs} index={i} />
              ))}
            </div>
          </div>
        )}

        {tab === 'radio' && (
          <div className="px-4 pt-12 text-center text-white/40">
            <p className="text-lg">Próximamente</p>
            <p className="text-sm mt-1">Estaciones y mixes personalizados</p>
          </div>
        )}
      </main>

      <MiniPlayer />
      <FullPlayer />
      <BottomNav active={tab} onChange={setTab} />
      <AccountPanel open={accountOpen} onClose={() => setAccountOpen(false)} />
      {playlistOpen && <div className="fixed inset-0 z-[75] bg-black/55 backdrop-blur-md flex items-center justify-center p-4"><LiquidGlass className="rounded-3xl p-5 w-full max-w-sm"><h3 className="text-xl font-bold mb-3">Crear playlist</h3><input autoFocus value={playlistName} onChange={e=>setPlaylistName(e.target.value)} placeholder="Nombre de playlist" className="glass-input w-full mb-3"/><div className="flex gap-2"><button onClick={()=>setPlaylistOpen(false)} className="flex-1 py-3 rounded-2xl bg-white/8">Cancelar</button><button onClick={async()=>{try{await createPlaylist(playlistName);setPlaylistName('');setPlaylistOpen(false)}catch{setAccountOpen(true)}}} className="flex-1 py-3 rounded-2xl bg-green-400 text-black font-bold">Crear</button></div></LiquidGlass></div>}
    </div>
  )
}
