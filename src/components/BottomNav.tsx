import { Home, Search, Library, Radio } from 'lucide-react'
import { motion } from 'framer-motion'

interface BottomNavProps { active: string; onChange: (tab: string) => void }
const tabs = [
  { id: 'home', label: 'Inicio', icon: Home },
  { id: 'search', label: 'Buscar', icon: Search },
  { id: 'library', label: 'Biblioteca', icon: Library },
  { id: 'radio', label: 'Radio', icon: Radio }
]

export function BottomNav({ active, onChange }: BottomNavProps) {
  return (
    <nav className="fixed bottom-0 left-0 right-0 z-30 safe-bottom pointer-events-none px-3">
      <div className="liquid-nav pointer-events-auto mx-auto max-w-xl mb-3 rounded-[24px] flex items-center justify-around py-2 px-1">
        {tabs.map(({ id, label, icon: Icon }) => {
          const isActive = active === id
          return (
            <button key={id} onClick={() => onChange(id)} className={`relative min-w-0 flex-1 flex flex-col items-center gap-0.5 px-2 py-1.5 rounded-2xl pressable ${isActive ? 'text-white' : 'text-white/50'}`}>
              {isActive && <motion.span layoutId="nav-pill" className="absolute inset-0 rounded-2xl bg-white/10 border border-white/10" transition={{ type: 'spring', stiffness: 500, damping: 32 }} />}
              <span className="relative z-[1]"><Icon size={21} strokeWidth={isActive ? 2.5 : 1.8} /></span>
              <span className="relative z-[1] text-[10px] font-medium truncate max-w-full">{label}</span>
            </button>
          )
        })}
      </div>
    </nav>
  )
}
