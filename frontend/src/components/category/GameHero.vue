<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getFeatured } from '../../api/item'
import { getMeta } from '../../constants/types'
import TypeIcon from '../TypeIcon.vue'

const router = useRouter()
const meta = getMeta('game')
const games = ref([])

const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 640)
const STEP = computed(() => isMobile.value ? 304 : 344) // width 280+24(gap) vs 320+24(gap)

function onResize() { windowWidth.value = window.innerWidth }

const realCount = computed(() => games.value.length)
const activeIdx = ref(0)
const animating = ref(true)

const displayCards = computed(() => {
  const g = games.value; if (!g.length) return []; return [...g, ...g, ...g]
})

let autoplayTimer = null
function startAutoplay() { 
  clearInterval(autoplayTimer)
  autoplayTimer = setInterval(() => go(1), 3500) 
}

onMounted(async () => {
  window.addEventListener('resize', onResize)
  try { games.value = await getFeatured({ type: 'game' }) || [] } catch {}
  if (games.value.length) { activeIdx.value = realCount.value; startAutoplay() }
})
onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  clearInterval(autoplayTimer)
})

function go(dir) {
  const n = realCount.value; if (!n) return; activeIdx.value += dir
  if (dir > 0 && activeIdx.value >= n * 2) { 
    setTimeout(() => { animating.value=false; activeIdx.value-=n; requestAnimationFrame(()=>{animating.value=true}) },620) 
  } else if (dir < 0 && activeIdx.value < n) { 
    setTimeout(() => { animating.value=false; activeIdx.value+=n; requestAnimationFrame(()=>{animating.value=true}) },620) 
  }
}
function select(i) { activeIdx.value = realCount.value + i; startAutoplay() }
function goDetail(slug) { router.push({ name:'Detail', params:{slug} }) }

function hasDemo(item) {
  try { return !!JSON.parse(item.infoJson || '{}').demo_url } catch { return false }
}

const currentDrag = ref(0)
let touchStartX = 0
let touchStartY = 0
let isDragging = false

function onTouchStart(e) {
  if (e.touches.length !== 1) return
  touchStartX = e.touches[0].clientX
  touchStartY = e.touches[0].clientY
  isDragging = true
  clearInterval(autoplayTimer)
  animating.value = false
}

function onTouchMove(e) {
  if (!isDragging) return
  const dx = e.touches[0].clientX - touchStartX
  const dy = e.touches[0].clientY - touchStartY
  if (Math.abs(dy) > Math.abs(dx) && Math.abs(currentDrag.value) < 10) {
    isDragging = false
    animating.value = true
    return
  }
  if (e.cancelable) e.preventDefault()
  currentDrag.value = dx
}

function onTouchEnd() {
  if (!isDragging) return
  isDragging = false
  animating.value = true
  
  if (currentDrag.value < -40) go(1)
  else if (currentDrag.value > 40) go(-1)
  
  currentDrag.value = 0
  startAutoplay()
}

const trackStyle = computed(() => {
  const baseOffset = -(activeIdx.value * STEP.value) + currentDrag.value;
  return {
    transform: `translateX(${baseOffset}px)`,
    transition: animating.value ? 'transform 600ms cubic-bezier(0.25,0.46,0.45,0.94)' : 'none',
  }
})
const realIdx = computed(() => activeIdx.value % realCount.value)
</script>

<template>
  <div v-if="games.length === 0" class="relative w-full overflow-hidden" style="background: radial-gradient(ellipse at 30% 20%, #064e3b 0%, #022c22 30%, #0a0a0a 70%)">
    <div class="flex flex-col items-center justify-center py-28 gap-3">
      <span class="text-gray-500 text-3xl"><TypeIcon type="game" size="40" /></span>
      <span class="text-gray-400 text-sm">管理员尚未配置轮播作品</span>
    </div>
  </div>
  <div v-else class="relative w-full overflow-hidden" style="background: radial-gradient(ellipse at 30% 20%, #064e3b 0%, #022c22 30%, #0a0a0a 70%)">
    <div class="absolute inset-0 opacity-[0.03] pointer-events-none" style="background-image: repeating-linear-gradient(0deg, transparent, transparent 2px, #34d399 2px, #34d399 4px), repeating-linear-gradient(90deg, transparent, transparent 2px, #34d399 2px, #34d399 4px)" />

    <div class="relative max-w-[90%] mx-auto px-6 pt-10 pb-8">
      <div class="flex items-end justify-between mb-8">
        <div>
          <div class="text-[10px] tracking-[0.3em] text-emerald-500/80 uppercase mb-2 font-mono">Featured Games</div>
          <h2 class="text-4xl sm:text-5xl font-black tracking-tighter">
            <span class="text-emerald-400"><TypeIcon type="game" size="36" /></span>
            <span class="bg-gradient-to-r from-emerald-400 via-emerald-300 to-cyan-400 bg-clip-text text-transparent"> {{ meta.label }}专区</span>
          </h2>
        </div>
        <div class="hidden sm:flex gap-1.5">
          <button v-for="i in realCount" :key="'dot-'+i" @click="select(i-1)"
            :class="realIdx===i-1 ? 'bg-emerald-400 shadow-lg shadow-emerald-500/50 w-12' : 'bg-white/20 hover:bg-white/40 w-6'"
            class="h-1 rounded-full transition-all duration-500" />
        </div>
      </div>

      <div class="overflow-hidden -mx-3 px-3" @touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd" @touchcancel="onTouchEnd">
        <div class="flex gap-6 py-1" :style="trackStyle">
          <div v-for="(g, i) in displayCards" :key="g.id+'-'+i"
            class="shrink-0 w-[280px] sm:w-[320px] cursor-pointer group"
            @click="goDetail(g.slug)">
            <div class="relative w-full aspect-[21/10] rounded-2xl ring-1 ring-emerald-900/30 group-hover:ring-emerald-400/60 group-hover:shadow-xl group-hover:shadow-emerald-500/10 transition-all duration-500">
              <div class="absolute inset-0 rounded-2xl overflow-hidden">
                <div v-if="g.coverUrl"
                  class="absolute inset-0 bg-cover bg-top group-hover:scale-110 transition-transform duration-700"
                  :style="{ backgroundImage: 'url('+g.coverUrl+')' }" />
                <div v-else class="absolute inset-0 bg-gradient-to-br from-emerald-900 via-gray-900 to-black flex items-center justify-center"><TypeIcon type="game" size="40" /></div>
              </div>
              <div class="absolute inset-x-0 bottom-0 h-[60%] bg-gradient-to-t from-black/95 via-black/60 to-transparent rounded-b-2xl pointer-events-none" />
              <div class="absolute bottom-0 left-0 right-0 p-4 pb-5">
                <span class="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 mb-1.5 inline-block font-medium">GAME</span>
                <h3 class="font-extrabold text-base sm:text-lg leading-snug text-white drop-shadow-md group-hover:text-emerald-300 transition-colors line-clamp-2">{{ g.title }}</h3>
              </div>
              <div class="absolute top-3 left-3 z-10">
                <span v-if="hasDemo(g)" class="text-[9px] px-2 py-0.5 rounded-full bg-gradient-to-r from-emerald-400 via-cyan-400 to-blue-500 text-white font-black tracking-wider shadow-lg shadow-cyan-500/30 animate-pulse">DEMO</span>
              </div>
              <div class="absolute top-3 right-3 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                <span class="text-[10px] px-2 py-0.5 rounded-full bg-black/60 text-emerald-400 border border-emerald-600/40 backdrop-blur-sm">查看 →</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
