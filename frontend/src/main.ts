import { createApp } from 'vue'
import App from './App.vue'
import { pinia } from '@/app/pinia'
import { router } from '@/app/router'
import '@fontsource/inter/latin-400.css'
import '@fontsource/inter/latin-500.css'
import '@fontsource/inter/latin-600.css'
import '@fontsource/inter/latin-700.css'
import '@/assets/styles/main.css'

createApp(App).use(pinia).use(router).mount('#app')
