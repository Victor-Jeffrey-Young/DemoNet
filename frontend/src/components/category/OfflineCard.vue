<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({ item: { type: Object, required: true } })
const router = useRouter()
function go() { router.push({ name: 'Detail', params: { slug: props.item.slug } }) }

const info = computed(() => {
  try { return typeof props.item.infoJson === 'string' ? JSON.parse(props.item.infoJson) : (props.item.infoJson || {}) } catch { return {} }
})
const eventType = computed(() => info.value.event_type || '')
const venue = computed(() => info.value.venue || '')
const eventDate = computed(() => info.value.date || '')
const highlights = computed(() => {
  const raw = info.value.highlights || ''
  if (!raw) return []
  return raw.split(',').map(h => h.trim()).filter(Boolean)
})

const typeColors = {
  '密室逃脱':'bg-red-500/10 text-red-300 border-red-500/20',
  '沉浸式剧场':'bg-purple-500/10 text-purple-300 border-purple-500/20',
  '展览':'bg-indigo-500/10 text-indigo-300 border-indigo-500/20',
  '市集':'bg-orange-500/10 text-orange-300 border-orange-500/20',
  'Live':'bg-pink-500/10 text-pink-300 border-pink-500/20',
  '工作坊':'bg-emerald-500/10 text-emerald-300 border-emerald-500/20',
  '城市探索':'bg-cyan-500/10 text-cyan-300 border-cyan-500/20',
}
</script>

<template>
  <div @click="go" class="group cursor-pointer flex flex-col bg-gray-900 rounded-lg border border-gray-800 overflow-hidden hover:border-indigo-500/40 hover:-translate-y-1 hover:rotate-[0.5deg] transition-all duration-300">
    <!-- Poster image -->
    <div class="aspect-[4/3] relative overflow-hidden">
      <div v-if="item.wideCoverUrl || item.coverUrl"
        class="absolute inset-0 bg-cover bg-center group-hover:scale-105 transition-transform duration-500"
        :style="{ backgroundImage: `url(${item.wideCoverUrl || item.coverUrl})` }" />
      <div v-else class="absolute inset-0 flex items-center justify-center text-3xl bg-gradient-to-br from-indigo-900/50 to-slate-800">🏛️</div>
      <!-- Event type badge -->
      <span v-if="eventType" :class="typeColors[eventType] || 'bg-gray-500/10 text-gray-400 border-gray-500/20'"
        class="absolute top-2 left-2 text-[10px] px-2 py-0.5 rounded-full border">{{ eventType }}</span>
    </div>

    <!-- Tear-off stub area -->
    <div class="px-4 pt-3 pb-4 border-t-2 border-dashed border-gray-700 relative">
      <h3 class="text-base font-bold text-white group-hover:text-indigo-400 transition-colors truncate">{{ item.title }}</h3>
      <div class="mt-2 space-y-1">
        <p v-if="eventDate" class="text-[11px] text-gray-400 flex items-center gap-1.5">
          <span>📅</span> {{ eventDate }}
        </p>
        <p v-if="venue" class="text-[11px] text-gray-500 flex items-center gap-1.5">
          <span>📍</span> {{ venue }}
        </p>
      </div>
      <div v-if="highlights.length" class="flex flex-wrap gap-1 mt-2">
        <span v-for="h in highlights.slice(0,3)" :key="h" class="text-[10px] px-1.5 py-0.5 rounded bg-gray-800 text-gray-400">{{ h }}</span>
      </div>
      <p class="text-[10px] text-gray-600 mt-3 text-right group-hover:text-indigo-400 transition-colors">查看详情 →</p>
    </div>
  </div>
</template>
