import { useEffect, useState } from 'react'
import { useAudioEngine } from '../hooks/useAudioEngine'
import { usePlayerStore } from '../hooks/usePlayerStore'

interface EqualizerProps {
  bars?: number
  height?: number
  className?: string
}

export function Equalizer({ bars = 16, height = 40, className = '' }: EqualizerProps) {
  const { getEQData } = useAudioEngine()
  const isPlaying = usePlayerStore(s => s.isPlaying)
  const [levels, setLevels] = useState<number[]>(Array(bars).fill(0))

  useEffect(() => {
    if (!isPlaying) {
      setLevels(Array(bars).fill(4))
      return
    }
    let raf: number
    const tick = () => {
      const data = getEQData()
      // normalize 0-255 → 8%-100%
      setLevels(data.map(v => Math.max(8, (v / 255) * 100)))
      raf = requestAnimationFrame(tick)
    }
    raf = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(raf)
  }, [isPlaying, getEQData, bars])

  return (
    <div
      className={`flex items-end justify-center gap-[3px] ${className}`}
      style={{ height }}
    >
      {levels.map((h, i) => (
        <div
          key={i}
          className="eq-bar"
          style={{
            height: `${h}%`,
            opacity: 0.7 + (h / 100) * 0.3
          }}
        />
      ))}
    </div>
  )
}
