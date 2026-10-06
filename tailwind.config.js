/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        purple: {
          soft: '#e9d5ff',
          mid: '#c4b5fd',
          deep: '#7c3aed',
          dark: '#1a0a2e',
          glass: 'rgba(196, 181, 253, 0.15)'
        }
      },
      fontFamily: {
        display: ['SF Pro Display', 'Inter', 'system-ui', 'sans-serif'],
        body: ['SF Pro Text', 'Inter', 'system-ui', 'sans-serif']
      },
      backdropBlur: {
        glass: '24px'
      },
      animation: {
        'equalizer': 'equalizer 0.8s ease-in-out infinite',
        'pulse-soft': 'pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'boot-fade': 'bootFade 0.6s ease-out forwards'
      },
      keyframes: {
        equalizer: {
          '0%, 100%': { height: '20%' },
          '50%': { height: '100%' }
        },
        bootFade: {
          '0%': { opacity: '0', transform: 'scale(0.9)' },
          '100%': { opacity: '1', transform: 'scale(1)' }
        }
      }
    }
  },
  plugins: []
}
