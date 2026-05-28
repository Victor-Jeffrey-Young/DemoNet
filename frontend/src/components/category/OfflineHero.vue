<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getFeatured, getHotItems } from '../../api/item'
import { getMeta } from '../../constants/types'

const router = useRouter()
const meta = getMeta('offline')
const items = ref([])
const activeIdx = ref(0)
let autoTimer = null

onMounted(async () => {
  try {
    const featured = await getFeatured({ type: 'offline' }) || []
    items.value = featured.length ? featured : (await getHotItems({ type: 'offline', limit: 6 })) || []
  } catch {}
  if (items.value.length > 1) startAutoPlay()
})

onUnmounted(() => clearInterval(autoTimer))

function startAutoPlay() {
  clearInterval(autoTimer)
  autoTimer = setInterval(() => { activeIdx.value = (activeIdx.value + 1) % items.value.length }, 6000)
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
const activeHighlights = computed(() => {
  const raw = activeInfo.value.highlights || ''
  if (!raw) return []
  return raw.split(',').map(h => h.trim()).filter(Boolean)
})

const typeLabels = {
  '密室逃脱':'🔐','沉浸式剧场':'🎭','展览':'🖼️','市集':'🛍️',
  'Live':'🎸','工作坊':'🛠️','城市探索':'🗺️'
}

function prev() { activeIdx.value = (activeIdx.value - 1 + items.value.length) % items.value.length; resetAutoPlay() }
function next() { activeIdx.value = (activeIdx.value + 1) % items.value.length; resetAutoPlay() }
function goTo(i) { activeIdx.value = i; resetAutoPlay() }
function goDetail() { router.push({ name: 'Detail', params: { slug: activeItem.value.slug } }) }
</script>

<template>
  <div v-if="items.length" class="relative overflow-hidden border-b-4 border-amber-800/60" style="background: linear-gradient(175deg, #c4a47a 0%, #b8956a 30%, #a08060 100%)">
    <!-- Cork texture overlay -->
    <div class="absolute inset-0 opacity-30 pointer-events-none" style="background: radial-gradient(circle at 10% 20%, rgba(0,0,0,0.1) 1px, transparent 1px); background-size: 8px 8px" />
    <div class="absolute inset-0 opacity-10 pointer-events-none" style="background: radial-gradient(circle at 60% 70%, rgba(0,0,0,0.15) 1px, transparent 1px); background-size: 5px 5px" />

    <div class="max-w-4xl mx-auto px-6 py-12 relative z-10">
      <!-- Header: pinned label -->
      <div class="flex items-center justify-center mb-8">
        <div class="bg-red-500 w-4 h-4 rounded-full shadow-md mr-3" style="box-shadow: 1px 1px 3px rgba(0,0,0,0.4)" />
        <span class="text-amber-900/70 text-sm font-mono tracking-widest">📌 本周精选</span>
        <div class="bg-red-500 w-4 h-4 rounded-full shadow-md ml-3" style="box-shadow: 1px 1px 3px rgba(0,0,0,0.4)" />
      </div>

      <div class="flex flex-col items-center">
        <!-- Poster pinned to cork board -->
        <div class="relative cursor-pointer" @click="goDetail">
          <div class="absolute -top-2 left-1/2 -translate-x-1/2 z-20">
            <div class="w-3 h-3 bg-red-500 rounded-full shadow-lg" style="box-shadow: 0 2px 4px rgba(0,0,0,0.5)" />
          </div>
          <Transition name="cork-fade" mode="out-in">
            <div :key="activeItem.id" class="w-72 bg-white rounded shadow-xl overflow-hidden hover:scale-[1.02] transition-transform duration-500" style="box-shadow: 3px 4px 12px rgba(0,0,0,0.3), -1px -1px 0 rgba(255,255,255,0.1) inset">
              <img v-if="activeItem.wideCoverUrl || activeItem.coverUrl"
                :src="activeItem.wideCoverUrl || activeItem.coverUrl"
                class="w-full aspect-[4/3] object-cover" />
              <div v-else class="w-full aspect-[4/3] bg-gradient-to-br from-indigo-800 to-slate-700 flex items-center justify-center text-4xl">🏛️</div>
            </div>
          </Transition>
        </div>

        <!-- Info memo note -->
        <div class="mt-6 w-full max-w-md bg-yellow-50/90 rounded shadow-md p-4 transform -rotate-1" style="box-shadow: 2px 3px 8px rgba(0,0,0,0.2)">
          <h2 class="text-lg font-bold text-gray-800 mb-2">{{ activeItem.title }}</h2>
          <div class="space-y-1 text-xs text-gray-600">
            <p v-if="activeInfo.event_type" class="flex items-center gap-1.5">
              <span>{{ typeLabels[activeInfo.event_type] || '🏛️' }}</span> {{ activeInfo.event_type }}
            </p>
            <p v-if="activeInfo.date" class="flex items-center gap-1.5"><span>📅</span> {{ activeInfo.date }}</p>
            <p v-if="activeInfo.time" class="flex items-center gap-1.5"><span>🕐</span> {{ activeInfo.time }}</p>
            <p v-if="activeInfo.venue" class="flex items-center gap-1.5"><span>📍</span> {{ activeInfo.venue }}</p>
            <p v-if="activeInfo.price" class="flex items-center gap-1.5"><span>💰</span> {{ activeInfo.price }}</p>
          </div>
          <div v-if="activeHighlights.length" class="flex flex-wrap gap-1.5 mt-3">
            <span v-for="h in activeHighlights" :key="h" class="text-[10px] px-2 py-0.5 rounded-full bg-indigo-100 text-indigo-700">{{ h }}</span>
          </div>
        </div>

        <!-- Controls -->
        <div class="flex items-center gap-5 mt-8">
          <button @click="prev" class="w-9 h-9 rounded-full flex items-center justify-center border border-amber-700/40 text-amber-800/60 hover:text-amber-900 hover:border-amber-800/60 transition-colors bg-amber-200/30">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
          </button>
          <div class="flex gap-2.5">
            <button v-for="(_, i) in items" :key="i" @click="goTo(i)"
              :class="i === activeIdx ? 'w-6 h-2.5 bg-indigo-600' : 'w-2.5 h-2.5 bg-amber-700/30 hover:bg-amber-700/50'"
              class="rounded-full transition-all duration-300" />
          </div>
          <button @click="next" class="w-9 h-9 rounded-full flex items-center justify-center border border-amber-700/40 text-amber-800/60 hover:text-amber-900 hover:border-amber-800/60 transition-colors bg-amber-200/30">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
          </button>
        </div>

        <!-- Bottom cork pins -->
        <div class="flex items-center gap-12 mt-6 opacity-30">
          <div class="w-2.5 h-2.5 bg-red-400 rounded-full shadow" />
          <div class="w-2.5 h-2.5 bg-blue-400 rounded-full shadow" />
          <div class="w-2.5 h-2.5 bg-green-400 rounded-full shadow" />
        </div>
      </div>
    </div>
  </div>
  <div v-else />
</template>

<style scoped>
.cork-fade-enter-active, .cork-fade-leave-active { transition: opacity 0.6s ease; }
.cork-fade-enter-from, .cork-fade-leave-to { opacity: 0; }
</style>
