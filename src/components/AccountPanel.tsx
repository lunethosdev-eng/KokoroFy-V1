import { useEffect, useState } from 'react'
import { LogIn, LogOut, UserRound, X, ListPlus } from 'lucide-react'
import { AnimatePresence, motion } from 'framer-motion'
import { createPlaylist, getPlaylists, getSession, signIn, signOut, signUp, supabase, Playlist } from '../lib/supabase'
import { LiquidGlass } from './LiquidGlass'

export function AccountPanel({ open, onClose }: { open: boolean; onClose: () => void }) {
  const [session, setSession] = useState<any>(null)
  const [mode, setMode] = useState<'login' | 'signup'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [name, setName] = useState('')
  const [playlists, setPlaylists] = useState<Playlist[]>([])
  const [playlistName, setPlaylistName] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  const refresh = async () => {
    const { data } = await getSession()
    setSession(data.session)
    if (data.session) {
      try { setPlaylists(await getPlaylists()) } catch { setPlaylists([]) }
    }
  }

  useEffect(() => {
    refresh()
    const { data } = supabase.auth.onAuthStateChange(() => refresh())
    return () => data.subscription.unsubscribe()
  }, [])

  const submit = async () => {
    setBusy(true); setMessage('')
    try {
      if (mode === 'signup') await signUp(email, password, name)
      else await signIn(email, password)
      await refresh()
      setMessage(mode === 'signup' ? 'Cuenta creada. Revisa tu correo si se solicita confirmación.' : 'Sesión iniciada.')
    } catch (e: any) { setMessage(e?.message || 'No se pudo completar la operación.') }
    finally { setBusy(false) }
  }

  const addPlaylist = async () => {
    if (!playlistName.trim()) return
    setBusy(true); setMessage('')
    try {
      const playlist = await createPlaylist(playlistName)
      setPlaylists(p => [playlist, ...p]); setPlaylistName(''); setMessage('Playlist creada.')
    } catch (e: any) { setMessage(e?.message || 'No se pudo crear la playlist.') }
    finally { setBusy(false) }
  }

  return (
    <AnimatePresence>
      {open && (
        <motion.div className="fixed inset-0 z-[80] bg-black/55 backdrop-blur-md flex items-end sm:items-center justify-center p-3" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
          <motion.div initial={{ y: 60, scale: .96 }} animate={{ y: 0, scale: 1 }} exit={{ y: 40, scale: .98 }} transition={{ type: 'spring', stiffness: 420, damping: 30 }} className="w-full max-w-md max-h-[88vh] overflow-y-auto rounded-[28px]">
            <LiquidGlass className="p-5">
            <div className="flex items-center justify-between mb-5">
              <div><p className="text-xs uppercase tracking-[.18em] text-white/45">Cuenta KokoroFy</p><h2 className="text-2xl font-bold mt-1">{session ? 'Tu cuenta' : mode === 'login' ? 'Entrar' : 'Crear cuenta'}</h2></div>
              <LiquidGlass className="w-10 h-10 rounded-full flex items-center justify-center cursor-pointer" onClick={onClose}><X size={19}/></LiquidGlass>
            </div>
            {!session ? (
              <div className="space-y-3">
                {mode === 'signup' && <input value={name} onChange={e=>setName(e.target.value)} placeholder="Nombre" className="glass-input" />}
                <input type="email" value={email} onChange={e=>setEmail(e.target.value)} placeholder="Correo" className="glass-input" />
                <input type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder="Contraseña" className="glass-input" />
                <LiquidGlass className="rounded-2xl px-4 py-3 text-center font-semibold cursor-pointer" onClick={submit}><span className="text-green-400">{busy ? '...' : mode === 'login' ? 'Entrar' : 'Registrarme'}</span></LiquidGlass>
                <button onClick={()=>setMode(mode === 'login' ? 'signup' : 'login')} className="w-full text-sm text-white/55 py-2">{mode === 'login' ? 'Crear una cuenta nueva' : 'Ya tengo una cuenta'}</button>
              </div>
            ) : (
              <div className="space-y-4">
                <div className="flex items-center gap-3 p-3 rounded-2xl bg-white/5"><div className="w-11 h-11 rounded-full bg-white/10 flex items-center justify-center"><UserRound size={20}/></div><div className="min-w-0"><p className="font-semibold truncate">{session.user.user_metadata?.display_name || 'Usuario KokoroFy'}</p><p className="text-xs text-white/50 truncate">{session.user.email}</p></div></div>
                <div className="rounded-2xl bg-white/5 p-4"><div className="flex items-center justify-between mb-3"><h3 className="font-semibold">Tus playlists</h3><ListPlus size={18}/></div><div className="flex gap-2"><input value={playlistName} onChange={e=>setPlaylistName(e.target.value)} placeholder="Nueva playlist" className="glass-input flex-1"/><button onClick={addPlaylist} disabled={busy} className="px-4 rounded-xl bg-green-400 text-black font-bold">+</button></div><div className="mt-3 space-y-2">{playlists.map(p=><div key={p.id} className="px-3 py-2 rounded-xl bg-black/20 text-sm">{p.name}</div>)}{!playlists.length && <p className="text-xs text-white/40">Todavía no tienes playlists.</p>}</div></div>
                <button onClick={async()=>{await signOut(); await refresh()}} className="w-full rounded-2xl bg-white/8 border border-white/10 py-3 flex items-center justify-center gap-2"><LogOut size={17}/> Cerrar sesión</button>
              </div>
            )}
            {message && <p className="text-xs text-white/55 mt-3 leading-relaxed">{message}</p>}
            </LiquidGlass>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
