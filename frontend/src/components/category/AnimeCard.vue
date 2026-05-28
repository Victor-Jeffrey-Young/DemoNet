<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({ item: { type: Object, required: true } })
const router = useRouter()
function go() { router.push({ name: 'Detail', params: { slug: props.item.slug } }) }
</script>

<template>
  <div @click="go"
    class="group cursor-pointer bg-white rounded-xl overflow-hidden border border-gray-200 hover:shadow-xl hover:shadow-violet-500/10 hover:border-violet-200 transition-all duration-500">
    <!-- Image -->
    <div class="aspect-[3/4] relative overflow-hidden bg-gray-100">
      <div v-if="item.wideCoverUrl || item.coverUrl"
        class="absolute inset-0 bg-cover bg-center group-hover:scale-105 group-hover:grayscale transition-all duration-700"
        :style="{ backgroundImage: `url(${item.wideCoverUrl || item.coverUrl})` }" />
      <div v-else class="absolute inset-0 bg-gradient-to-br from-violet-100 to-fuchsia-100 flex items-center justify-center text-4xl">🎭</div>
      <!-- Overlay on hover -->
      <div class="absolute inset-0 bg-gradient-to-t from-violet-900/80 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 flex items-end p-4">
        <span class="text-white text-xs font-medium translate-y-2 group-hover:translate-y-0 transition-transform duration-500">查看详情 →</span>
      </div>
    </div>
    <!-- Info -->
    <div class="p-3">
      <div class="flex items-center gap-2 mb-1">
        <span class="text-[10px] px-2 py-0.5 rounded-full bg-violet-100 text-violet-700 font-medium">动漫</span>
      </div>
      <h3 class="text-sm font-bold text-gray-900 group-hover:text-violet-600 transition-colors line-clamp-1">{{ item.title }}</h3>
    </div>
  </div>
</template>
