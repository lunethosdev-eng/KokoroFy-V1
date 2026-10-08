import { PropsWithChildren, useRef, useState } from 'react'
import { motion, useMotionValue, useSpring, useTransform } from 'framer-motion'

export function LiquidGlass({ children, className = '', onClick, drag = false }: PropsWithChildren<{ className?: string; onClick?: () => void; drag?: boolean }>) {
  const ref = useRef<HTMLDivElement>(null)
  const [pressed, setPressed] = useState(false)
  const x = useMotionValue(0)
  const y = useMotionValue(0)
  const sx = useSpring(x, { stiffness: 520, damping: 28, mass: 0.48 })
  const sy = useSpring(y, { stiffness: 520, damping: 28, mass: 0.48 })
  const scaleX = useTransform(sx, [-32, 0, 32], [1.08, 1, 1.08])
  const scaleY = useTransform(sy, [-32, 0, 32], [1.08, 1, 1.08])

  return (
    <motion.div
      ref={ref}
      className={`liquid-surface ${className}`}
      onClick={onClick}
      onPointerDown={() => setPressed(true)}
      onPointerUp={() => setPressed(false)}
      onPointerCancel={() => setPressed(false)}
      drag={drag}
      dragConstraints={{ left: -32, right: 32, top: -32, bottom: 32 }}
      dragElastic={0.72}
      dragMomentum={false}
      onDrag={(e, info) => { x.set(info.offset.x * 0.22); y.set(info.offset.y * 0.22) }}
      onDragEnd={() => { x.set(0); y.set(0); setPressed(false) }}
      style={{ x: sx, y: sy, scaleX: pressed ? scaleX : 1, scaleY: pressed ? scaleY : 1 }}
      whileTap={{ scale: 0.985 }}
    >
      <span className="liquid-highlight" />
      <span className="liquid-specular" />
      <span className="liquid-noise" />
      <span className="relative z-[1]">{children}</span>
    </motion.div>
  )
}
