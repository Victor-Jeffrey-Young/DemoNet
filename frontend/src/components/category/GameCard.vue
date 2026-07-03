<script setup>
import { useRouter } from 'vue-router'
import { computed } from 'vue'

import TypeIcon from '../TypeIcon.vue'

const props = defineProps({ item: { type: Object, required: true } })
const router = useRouter()

const hasDemo = computed(() => {
  try { return JSON.parse(props.item.infoJson || '{}').demo_available } catch { return false }
})
const isDlc = computed(() => {
  try { return !!JSON.parse(props.item.infoJson || '{}').is_dlc } catch { return false }
})

function go() { router.push({ name:'Detail', params:{ slug: props.item.slug } }) }
function tryDemo(e) {
  e.stopPropagation()
  if (demoUrl.value) window.open(demoUrl.value, '_blank')
}

const demoUrl = computed(() => {
  try { return JSON.parse(props.item.infoJson || '{}').demo_url } catch { return null }
})
</script>

<template>
  <div @click="go"
    class="game-card-hover relative rounded-2xl overflow-hidden aspect-[3/4] cursor-pointer group ring-1 ring-emerald-900/20 hover:ring-emerald-400/50 shadow-lg shadow-black/40 hover:shadow-xl hover:shadow-emerald-500/10 transition-all duration-500">
    <div v-if="item.posterUrl || item.coverUrl"
      class="card-bg absolute inset-0 bg-cover bg-top transition-transform duration-700"
      :style="{ backgroundImage: 'url(' + (item.posterUrl || item.coverUrl) + ')' }" />
    <div v-else class="card-bg absolute inset-0 bg-gradient-to-br from-emerald-950 via-gray-950 to-black flex items-center justify-center transition-transform duration-700"><TypeIcon type="game" size="40" /></div>

    <div v-if="hasDemo" class="absolute top-3 left-3 z-10">
      <span class="text-[9px] px-2 py-0.5 rounded-full bg-gradient-to-r from-emerald-400 via-cyan-400 to-blue-500 text-white font-black tracking-wider shadow-lg shadow-cyan-500/30 animate-pulse">DEMO</span>
    </div>
    <div v-if="isDlc" class="absolute top-3 z-10" :class="hasDemo ? 'left-16' : 'left-3'">
      <span class="text-[9px] px-2 py-0.5 rounded-full bg-amber-500/80 text-white font-black tracking-wider shadow-lg shadow-amber-500/30">DLC</span>
    </div>

    <div class="absolute inset-0 bg-gradient-to-t from-black/95 via-black/40 to-transparent" />

    <div class="card-content absolute bottom-0 left-0 right-0 px-5 pt-2 pb-10">
      <span class="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/15 text-emerald-300 border border-emerald-600/30 mb-1 inline-block font-medium">GAME</span>
      <h3 class="text-lg font-extrabold leading-tight text-white mb-1 group-hover:text-emerald-300 transition-colors line-clamp-2">{{ item.title }}</h3>
      <p class="card-desc text-gray-400 text-sm leading-relaxed line-clamp-3 mb-2">{{ item.description }}</p>
      <div class="card-btn flex gap-2">
        <button class="text-xs px-5 py-2.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white transition-colors font-semibold shadow-lg shadow-emerald-900/30">
          查看详情 →
        </button>
        <button v-if="hasDemo" @click="tryDemo"
          class="text-xs px-4 py-2.5 rounded-lg bg-emerald-500/20 border border-emerald-500/40 hover:bg-emerald-500 hover:text-white text-emerald-300 transition-all font-semibold">
          <TypeIcon type="game" size="14" /> 试玩
        </button>
      </div>
    </div>
  </div>
</template>
