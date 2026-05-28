<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getFeatured, getHotItems } from '../../api/item'
import { getMeta } from '../../constants/types'

const router = useRouter()
const meta = getMeta('digital')
const items = ref([])
const activeIdx = ref(0)
let autoTimer = null

onMounted(async () => {
  try {
    const featured = await getFeatured({ type: 'digital' }) || []
    items.value = featured.length ? featured : (await getHotItems({ type: 'digital', limit: 6 })) || []
  } catch {}
  if (items.value.length > 1) startAutoPlay()
})

onUnmounted(() => clearInterval(autoTimer))

function startAutoPlay() {
  clearInterval(autoTimer)
  autoTimer = setInterval(() => { activeIdx.value = (activeIdx.value + 1) % items.value.length }, 5000)
}
function resetAutoPlay() {
  clearInterval(autoTimer)
  startAutoPlay()
}

function parseInfo(item) {
  try { return JSON.parse(item.infoJson || '{}') } catch { return {} }
}

const activeItem = computed(() => items.value[activeIdx.value] || {})
const activeInfo = computed(() => parseInfo(activeItem.value))
const activeFeatures = computed(() => {
  const raw = activeInfo.value.features || ''
  if (!raw) return []
  return raw.split(',').map(f => f.trim()).filter(Boolean)
})

function prev() { activeIdx.value = (activeIdx.value - 1 + items.value.length) % items.value.length; resetAutoPlay() }
function next() { activeIdx.value = (activeIdx.value + 1) % items.value.length; resetAutoPlay() }
function goTo(i) { activeIdx.value = i; resetAutoPlay() }
function goDetail() { router.push({ name: 'Detail', params: { slug: activeItem.value.slug } }) }
</script>

<template>
  <div v-if="items.length" class="relative overflow-hidden bg-gradient-to-br from-cyan-950 via-slate-950 to-gray-950 border-b border-cyan-900/30">
    <div class="max-w-5xl mx-auto px-6 py-12">
      <!-- Label -->
      <div class="flex items-center gap-3 mb-8">
        <span class="w-8 h-[2px] bg-cyan-400/40" />
        <span class="text-cyan-400/60 text-xs tracking-[0.25em] uppercase font-mono">今日精选</span>
        <span class="flex-1" />
        <span class="text-xs text-gray-600">{{ meta.emoji }} {{ meta.label }}</span>
      </div>

      <div class="flex flex-col items-center">
        <!-- Product image with transition -->
        <div class="relative w-full max-w-2xl mb-8 cursor-pointer" @click="goDetail">
          <Transition name="fade" mode="out-in">
            <div :key="activeItem.id" class="aspect-[16/9] rounded-2xl overflow-hidden bg-gray-900 flex items-center justify-center">
              <img v-if="activeItem.wideCoverUrl || activeItem.coverUrl"
                :src="activeItem.wideCoverUrl || activeItem.coverUrl"
                class="w-full h-full object-contain p-4 hover:scale-[1.02] transition-transform duration-700" />
              <span v-else class="text-6xl text-gray-700">📱</span>
            </div>
          </Transition>
        </div>

        <!-- Info bar: frosted glass dark -->
        <div class="w-full max-w-2xl bg-cyan-950/40 backdrop-blur-md rounded-2xl border border-cyan-500/10 px-6 py-4 text-center shadow-lg shadow-cyan-900/10">
          <h2 class="text-xl font-bold text-white mb-1">{{ activeItem.title }}</h2>
          <p class="text-sm text-gray-400">
            {{ activeInfo.brand }}<span v-if="activeInfo.year"> · {{ activeInfo.year }}</span>
            <span v-if="activeInfo.category"> · {{ activeInfo.category }}</span>
          </p>
          <div v-if="activeFeatures.length" class="flex flex-wrap justify-center gap-1.5 mt-3">
            <span v-for="f in activeFeatures" :key="f" class="text-[11px] px-2.5 py-0.5 rounded-full bg-cyan-500/10 text-cyan-300 border border-cyan-500/20">{{ f }}</span>
          </div>
        </div>

        <!-- Controls -->
        <div class="flex items-center gap-6 mt-6">
          <button @click="prev" class="w-10 h-10 rounded-full flex items-center justify-center border border-gray-700 bg-gray-800 text-gray-400 hover:text-cyan-400 hover:border-cyan-500/50 transition-colors">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
          </button>
          <div class="flex gap-2">
            <button v-for="(_, i) in items" :key="i" @click="goTo(i)"
              :class="i === activeIdx ? 'w-7 h-1.5 bg-cyan-400' : 'w-1.5 h-1.5 bg-gray-600 hover:bg-gray-500'"
              class="rounded-full transition-all duration-300" />
          </div>
          <button @click="next" class="w-10 h-10 rounded-full flex items-center justify-center border border-gray-700 bg-gray-800 text-gray-400 hover:text-cyan-400 hover:border-cyan-500/50 transition-colors">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
          </button>
        </div>
      </div>
    </div>
  </div>
  <div v-else />
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.5s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
