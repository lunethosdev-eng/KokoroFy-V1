import { Home, Search, Library, Radio } from 'lucide-react'

interface BottomNavProps {
  active: string
  onChange: (tab: string) => void
}

const tabs = [
  { id: 'home', label: 'Inicio', icon: Home },
  { id: 'search', label: 'Buscar', icon: Search },
  { id: 'library', label: 'Biblioteca', icon: Library },
  { id: 'radio', label: 'Radio', icon: Radio }
]

export function BottomNav({ active, onChange }: BottomNavProps) {
  return (
    <nav className="fixed bottom-0 left-0 right-0 z-30 safe-bottom">
      <div className="liquid-glass-strong mx-3 mb-3 rounded-2xl flex items-center justify-around py-2 px-1">
        {tabs.map(({ id, label, icon: Icon }) => {
          const isActive = active === id
          return (
            <button
              key={id}
              onClick={() => onChange(id)}
              className={`flex flex-col items-center gap-0.5 px-4 py-1.5 rounded-xl pressable transition-colors ${
                isActive ? 'text-purple-mid' : 'text-white/50'
              }`}
            >
              <Icon size={22} strokeWidth={isActive ? 2.5 : 1.8} />
              <span className="text-[10px] font-medium">{label}</span>
            </button>
          )
        })}
      </div>
    </nav>
  )
}
