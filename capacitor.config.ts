import type { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  appId: 'com.kokorofy.music',
  appName: 'KokoroFy',
  webDir: 'dist',
  server: {
    androidScheme: 'https'
  },
  plugins: {
    SplashScreen: {
      launchShowDuration: 0 // usamos nuestro bootloader de video
    },
    StatusBar: {
      style: 'DARK',
      backgroundColor: '#1a0a2e'
    }
  }
}

export default config
