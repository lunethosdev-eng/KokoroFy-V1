import { createClient } from '@supabase/supabase-js'

// Usa las credenciales de tu proyecto Supabase (las del mensaje del usuario)
// Reemplaza PUBLISHABLE_KEY con tu anon/public key real
const SUPABASE_URL = 'https://esjoifsjljvymttinyhj.supabase.co'
const SUPABASE_ANON_KEY = import.meta.env.VITE_SUPABASE_ANON_KEY || 'PUBLISHABLE_OR_SERVICE_KEY'

export const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY)

export interface Song {
  id: string
  title: string
  artist: string
  duration?: number
  audio_url: string
  cover_url?: string
  album?: string
  lyrics?: { text: string; time: number }[] | string
  created_at?: string
}

export async function getCatalog(): Promise<Song[]> {
  // Intenta primero tabla "songs", luego "tracks" (Kokoro schema)
  let { data, error } = await supabase
    .from('songs')
    .select('*')
    .order('created_at', { ascending: false })

  if (error || !data || data.length === 0) {
    const res = await supabase
      .from('tracks')
      .select('id, title, artist, audio_url, cover_url, album, lyrics, created_at')
      .order('created_at', { ascending: false })
      .limit(100)
    data = res.data as any
    error = res.error
  }

  if (error) {
    console.error('Error fetching catalog:', error)
    return []
  }

  return (data || []).map((s: any) => ({
    id: String(s.id),
    title: s.title || 'Unknown',
    artist: s.artist || 'Unknown Artist',
    duration: s.duration || 0,
    audio_url: s.audio_url || s.stream_url || '',
    cover_url: s.cover_url || '',
    album: s.album || '',
    lyrics: Array.isArray(s.lyrics) ? s.lyrics : (typeof s.lyrics === 'string' ? JSON.parse(s.lyrics || '[]') : []),
    created_at: s.created_at
  }))
}

export async function searchSongs(query: string): Promise<Song[]> {
  const { data, error } = await supabase
    .from('songs')
    .select('*')
    .or(`title.ilike.%${query}%,artist.ilike.%${query}%`)
    .limit(50)

  if (error) {
    // fallback tracks
    const res = await supabase
      .from('tracks')
      .select('*')
      .or(`title.ilike.%${query}%,artist.ilike.%${query}%`)
      .limit(50)
    return (res.data || []).map((s: any) => ({
      id: String(s.id),
      title: s.title || 'Unknown',
      artist: s.artist || 'Unknown',
      audio_url: s.audio_url || '',
      cover_url: s.cover_url || '',
      lyrics: s.lyrics || []
    }))
  }

  return (data || []).map((s: any) => ({
    id: String(s.id),
    title: s.title,
    artist: s.artist,
    audio_url: s.audio_url,
    cover_url: s.cover_url,
    lyrics: s.lyrics || []
  }))
}
