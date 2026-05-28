<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({ item: { type: Object, required: true } })
const router = useRouter()
function go() { router.push({ name: 'Detail', params: { slug: props.item.slug } }) }

const info = computed(() => {
  try { return typeof props.item.infoJson === 'string' ? JSON.parse(props.item.infoJson) : (props.item.infoJson || {}) } catch { return {} }
})
const players = computed(() => info.value.players || '')
const playtime = computed(() => info.value.playtime || '')
const weight = computed(() => parseFloat(info.value.weight) || 0)
const weightStars = computed(() => Math.round(weight.value))

const hasVideo = computed(() => {
  const v = info.value.videos
  return (v?.bilibili || v?.youtube) ? true : false
})

function watchVideo() {
  const v = info.value.videos
  const url = v?.bilibili || v?.youtube
  if (url) window.open(url, '_blank')
}
</script>

<template>
  <div class="group cursor-pointer" @click="go">
    <!-- Game box on shelf -->
    <div class="flex items-end gap-4 p-4 rounded-xl bg-gradient-to-b from-[#1e1508] to-[#140e05] border border-amber-900/20 hover:border-amber-500/30 transition-all duration-300 hover:shadow-lg hover:shadow-amber-500/5">
      <!-- Box image -->
      <div class="shrink-0 w-28 sm:w-36 aspect-[4/5] rounded-md overflow-hidden border-2 border-amber-900/30 group-hover:border-amber-500/40 bg-[#1a1206] shadow-md relative group-hover:-translate-y-1 transition-all duration-300">
        <div v-if="item.wideCoverUrl || item.coverUrl"
          class="absolute inset-0 bg-cover bg-center group-hover:scale-105 transition-transform duration-500"
          :style="{ backgroundImage: `url(${item.wideCoverUrl || item.coverUrl})` }" />
        <div v-else class="absolute inset-0 bg-gradient-to-br from-amber-900 to-yellow-900 flex items-center justify-center text-3xl opacity-40">🎲</div>
        <!-- Box edge (3D) -->
        <div class="absolute top-0 right-0 w-[2px] h-full bg-black/40" />
        <div class="absolute top-0 left-0 right-0 h-[1px] bg-gradient-to-r from-transparent via-white/5 to-transparent" />
        <!-- Weight badge on box corner -->
        <div v-if="weight" class="absolute top-2 right-2 flex gap-px">
          <span v-for="i in 5" :key="i" :class="i <= weightStars ? 'text-amber-300' : 'text-gray-800'" class="text-[9px] drop-shadow-sm">★</span>
        </div>
      </div>

      <!-- Info area -->
      <div class="flex-1 min-w-0 pb-1">
        <!-- Category label -->
        <span class="text-[10px] px-2 py-0.5 rounded-sm bg-amber-900/30 text-amber-500 font-medium tracking-wide">BOARD GAME</span>

        <h3 class="text-base font-bold text-amber-50 group-hover:text-amber-300 transition-colors mt-2 truncate">{{ item.title }}</h3>

        <!-- Specs row -->
        <div class="flex items-center gap-3 mt-1.5 flex-wrap">
          <span v-if="players" class="text-xs text-amber-600/80 bg-amber-900/20 px-2 py-0.5 rounded flex items-center gap-1">
            <span>👥</span> {{ players }}
          </span>
          <span v-if="playtime" class="text-xs text-amber-600/80 bg-amber-900/20 px-2 py-0.5 rounded flex items-center gap-1">
            <span>⏱</span> {{ playtime }}
          </span>
        </div>

        <!-- Description -->
        <p class="text-xs text-amber-700/60 mt-2 line-clamp-2 leading-relaxed">{{ item.description }}</p>

        <!-- Actions -->
        <div class="flex items-center gap-3 mt-3">
          <span class="text-[10px] text-amber-600/40 group-hover:text-amber-500 transition-colors">查看游戏详情 →</span>
          <button v-if="hasVideo" @click.stop="watchVideo"
            class="text-[10px] px-2.5 py-1 rounded bg-amber-600/20 hover:bg-amber-600/40 text-amber-400 border border-amber-500/20 transition">
            ▶ 教学
          </button>
        </div>
      </div>
    </div>

    <!-- Wood shelf bar beneath -->
    <div class="h-1.5 rounded-b-lg mx-2 -mt-px"
      style="background: linear-gradient(180deg, #3d2b0f 0%, #2a1d0a 100%); box-shadow: 0 2px 4px rgba(0,0,0,0.3)" />
  </div>
</template>
