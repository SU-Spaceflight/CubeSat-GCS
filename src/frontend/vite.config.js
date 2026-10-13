import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

const envDir = '../..'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, envDir, '')

  return {
    plugins: [react()],
    envDir,
    server: {
      port: Number(env.VITE_PORT),
      proxy: { '/api': `http://localhost:${env.SPRING_PORT}` },
    },
    build: {
      outDir: '../../target/classes/static',
      emptyOutDir: true,
    },
  }
})