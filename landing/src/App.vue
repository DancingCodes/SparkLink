<template>
  <div class="h-screen bg-#F7F9FB flex flex-col items-center justify-center">
    <div v-if="loading" class="flex flex-col items-center gap-4">
      <div class="w-12 h-12 border-4 border-blue-200 border-t-blue-600 rd-full animate-spin"></div>
      <p class="text-slate-400 animate-pulse">努力加载中</p>
    </div>
    <div v-else class="w-full flex flex-col items-center animate-fade-in p-4 box-border">
      <div class="w-24 h-24 rd-2xl shadow-xl shadow-blue-100 mb-6 overflow-hidden border-1 border-white">
        <img :src="appData.logo" alt="logo" class="w-full h-full object-cover" />
      </div>
      <h1 class="text-2xl font-bold text-slate-800 mb-1">{{ appData.appName }}</h1>
      <div class="flex items-center gap-2 mb-10">
        <span class="text-xs font-mono px-2 py-0.5 bg-slate-200 text-slate-600 rd">
          v{{ appData.version }}
        </span>
      </div>
      <div class="w-full space-y-4">
        <div v-if="device === 'ios'"
          class="group w-full py-4 bg-slate-100 border-2 border-dashed border-slate-200 rd-2xl flex flex-col items-center justify-center gap-1 cursor-not-allowed">
          <div class="flex items-center gap-2 text-slate-400 font-semibold">
            <div class="i-mingcute:apple-line text-xl"></div>
            <span>iOS 版本暂未兼容</span>
          </div>
          <span class="text-10px text-slate-300">敬请期待</span>
        </div>
        <a v-else-if="device === 'android'" :href="appData.downloadUrl"
          class="w-full py-4 bg-blue-600 text-white rd-2xl font-bold flex items-center justify-center gap-2 shadow-lg shadow-blue-200 active:scale-95 transition-transform">
          <div class="i-mingcute:android-fill text-xl"></div>
          立即下载 APK
        </a>
      </div>
    </div>
    <div v-if="isWechat" class="fixed inset-0 bg-black/90 z-100 p-10 text-white flex flex-col items-end"
      @click="isWechat = false">
      <div class="text-4xl mb-4 animate-bounce">↗</div>
      <p class="text-xl font-bold mb-2">在微信中无法直接下载</p>
      <p class="opacity-70 text-right">请点击右上角三个点<br />选择 “在浏览器中打开”</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface AppData {
  appName: string
  logo: string
  version: string
  downloadUrl: string
}

const loading = ref(true)
const device = ref<'ios' | 'android'>()
const isWechat = ref(false)
const appData = ref<AppData>({
  appName: '',
  logo: '',
  version: '',
  downloadUrl: ''
})


const getRemoteConfig = (): Promise<AppData> => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        appName: 'SparkLink',
        logo: 'https://api.dicebear.com/7.x/shapes/svg?seed=Spark',
        version: '1.2.4',
        downloadUrl: 'https://example.com/files/sparklink_v1.2.4.apk'
      })
    }, 1500)
  })
}


const detectDevice = () => {
  const ua = navigator.userAgent.toLowerCase()
  isWechat.value = /micromessenger/.test(ua)
  if (/iphone|ipad|ipod/.test(ua)) {
    device.value = 'ios'
  } else if (/android/.test(ua)) {
    device.value = 'android'
  }
}

onMounted(async () => {
  detectDevice()
  try {
    const res = await getRemoteConfig()
    appData.value = res
  } finally {
    loading.value = false
  }
})
</script>