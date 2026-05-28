<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFeatured, getHotItems } from '../../api/item'
import { getMeta } from '../../constants/types'

const router = useRouter()
const meta = getMeta('book')
const items = ref([])

onMounted(async () => {
  try {
    const featured = await getFeatured({ type: 'book' }) || []
    items.value = featured.length ? featured.slice(0, 4) : (await getHotItems({ type:'book', limit:4 }) || [])
  } catch {}
})

function goDetail(slug) { router.push({ name:'Detail', params:{ slug } }) }

function getInfo(item) {
  try { return typeof item.infoJson === 'string' ? JSON.parse(item.infoJson) : (item.infoJson || {}) } catch { return {} }
}
</script>

<template>
  <div v-if="items.length" class="relative overflow-hidden" style="background: linear-gradient(180deg, #451a03 0%, #291105 50%, #1a0c03 100%)">
    <!-- Spotlight -->
    <div class="absolute inset-0 opacity-30 pointer-events-none"
      style="background: radial-gradient(ellipse at 50% 30%, rgba(251,191,36,0.15) 0%, transparent 70%)" />

    <div class="relative max-w-7xl mx-auto px-6 py-10">
      <!-- Header with recommendation card -->
      <div class="flex items-center gap-6 mb-8">
        <div class="flex items-center gap-3">
          <span class="text-3xl">📖</span>
          <div>
            <h2 class="text-2xl font-black text-amber-100 tracking-tight">本月精选</h2>
            <p class="text-xs text-amber-700/60 tracking-widest uppercase mt-0.5">Staff Recommendations</p>
          </div>
        </div>
        <div class="flex-1 mx-4 h-px bg-gradient-to-r from-amber-800/40 via-amber-600/20 to-transparent" />
        <span class="text-xs text-amber-700/40 font-mono">Book Store</span>
      </div>

      <!-- Display table: books laid flat, cover-up -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-6 relative">
        <!-- Wood table surface -->
        <div class="absolute -inset-x-4 -bottom-6 h-4 rounded-b-lg"
          style="background: linear-gradient(180deg, #5c3a1a 0%, #3d2510 60%, #1a0c03 100%); box-shadow: 0 4px 16px rgba(0,0,0,0.4), inset 0 1px 0 rgba(251,191,36,0.08)" />

        <div v-for="item in items" :key="item.id" @click="goDetail(item.slug)"
          class="group cursor-pointer relative z-10">
          <!-- Book cover with 3D spine -->
          <div class="aspect-[3/4] rounded-sm overflow-hidden bg-[#2d1a05] shadow-lg group-hover:shadow-amber-500/10 group-hover:-translate-y-1 transition-all duration-300 relative"
            style="border-right: 3px solid rgba(0,0,0,0.25)">
            <!-- Cover image -->
            <div v-if="item.wideCoverUrl || item.coverUrl"
              class="absolute inset-0 bg-cover bg-center group-hover:scale-105 transition-transform duration-500"
              :style="{ backgroundImage: `url(${item.wideCoverUrl || item.coverUrl})` }" />
            <div v-else class="absolute inset-0 bg-gradient-to-br from-amber-200 via-amber-100 to-white flex items-center justify-center">
              <span class="text-5xl opacity-30">📖</span>
            </div>
            <!-- Spine highlights -->
            <div class="absolute top-0 right-[3px] w-[2px] h-full bg-gradient-to-b from-white/5 via-transparent to-black/10" />
            <div class="absolute top-0 left-0 right-0 h-[1px] bg-gradient-to-r from-transparent via-white/10 to-transparent" />
          </div>

          <!-- Recommendation card (handwritten style) -->
          <div class="mt-3 relative">
            <div class="bg-[#faf6f1] rounded-sm px-3 py-2 shadow-sm border border-amber-200/40"
              style="transform: rotate(-1deg); font-family: Georgia, serif;">
              <h4 class="text-xs font-bold text-amber-900 italic truncate">{{ item.title }}</h4>
              <p class="text-[10px] text-amber-700/60 mt-0.5">{{ getInfo(item).author || '' }}</p>
            </div>
            <!-- Pin -->
            <div class="absolute -top-1 left-3 w-2 h-2 rounded-full bg-red-400 shadow-sm" />
          </div>
        </div>
      </div>
    </div>
  </div>
  <div v-else />
</template>
