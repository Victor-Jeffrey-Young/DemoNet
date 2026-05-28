<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFeatured, getHotItems } from '../../api/item'
import { getMeta } from '../../constants/types'

const router = useRouter()
const meta = getMeta('model')
const items = ref([])

onMounted(async () => {
  try {
    const featured = await getFeatured({ type: 'model' }) || []
    if (featured.length) {
      items.value = featured.slice(0, 4)
    } else {
      items.value = (await getHotItems({ type: 'model', limit: 4 })) || []
    }
  } catch {}
})

function goDetail(slug) { router.push({ name:'Detail', params:{ slug } }) }
</script>

<template>
  <div v-if="items.length" class="relative bg-[#0f0f11] overflow-hidden">
    <!-- Top warning stripe -->
    <div class="h-1 bg-[repeating-linear-gradient(-45deg,#f59e0b,#f59e0b_10px,#1a1a1e_10px,#1a1a1e_20px)]" />

    <!-- Header row -->
    <div class="max-w-7xl mx-auto px-6 pt-8 pb-4">
      <div class="flex items-center gap-3 mb-6">
        <span class="w-8 h-[2px] bg-amber-500/60" />
        <span class="text-amber-500/80 text-xs tracking-[0.25em] uppercase font-mono">精选陈列</span>
        <span class="flex-1" />
        <span class="text-xs text-gray-600 font-mono">{{ meta.emoji }} {{ meta.label }}</span>
      </div>

      <!-- Display shelf - horizontal row of cards -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div
          v-for="item in items" :key="item.id"
          @click="goDetail(item.slug)"
          class="group cursor-pointer bg-[#141418] rounded-lg border border-gray-800 hover:border-amber-500/30 transition-all duration-300 overflow-hidden"
        >
          <!-- Thumbnail -->
          <div class="aspect-[4/3] relative overflow-hidden bg-[#1a1a1e]">
            <div v-if="item.wideCoverUrl || item.coverUrl"
              class="absolute inset-0 bg-cover bg-center group-hover:scale-105 transition-transform duration-500"
              :style="{ backgroundImage: `url(${item.wideCoverUrl || item.coverUrl})` }" />
            <div v-else class="absolute inset-0 flex items-center justify-center text-3xl opacity-20"
              style="background-image: repeating-linear-gradient(0deg,transparent,transparent 6px,#2a2a2e 6px,#2a2a2e 7px)">
              🧩
            </div>
            <!-- Top amber accent line -->
            <div class="absolute top-0 left-0 right-0 h-[2px] bg-gradient-to-r from-amber-500/50 via-amber-500/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
            <!-- Hover overlay -->
            <div class="absolute inset-0 bg-gradient-to-t from-[#141418] via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-end justify-center pb-3">
              <span class="text-amber-400 text-xs font-medium tracking-wider">查看详情 →</span>
            </div>
          </div>
          <!-- Label -->
          <div class="px-3 py-2.5 border-t border-gray-800/50">
            <h4 class="text-xs font-bold text-white group-hover:text-amber-400 transition-colors truncate">{{ item.title }}</h4>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom warning stripe -->
    <div class="mt-6 h-1 bg-[repeating-linear-gradient(-45deg,#f59e0b,#f59e0b_10px,#1a1a1e_10px,#1a1a1e_20px)]" />
  </div>

  <!-- Empty: silent, no placeholder needed -->
  <div v-else />
</template>
