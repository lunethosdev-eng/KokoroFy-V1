import { useEffect, useRef, useState } from 'react'
import { ChevronDown, Play, Pause, SkipBack, SkipForward, Shuffle, Repeat, Repeat1, Download, Share2, MoreHorizontal, Mic2 } from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import { LiquidGlass } from './LiquidGlass'
import { usePlayerStore } from '../hooks/usePlayerStore'
import { useAudioEngine } from '../hooks/useAudioEngine'
import { Equalizer } from './Equalizer'
import { useDeviceTilt } from '../hooks/useDeviceTilt'

function formatTime(s: number) { if (!s || isNaN(s)) return '0:00'; const m=Math.floor(s/60); const sec=Math.floor(s%60); return `${m}:${sec.toString().padStart(2,'0')}` }

export function FullPlayer() {
  const { currentSong,isPlaying,isFullPlayerOpen,progress,currentTime,duration,isShuffle,repeatMode,isLoading,downloadedIds,closeFullPlayer,togglePlay,next,prev,toggleShuffle,cycleRepeat } = usePlayerStore()
  const { seek, downloadForOffline } = useAudioEngine()
  const [showLyrics,setShowLyrics]=useState(false)
  const [downloading,setDownloading]=useState(false)
  const progressRef=useRef<HTMLDivElement>(null)
  const lyricsRef=useRef<HTMLDivElement>(null)
  const tilt=useDeviceTilt(isFullPlayerOpen)
  const lyrics=Array.isArray(currentSong?.lyrics) ? currentSong.lyrics : []

  useEffect(()=>{
    if(!showLyrics || !lyrics.length || !lyricsRef.current) return
    const activeIdx=lyrics.findIndex((l,i)=>{const n=lyrics[i+1];return currentTime>=l.time && (!n || currentTime<n.time)})
    if(activeIdx>=0) (lyricsRef.current.children[activeIdx] as HTMLElement)?.scrollIntoView({behavior:'smooth',block:'center'})
  },[currentTime,showLyrics,lyrics])
  if(!currentSong) return null
  const isDownloaded=downloadedIds.has(currentSong.id)
  const handleProgressClick=(e:React.MouseEvent)=>{if(!progressRef.current||!duration)return;const r=progressRef.current.getBoundingClientRect();seek(Math.max(0,Math.min(1,(e.clientX-r.left)/r.width))*duration)}
  const handleDownload=async()=>{setDownloading(true);await downloadForOffline();setDownloading(false)}

  return <AnimatePresence>
    {isFullPlayerOpen && <motion.div initial={{y:'100%',opacity:.8}} animate={{y:0,opacity:1}} exit={{y:'100%',opacity:0}} transition={{type:'spring',damping:30,stiffness:340,mass:.8}} className="fixed inset-0 z-50 flex flex-col player-bg safe-top safe-bottom overflow-hidden">
      <div className="player-orb player-orb-a"/><div className="player-orb player-orb-b"/>
      <div className="relative z-10 flex items-center justify-between px-5 pt-3 pb-2">
        <LiquidGlass className="w-11 h-11 rounded-full flex items-center justify-center pressable cursor-pointer" onClick={closeFullPlayer}><ChevronDown size={22}/></LiquidGlass>
        <div className="text-center min-w-0"><p className="text-[10px] uppercase tracking-[.22em] text-white/45">Reproduciendo desde</p><p className="text-sm font-semibold truncate max-w-[210px]">{currentSong.album||'KokoroFy'}</p></div>
        <LiquidGlass className="w-11 h-11 rounded-full flex items-center justify-center pressable cursor-pointer"><MoreHorizontal size={20}/></LiquidGlass>
      </div>

      <div className="relative z-10 flex-1 flex flex-col items-center justify-center px-7 gap-5 min-h-0">
        <motion.div className="cover-3d-shell relative w-full max-w-[330px] aspect-square rounded-[30px] overflow-hidden" animate={{ rotateX: tilt.x, rotateY: tilt.y, scale: isPlaying ? 1 : .985 }} transition={{ type:'spring', stiffness:120, damping:18 }} style={{ transformPerspective: 900 }}>
          <div className="cover-3d-glow"/>
          {currentSong.cover_url?<img src={currentSong.cover_url} alt={currentSong.title} className="relative z-[1] w-full h-full object-cover"/>:<div className="relative z-[1] w-full h-full bg-purple-deep flex items-center justify-center text-6xl">♪</div>}
          <div className="cover-glass-reflection"/>
          {isLoading&&<div className="absolute inset-0 z-10 bg-black/20 backdrop-blur-sm flex items-center justify-center"><div className="w-10 h-10 border-2 border-white/25 border-t-white rounded-full animate-spin"/></div>}
        </motion.div>
        <Equalizer height={38} className="w-full max-w-[270px] opacity-80"/>
      </div>

      <div className="relative z-10 px-6 pb-5 space-y-4">
        <div className="flex items-end gap-3"><div className="flex-1 min-w-0"><h1 className="text-[26px] leading-tight font-bold tracking-tight truncate">{currentSong.title}</h1><p className="text-base text-white/55 truncate">{currentSong.artist}</p></div><button className="text-white/80 text-3xl leading-none">♡</button></div>
        <div className="space-y-1.5"><div ref={progressRef} onClick={handleProgressClick} className="progress-track player-progress"><div className="progress-fill" style={{width:`${progress*100}%`}}/></div><div className="flex justify-between text-[11px] text-white/45 tabular-nums"><span>{formatTime(currentTime)}</span><span>{formatTime(duration)}</span></div></div>
        <div className="flex items-center justify-center gap-6">
          <button onClick={toggleShuffle} className={`p-2 pressable ${isShuffle?'text-purple-mid':'text-white/55'}`}><Shuffle size={20}/></button>
          <button onClick={prev} className="p-2 pressable"><SkipBack size={27} fill="white"/></button>
          <motion.button whileTap={{scale:.9}} onClick={togglePlay} className="w-[70px] h-[70px] rounded-full play-orb flex items-center justify-center">{isPlaying?<Pause size={29} fill="black"/>:<Play size={29} fill="black" className="ml-1"/>}</motion.button>
          <button onClick={next} className="p-2 pressable"><SkipForward size={27} fill="white"/></button>
          <button onClick={cycleRepeat} className={`p-2 pressable ${repeatMode!=='off'?'text-purple-mid':'text-white/55'}`}>{repeatMode==='one'?<Repeat1 size={20}/>:<Repeat size={20}/>}</button>
        </div>
        <div className="flex items-center justify-between px-1"><button onClick={()=>setShowLyrics(v=>!v)} className={`flex items-center gap-1.5 text-sm pressable ${showLyrics?'text-purple-mid':'text-white/60'}`}><Mic2 size={18}/>Letras</button><button onClick={handleDownload} disabled={isDownloaded||downloading} className={`flex items-center gap-1.5 text-sm pressable ${isDownloaded?'text-green-400':'text-white/60'}`}><Download size={18}/>{isDownloaded?'Offline':downloading?'...':'Descargar'}</button><button className="text-white/60 pressable"><Share2 size={18}/></button></div>
      </div>

      <AnimatePresence>{showLyrics&&<motion.div initial={{y:'100%',opacity:0}} animate={{y:0,opacity:1}} exit={{y:'100%',opacity:0}} transition={{type:'spring',stiffness:300,damping:28}} className="absolute inset-x-0 bottom-0 z-30 h-[56%] lyrics-sheet rounded-t-[34px] overflow-hidden"><div className="w-12 h-1.5 rounded-full bg-white/20 mx-auto mt-3"/><div ref={lyricsRef} className="h-full overflow-y-auto px-6 pt-6 pb-14 space-y-2">
        {lyrics.length===0?<p className="text-center text-white/40 py-10">No hay letras disponibles</p>:lyrics.map((line,i)=>{const next=lyrics[i+1];const active=currentTime>=line.time&&(!next||currentTime<next.time);const passed=currentTime>(next?.time??Infinity);const lineDuration=Math.max(.35,(next?.time??(line.time+4))-line.time);const raw=Math.max(0,Math.min(1,(currentTime-line.time)/lineDuration));const charProgress=active?Math.min(1,raw*1.2):passed?1:0;const chars=line.text.split('');const mood=line.text.includes('!')?'energy':line.text.includes('?')?'dream':line.text.includes('♥')||line.text.includes('love')||line.text.includes('amor')?'love':'neutral';return <motion.button key={i} onClick={()=>seek(line.time)} className={`w-full text-left lyric-line mood-${mood} ${active?'active':''} ${passed?'passed':''}`} animate={{scale:active?1.035:1,opacity:active?1:passed?.38:.58}} transition={{type:'spring',stiffness:300,damping:24}}><span className="lyric-text">{chars.map((c,ci)=><span key={ci} className={ci/Math.max(1,chars.length)<=charProgress?'lyric-char-on':'lyric-char-off'} style={{transitionDelay:`${Math.min(80,ci*4)}ms`}}>{c}</span>)}</span></motion.button>})}
      </div></motion.div>}</AnimatePresence>
    </motion.div>}
  </AnimatePresence>
}
