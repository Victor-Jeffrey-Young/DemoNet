<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({ item: { type: Object, required: true } })
const router = useRouter()
function go() { router.push({ name: 'Detail', params: { slug: props.item.slug } }) }

const info = computed(() => {
  try { return typeof props.item.infoJson === 'string' ? JSON.parse(props.item.infoJson) : (props.item.infoJson || {}) } catch { return {} }
})
const author = computed(() => info.value.author || '')
const year = computed(() => info.value.year || '')
const category = computed(() => info.value.category || '')
</script>

<template>
  <div @click="go" class="group cursor-pointer flex flex-col">
    <!-- Book standing on shelf - cover facing out -->
    <div class="aspect-[3/4] rounded-sm overflow-hidden bg-[#3d2510] shadow-md group-hover:shadow-lg group-hover:-translate-y-1 transition-all duration-300 relative"
      style="border-right: 4px solid rgba(0,0,0,0.2)">
      <!-- Cover -->
      <div v-if="item.wideCoverUrl || item.coverUrl"
        class="absolute inset-0 bg-cover bg-center group-hover:scale-105 transition-transform duration-500"
        :style="{ backgroundImage: `url(${item.wideCoverUrl || item.coverUrl})` }" />
      <div v-else class="absolute inset-0 bg-gradient-to-br from-amber-200 via-amber-100 to-white flex items-center justify-center text-3xl opacity-40">📖</div>
      <!-- Spine highlights -->
      <div class="absolute top-0 right-[4px] w-[2px] h-full bg-gradient-to-b from-white/8 via-transparent to-black/15" />
      <div class="absolute top-0 left-0 right-0 h-[1px] bg-gradient-to-r from-transparent via-white/15 to-transparent" />
      <!-- Category ribbon -->
      <div v-if="category" class="absolute top-2 left-0 px-2 py-0.5 text-[9px] font-bold text-white bg-amber-700/80 rounded-r shadow-sm">
        {{ category }}
      </div>
      <!-- Hover overlay -->
      <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-end p-3">
        <span class="text-white text-xs font-medium">查看详情 →</span>
      </div>
    </div>

    <!-- Book info -->
    <div class="px-1 pt-2 pb-1">
      <h3 class="text-xs font-bold text-white group-hover:text-amber-300 transition-colors truncate leading-snug"
        style="font-family: Georgia, 'Times New Roman', serif;">{{ item.title }}</h3>
      <div class="flex items-center gap-1 mt-0.5">
        <span v-if="author" class="text-[10px] text-amber-600/60 truncate">{{ author }}</span>
        <span v-if="year" class="text-[10px] text-gray-600">{{ year }}</span>
      </div>
    </div>
  </div>
</template>
