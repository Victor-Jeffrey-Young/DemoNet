<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({ item: { type: Object, required: true } })
const router = useRouter()
function go() { router.push({ name: 'Detail', params: { slug: props.item.slug } }) }

const info = computed(() => {
  try { return typeof props.item.infoJson === 'string' ? JSON.parse(props.item.infoJson) : (props.item.infoJson || {}) } catch { return {} }
})
const artist = computed(() => info.value.artist || '')
const year = computed(() => info.value.year || '')
const genre = computed(() => info.value.genre || '')
</script>

<template>
  <div @click="go" class="group cursor-pointer flex flex-col">
    <!-- Album cover -->
    <div class="aspect-square rounded-lg overflow-hidden shadow-lg group-hover:shadow-fuchsia-500/20 group-hover:-translate-y-1 transition-all duration-300 relative"
      style="background: linear-gradient(135deg, #1a0a2e 0%, #2d1b4e 100%)">
      <div v-if="item.wideCoverUrl || item.coverUrl"
        class="absolute inset-0 bg-cover bg-center group-hover:scale-105 transition-transform duration-500"
        :style="{ backgroundImage: `url(${item.wideCoverUrl || item.coverUrl})` }" />
      <div v-else class="absolute inset-0 flex items-center justify-center text-3xl opacity-30">🎵</div>
      <!-- Vinyl groove ring -->
      <div class="absolute inset-4 rounded-full border border-white/5 opacity-0 group-hover:opacity-100 transition-opacity" />
      <!-- Hover -->
      <div class="absolute inset-0 bg-gradient-to-t from-fuchsia-900/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex items-end p-3">
        <span class="text-white/80 text-xs">查看详情</span>
      </div>
    </div>
    <!-- Info -->
    <div class="px-1 pt-2 pb-1">
      <h3 class="text-xs font-bold text-white group-hover:text-fuchsia-300 transition-colors truncate">{{ item.title }}</h3>
      <p v-if="artist" class="text-[10px] text-fuchsia-400/40 truncate mt-0.5">{{ artist }}{{ year ? ' · ' + year : '' }}</p>
    </div>
  </div>
</template>
