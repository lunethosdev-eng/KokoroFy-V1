import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './styles/index.css'

// Hide bootloader after video ends or timeout
const boot = document.getElementById('bootloader')
const video = document.getElementById('boot-video') as HTMLVideoElement

function hideBoot() {
  if (boot) {
    boot.classList.add('hidden')
    setTimeout(() => boot.remove(), 600)
  }
}

if (video) {
  video.addEventListener('ended', hideBoot)
  // Fallback timeout
  setTimeout(hideBoot, 3500)
} else {
  setTimeout(hideBoot, 1500)
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)
