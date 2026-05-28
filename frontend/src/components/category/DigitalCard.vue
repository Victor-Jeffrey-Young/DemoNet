<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({ item: { type: Object, required: true } })
const router = useRouter()
function go() { router.push({ name: 'Detail', params: { slug: props.item.slug } }) }

const info = computed(() => {
  try { return typeof props.item.infoJson === 'string' ? JSON.parse(props.item.infoJson) : (props.item.infoJson || {}) } catch { return {} }
})
const brand = computed(() => info.value.brand || '')
const category = computed(() => info.value.category || '')
const year = computed(() => info.value.year || '')
const features = computed(() => {
  const raw = info.value.features || ''
  if (!raw) return []
  return raw.split(',').map(f => f.trim()).filter(Boolean)
})
</script>

<template>
  <div @click="go" class="group cursor-pointer flex gap-4 p-4 bg-gray-900 border border-gray-800 rounded-xl hover:border-cyan-500/50 hover:shadow-lg hover:shadow-cyan-900/10 hover:-translate-y-0.5 transition-all duration-300">
    <div class="w-28 h-28 shrink-0 rounded-lg overflow-hidden bg-gray-800">
      <div v-if="item.wideCoverUrl || item.coverUrl"
        class="w-full h-full bg-cover bg-center group-hover:scale-105 transition-transform duration-500"
        :style="{ backgroundImage: `url(${item.wideCoverUrl || item.coverUrl})` }" />
      <div v-else class="w-full h-full flex items-center justify-center text-2xl text-gray-600">📱</div>
    </div>
    <div class="flex-1 min-w-0 flex flex-col justify-center gap-1.5">
      <div class="flex items-center gap-2">
        <span v-if="brand" class="text-[11px] text-gray-500 uppercase tracking-wider">{{ brand }}</span>
        <span v-if="year" class="text-[11px] text-gray-600">· {{ year }}</span>
      </div>
      <h3 class="text-base font-semibold text-gray-100 group-hover:text-cyan-400 transition-colors truncate">{{ item.title }}</h3>
      <div v-if="category" class="flex items-center gap-1.5 mt-0.5">
        <span class="text-[10px] px-2 py-0.5 rounded-full bg-cyan-500/10 text-cyan-300 border border-cyan-500/20">{{ category }}</span>
      </div>
      <div v-if="features.length" class="flex flex-wrap gap-1 mt-0.5">
        <span v-for="f in features.slice(0,4)" :key="f" class="text-[10px] px-1.5 py-0.5 rounded bg-gray-800 text-gray-400">{{ f }}</span>
        <span v-if="features.length > 4" class="text-[10px] text-gray-500">+{{ features.length - 4 }}</span>
      </div>
    </div>
  </div>
</template>
