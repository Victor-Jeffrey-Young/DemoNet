<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({ item: { type: Object, required: true } })
const router = useRouter()
function go() { router.push({ name: 'Detail', params: { slug: props.item.slug } }) }

const info = computed(() => {
  try { return typeof props.item.infoJson === 'string' ? JSON.parse(props.item.infoJson) : (props.item.infoJson || {}) } catch { return {} }
})
const origin = computed(() => info.value.origin || '')
const year = computed(() => info.value.year || '')
</script>

<template>
  <div @click="go" class="group cursor-pointer flex flex-col">
    <!-- DVD case - plastic case feel -->
    <div class="aspect-[2/3] rounded-sm overflow-hidden relative border-2 border-black/20 group-hover:border-violet-400/50 shadow-md group-hover:shadow-violet-500/20 group-hover:-translate-y-1 transition-all duration-300"
      style="background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)">
      <!-- Cover image -->
      <div v-if="item.coverUrl || item.wideCoverUrl"
        class="absolute inset-0 bg-cover bg-center group-hover:scale-105 transition-transform duration-500"
        :style="{ backgroundImage: `url(${item.coverUrl || item.wideCoverUrl})` }" />
      <!-- No cover fallback -->
      <div v-else class="absolute inset-0 flex items-center justify-center text-4xl opacity-30">🎭</div>

      <!-- DVD plastic case reflection -->
      <div class="absolute inset-0 pointer-events-none opacity-10"
        style="background: linear-gradient(135deg, rgba(255,255,255,0.4) 0%, transparent 40%, transparent 60%, rgba(255,255,255,0.1) 100%)" />

      <!-- Top label strip -->
      <div class="absolute top-0 left-0 right-0 h-5 bg-gradient-to-r from-violet-900/80 via-fuchsia-800/60 to-transparent flex items-center px-2">
        <span v-if="year" class="text-[8px] font-bold text-white tracking-widest">{{ year }}</span>
        <span v-else class="text-[8px] font-bold text-white tracking-widest uppercase">DVD</span>
        <span v-if="origin" class="text-[8px] text-violet-200/80 ml-auto">{{ origin }}</span>
      </div>

      <!-- Bottom bar with spec -->
      <div class="absolute bottom-0 left-0 right-0 h-6 bg-gradient-to-t from-black/80 to-transparent flex items-end px-2 pb-1">
        <span class="text-[9px] text-white/80 font-medium truncate">{{ item.title }}</span>
      </div>

      <!-- Hover: subtle corner indicator -->
      <div class="absolute inset-0 bg-gradient-to-t from-violet-900/60 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-end p-3">
        <span class="text-white/70 text-[10px] tracking-wide">查看详情</span>
      </div>
    </div>

    <!-- Shelf bar beneath -->
    <div class="h-1.5 rounded-b mx-1 -mt-px"
      style="background: linear-gradient(180deg, #2d1f3d 0%, #1a1030 100%); box-shadow: 0 1px 3px rgba(0,0,0,0.3)" />
  </div>
</template>
