<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({ item: { type: Object, required: true } })
const router = useRouter()
function go() { router.push({ name:'Detail', params:{ slug: props.item.slug } }) }
</script>

<template>
  <div @click="go"
    class="flex rounded-2xl overflow-hidden h-48 cursor-pointer group border border-gray-800 hover:border-red-500/40 transition-all duration-500 bg-gray-900">
    <div class="w-1/3 shrink-0 relative overflow-hidden">
      <div v-if="item.wideCoverUrl || item.coverUrl"
        class="absolute inset-0 bg-cover bg-center group-hover:scale-105 transition-transform duration-700"
        :style="{ backgroundImage: `url(${item.wideCoverUrl || item.coverUrl})` }" />
      <div v-else class="absolute inset-0 bg-gradient-to-br from-red-900 to-gray-900 flex items-center justify-center text-3xl">🎬</div>
      <div class="absolute inset-0 bg-black/20 group-hover:bg-black/0 transition-colors" />
    </div>
    <div class="flex-1 p-5 flex flex-col justify-center">
      <h3 class="text-lg font-bold group-hover:text-red-400 transition-colors">{{ item.title }}</h3>
      <span class="text-xs px-2 py-0.5 rounded bg-red-900/40 text-red-400 inline-block w-fit mt-2 mb-2">电影</span>
      <p class="text-gray-400 text-sm line-clamp-2">{{ item.description }}</p>
      <span class="text-xs text-red-500/60 mt-auto pt-2 opacity-0 group-hover:opacity-100 transition-opacity duration-500">观看预告 →</span>
    </div>
  </div>
</template>
