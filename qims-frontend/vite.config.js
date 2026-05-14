import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 3000,
    open: false,
    proxy: {
      // 本地开发代理设置，将 /api 的请求转发给 Spring Boot
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
        // 由于 Spring Boot 端也是 /api 开头，所以不需要重写路径
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
