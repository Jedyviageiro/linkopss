import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    host: '0.0.0.0',
    watch: {
      // Docker Desktop can miss filesystem events from Windows/OneDrive bind mounts.
      usePolling: true,
      interval: 250,
    },
    hmr: {
      host: process.env.VITE_HMR_CLIENT_HOST,
      clientPort: Number(process.env.VITE_HMR_CLIENT_PORT ?? 5173),
    },
  },
})
