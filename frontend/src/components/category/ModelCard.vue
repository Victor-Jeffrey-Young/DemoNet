<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({ item: { type: Object, required: true } })
const router = useRouter()
function go() { router.push({ name: 'Detail', params: { slug: props.item.slug } }) }

const info = computed(() => {
  try { return typeof props.item.infoJson === 'string' ? JSON.parse(props.item.infoJson) : (props.item.infoJson || {}) } catch { return {} }
})
const grade = computed(() => info.value.grade || '')
const scale = computed(() => info.value.scale || '')
const material = computed(() => info.value.material || '')
const series = computed(() => info.value.series || '')
</script>

<template>
  <div @click="go"
    class="group cursor-pointer bg-[#141418] rounded-lg border border-gray-800 hover:border-amber-500/30 transition-all duration-500 overflow-hidden">
    <!-- Wide Cover Image -->
    <div class="aspect-[21/9] relative overflow-hidden bg-[#1a1a1e]">
      <div v-if="item.wideCoverUrl || item.coverUrl"
        class="absolute inset-0 bg-cover bg-center group-hover:scale-[1.02] transition-transform duration-700"
        :style="{ backgroundImage: `url(${item.wideCoverUrl || item.coverUrl})` }" />
      <!-- Fallback grid texture -->
      <div v-else class="absolute inset-0 flex items-center justify-center text-5xl opacity-25"
        style="background-image: repeating-linear-gradient(0deg,transparent,transparent 8px,#2a2a2e 8px,#2a2a2e 9px)">
        <span class="text-5xl">🧩</span>
      </div>
      <!-- Top warning stripe accent -->
      <div class="absolute top-0 left-0 right-0 h-[2px] bg-gradient-to-r from-amber-500/40 via-amber-500/20 to-transparent" />
      <!-- Bottom dark gradient -->
      <div class="absolute inset-x-0 bottom-0 h-20 bg-gradient-to-t from-[#141418] to-transparent" />
    </div>

    <!-- Info Bar -->
    <div class="px-5 py-4 flex items-center gap-4">
      <!-- Grade badge -->
      <div v-if="grade" class="flex items-center gap-1.5 shrink-0">
        <span class="text-[10px] font-bold tracking-wider text-amber-400 bg-amber-500/10 px-2 py-1 rounded border border-amber-500/20 font-mono">{{ grade }}</span>
        <span v-if="scale" class="text-[10px] text-gray-500 font-mono">{{ scale }}</span>
      </div>
      <!-- Title & description -->
      <div class="flex-1 min-w-0">
        <h3 class="text-base font-bold text-white group-hover:text-amber-400 transition-colors truncate">{{ item.title }}</h3>
        <div class="flex items-center gap-2 mt-0.5">
          <span v-if="material" class="text-[10px] text-gray-500">{{ material }}</span>
          <span v-if="series" class="text-[10px] text-gray-600">| {{ series }}</span>
        </div>
      </div>
      <!-- Arrow -->
      <span class="text-gray-600 group-hover:text-amber-500 group-hover:translate-x-1 transition-all duration-300 text-sm shrink-0">查看 →</span>
    </div>
  </div>
</template>
