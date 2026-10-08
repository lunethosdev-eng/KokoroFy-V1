import { useEffect, useState } from 'react'

export function useDeviceTilt(enabled = true) {
  const [tilt, setTilt] = useState({ x: 0, y: 0 })

  useEffect(() => {
    if (!enabled || typeof window === 'undefined' || !('DeviceOrientationEvent' in window)) return
    let last = { x: 0, y: 0 }
    const onOrientation = (event: DeviceOrientationEvent) => {
      const gamma = Math.max(-30, Math.min(30, event.gamma ?? 0))
      const beta = Math.max(-30, Math.min(30, (event.beta ?? 0) - 45))
      const next = { x: beta * 0.22, y: gamma * 0.22 }
      last = { x: last.x * 0.8 + next.x * 0.2, y: last.y * 0.8 + next.y * 0.2 }
      setTilt(last)
    }
    window.addEventListener('deviceorientation', onOrientation, true)
    return () => window.removeEventListener('deviceorientation', onOrientation, true)
  }, [enabled])

  return tilt
}
