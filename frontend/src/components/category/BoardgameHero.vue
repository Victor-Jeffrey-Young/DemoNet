<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFeatured, getHotItems } from '../../api/item'
import { getMeta } from '../../constants/types'
import TypeIcon from '../TypeIcon.vue'

const router = useRouter()
const meta = getMeta('boardgame')
const items = ref([])

onMounted(async () => {
  try {
    const featured = await getFeatured({ type: 'boardgame' }) || []
    // 增加到 8 个，以保证移动端横向滑动时有足够的货架物品
    items.value = featured.length ? featured.slice(0, 8) : (await getHotItems({ type:'boardgame', limit:8 }) || [])
  } catch {}
})

function goDetail(slug) { router.push({ name:'Detail', params:{ slug } }) }

function weightStars(w) {
  return Math.round(parseFloat(w) || 0)
}
</script>

<template>
  <div v-if="items.length" class="relative overflow-hidden" style="background: linear-gradient(180deg, #2d1f0e 0%, #1a1206 40%, #0f0b05 100%)">
    <!-- Wood panel texture -->
    <div class="absolute inset-0 opacity-[0.06] pointer-events-none"
      style="background-image: repeating-linear-gradient(90deg,transparent,transparent 3px,#8B6914 3px,#8B6914 4px),repeating-linear-gradient(0deg,transparent,transparent 40px,rgba(139,105,20,0.3) 40px,rgba(139,105,20,0.3) 41px)" />

    <div class="relative w-full max-w-7xl mx-auto px-6 py-10">
      <!-- Header -->
      <div class="flex items-center gap-4 mb-8">
        <div class="flex items-center gap-3">
          <TypeIcon type="boardgame" :size="36" />
          <div>
            <h2 class="text-2xl sm:text-3xl font-black text-amber-100 tracking-tight">店员推荐</h2>
            <p class="text-xs sm:text-sm text-amber-700/60 tracking-widest uppercase mt-0.5">Staff Picks</p>
          </div>
        </div>
        <div class="flex-1 mx-2 sm:mx-6 h-px bg-gradient-to-r from-amber-800/40 via-amber-600/20 to-transparent" />
        <span class="text-[10px] sm:text-xs text-amber-700/40 font-mono hidden sm:inline-block">Board Game Store</span>
      </div>

      <!-- Shelf display -->
      <!-- 移动端：横向滚动 snap；PC端：Grid 布局 -->
      <div class="flex sm:grid sm:grid-cols-4 gap-4 sm:gap-6 overflow-x-auto snap-x snap-mandatory pb-4 pt-2 -mx-6 px-6 sm:mx-0 sm:px-0 scrollbar-hide">
        <div v-for="item in items" :key="item.id" @click="goDetail(item.slug)"
          class="snap-center shrink-0 w-[140px] sm:w-auto group cursor-pointer flex flex-col relative z-10">
          
          <!-- Box container for shelf positioning -->
          <div class="relative w-full">
            <!-- Wood shelf backdrop (per item, bleeds into gaps to form continuous shelf) -->
            <!-- gap-4 (16px) => -inset-x-2 (8px), gap-6 (24px) => -inset-x-3 (12px) -->
            <div class="absolute -inset-x-2 sm:-inset-x-3 -bottom-2 sm:-bottom-3 h-3 sm:h-4 rounded-[1px] pointer-events-none"
              style="background: linear-gradient(180deg, #3d2b0f 0%, #2a1d0a 60%, #1a0f04 100%); box-shadow: 0 4px 12px rgba(0,0,0,0.6), inset 0 1px 0 rgba(139,105,20,0.15); z-index: 0;" />
            
            <!-- Game box -->
            <div class="relative z-10 aspect-[4/5] rounded-md overflow-hidden border-2 border-amber-900/30 group-hover:border-amber-500/50 bg-[#1a1206] shadow-xl shadow-black group-hover:shadow-amber-500/20 group-hover:-translate-y-1 sm:group-hover:-translate-y-2 transition-all duration-300 origin-bottom">
              <!-- Cover art -->
              <div v-if="item.wideCoverUrl || item.coverUrl"
                class="absolute inset-0 bg-cover bg-center group-hover:scale-105 transition-transform duration-500"
                :style="{ backgroundImage: `url(${item.wideCoverUrl || item.coverUrl})` }" />
              <!-- Fallback -->
              <div v-else class="absolute inset-0 bg-gradient-to-br from-amber-900 via-yellow-800 to-amber-950 flex items-center justify-center">
                <span class="text-5xl opacity-30">🎲</span>
              </div>
              <!-- Shine line on box edge -->
              <div class="absolute top-0 left-0 right-0 h-[1px] bg-gradient-to-r from-transparent via-white/10 to-transparent" />
              <!-- Box side shadow (3D effect) -->
              <div class="absolute top-0 right-0 w-[3px] h-full bg-black/30" />
            </div>
          </div>

          <!-- Box label (price tag style) -->
          <div class="mt-4 flex items-start gap-2">
            <!-- Tag -->
            <div class="shrink-0 mt-0.5">
              <div class="bg-amber-100 text-amber-900 text-[9px] sm:text-[10px] font-black px-1.5 py-0.5 rounded-sm border border-amber-300 shadow-sm"
                style="clip-path: polygon(0 0, 100% 0, 100% 70%, 85% 100%, 0 100%)">
                HOT
              </div>
            </div>
            <!-- Title + info -->
            <div class="min-w-0">
              <h4 class="text-xs sm:text-sm font-bold text-amber-50 group-hover:text-amber-300 transition-colors truncate">{{ item.title }}</h4>
              <div class="flex items-center gap-1.5 mt-1 text-[9px] sm:text-[10px] text-amber-700/60">
                <span v-if="item.infoJson" class="truncate">{{ (() => { try { const j = typeof item.infoJson === 'string' ? JSON.parse(item.infoJson) : item.infoJson; return (j.players || '') + (j.playtime ? ' · ' + j.playtime : '') } catch { return '' } })() }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div v-else />
</template>

<style scoped>
/* 隐藏原生滚动条但保留滚动功能 */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
