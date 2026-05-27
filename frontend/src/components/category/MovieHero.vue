<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHotItems } from '../../api/item'
import { getMeta } from '../../constants/types'

const router = useRouter()
const meta = getMeta('movie')
const movies = ref([])
const active = ref(0)

onMounted(async () => {
  try { movies.value = await getHotItems({ type:'movie', limit:5 }) || [] } catch {}
  autoplayTimer = setInterval(() => {
    if (movies.value.length) active.value = (active.value + 1) % movies.value.length
  }, 5000)
})

onUnmounted(() => clearInterval(autoplayTimer))

let autoplayTimer = null

function next() { active.value = (active.value + 1) % movies.value.length }
function goDetail(slug) { router.push({ name:'Detail', params:{ slug } }) }
</script>

<template>
  <div class="relative w-full h-[calc(100vh-4rem)] overflow-hidden bg-black" v-if="movies[active]">
    <div class="absolute inset-0 z-0 transition-all duration-[900ms]"
         :style="{ backgroundImage: `url(${movies[active].coverUrl||movies[active].wideCoverUrl||''})`, backgroundSize:'cover', backgroundPosition:'center 30%' }">
      <div class="absolute inset-0 bg-gradient-to-r from-gray-950 via-gray-950/70 to-transparent" />
      <div class="absolute inset-0 bg-gradient-to-t from-gray-950 via-transparent to-transparent" />
    </div>

    <div class="relative z-10 h-full flex flex-col justify-center px-12 max-w-4xl">
      <span class="text-red-400 text-xs tracking-widest uppercase mb-4">{{ meta.label }}</span>
      <h1 class="text-5xl sm:text-7xl font-black tracking-tighter mb-3 drop-shadow-lg">{{ movies[active].title }}</h1>
      <p class="text-gray-300 text-lg max-w-xl mb-8 leading-relaxed line-clamp-2">{{ movies[active].description }}</p>
      <div class="flex gap-4">
        <button @click="goDetail(movies[active].slug)"
          class="px-6 py-3 bg-red-600 hover:bg-red-500 rounded-xl text-sm font-semibold transition-colors shadow-lg shadow-red-900/30">
          查看详情
        </button>
        <button v-if="movies[active].externalLink" @click="window.open(movies[active].externalLink,'_blank')"
          class="px-6 py-3 border border-white/20 hover:border-white/40 rounded-xl text-sm transition-all">
          ▶ 观看预告
        </button>
      </div>
    </div>

    <div class="absolute bottom-8 right-8 z-20 flex items-center gap-3">
      <div class="flex gap-2">
        <button v-for="(m,i) in movies" :key="i" @click="active = i"
          :class="i===active ? 'bg-red-500 w-8' : 'bg-white/30 w-2'"
          class="h-2 rounded-full transition-all duration-500" />
      </div>
      <button @click="next"
        class="w-12 h-12 rounded-full border border-white/20 flex items-center justify-center hover:bg-white/10 transition group">
        <svg class="w-5 h-5 text-white group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </button>
    </div>
  </div>
  <div v-else class="w-full h-[calc(100vh-4rem)] flex items-center justify-center bg-gradient-to-b from-red-950 to-gray-950">
    <span class="text-gray-500">暂无电影数据</span>
  </div>
</template>
