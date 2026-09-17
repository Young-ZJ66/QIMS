import { createApp } from 'vue'
import App from './App.vue'

// 引入路由
import router from './router'

// 引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

// 引入全部图标（Element Plus 图标按名引用，无法按需导入）
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

// 全局错误处理
app.config.errorHandler = (err, instance, info) => {
  console.error('[Vue Error]', err, info)
  // 避免白屏，显示友好提示
  if (err?.message?.includes('Cannot read properties of null')) {
    // 常见的 null 引用错误，不阻断用户
    return
  }
}

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')