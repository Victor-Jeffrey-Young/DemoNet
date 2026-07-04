<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFeatured } from '../../api/item'
import { getMeta } from '../../constants/types'

const router = useRouter()
const meta = getMeta('anime')
const items = ref([])
const active = ref(0)

onMounted(async () => {
  try { items.value = await getFeatured({ type: 'anime' }) || [] } catch {}
  startTimer()
})

onUnmounted(() => clearInterval(autoplayTimer))

let autoplayTimer = null

function startTimer() {
  clearInterval(autoplayTimer)
  autoplayTimer = setInterval(() => {
    if (items.value.length) active.value = (active.value + 1) % items.value.length
  }, 5000)
}

function resetTimer() {
  startTimer()
}

function next() { 
  if (!items.value.length) return
  active.value = (active.value + 1) % items.value.length 
  resetTimer()
}

function prev() {
  if (!items.value.length) return
  active.value = (active.value - 1 + items.value.length) % items.value.length
  resetTimer()
}

function goDetail(slug) { router.push({ name:'Detail', params:{ slug } }) }

// Swipe handling
let touchStartX = 0
let touchStartY = 0

function onTouchStart(e) {
  if (e.touches.length !== 1) return
  touchStartX = e.touches[0].clientX
  touchStartY = e.touches[0].clientY
}

function onTouchEnd(e) {
  if (e.changedTouches.length !== 1) return
  const dx = e.changedTouches[0].clientX - touchStartX
  const dy = e.changedTouches[0].clientY - touchStartY
  
  if (Math.abs(dy) > Math.abs(dx)) return // vertical scroll
  
  if (dx < -40) {
    next()
  } else if (dx > 40) {
    prev()
  }
}
</script>

<template>
  <div class="relative w-full h-[calc(100vh-4rem)] overflow-hidden bg-black" v-if="items.length"
       @touchstart="onTouchStart" @touchend="onTouchEnd">
    <Transition name="hero-crossfade">
      <div :key="active" class="absolute inset-0 z-0"
           :style="{ backgroundImage: `url(${items[active].wideCoverUrl||items[active].coverUrl||''})`, backgroundSize:'cover', backgroundPosition:'center' }">
        <div class="absolute inset-0 bg-gradient-to-r from-violet-950 via-fuchsia-950/70 sm:via-fuchsia-950/50 to-transparent" />
        <div class="absolute inset-0 bg-gradient-to-t from-gray-950 via-transparent to-transparent" />
      </div>
    </Transition>

    <div class="relative z-10 h-full flex flex-col justify-center px-6 sm:px-12 max-w-4xl">
      <span class="text-violet-400 text-xs sm:text-sm tracking-widest uppercase mb-4">{{ meta.label }}</span>
      <h1 class="text-4xl sm:text-7xl font-black tracking-tighter mb-3 drop-shadow-lg text-white">{{ items[active].title }}</h1>
      <p class="text-gray-300 text-base sm:text-lg max-w-xl mb-8 leading-relaxed line-clamp-3 sm:line-clamp-2">{{ items[active].description }}</p>
      <div class="flex flex-col sm:flex-row gap-3 sm:gap-4">
        <button @click="goDetail(items[active].slug)"
          class="px-6 py-3.5 sm:py-3 bg-violet-600 hover:bg-violet-500 rounded-xl text-sm font-semibold transition-colors shadow-lg shadow-violet-900/30 text-center">
          查看详情
        </button>
      </div>
    </div>

    <div class="absolute bottom-6 left-6 sm:bottom-8 sm:right-8 sm:left-auto z-20 flex items-center gap-3">
      <div class="flex gap-2">
        <button v-for="(m,i) in items" :key="i" @click="active = i; resetTimer()"
          :class="i===active ? 'bg-violet-500 w-8' : 'bg-white/30 w-2'"
          class="h-2 rounded-full transition-all duration-500" />
      </div>
      <button @click="next"
        class="hidden sm:flex w-12 h-12 rounded-full border border-white/20 items-center justify-center hover:bg-white/10 transition group">
        <svg class="w-5 h-5 text-white group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </button>
    </div>
  </div>
  <div v-else class="w-full h-[calc(100vh-4rem)] flex flex-col items-center justify-center bg-gradient-to-b from-violet-950 to-gray-950 gap-3">
    <span class="text-gray-500 text-5xl">🎭</span>
    <span class="text-gray-400 text-sm">管理员尚未配置轮播作品</span>
  </div>
</template>

<style scoped>
.hero-crossfade-enter-active,
.hero-crossfade-leave-active { transition: opacity 1.2s ease-in-out; }
.hero-crossfade-enter-from,
.hero-crossfade-leave-to { opacity: 0; }
</style>
