<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFeatured } from '../../api/item'
import { getMeta } from '../../constants/types'

const router = useRouter()
const meta = getMeta('movie')
const movies = ref([])
const active = ref(0)

onMounted(async () => {
  try { movies.value = await getFeatured({ type: 'movie' }) || [] } catch {}
  autoplayTimer = setInterval(() => {
    if (movies.value.length) active.value = (active.value + 1) % movies.value.length
  }, 5000)
})

onUnmounted(() => clearInterval(autoplayTimer))

let autoplayTimer = null

function next() { active.value = (active.value + 1) % movies.value.length }
function goDetail(slug) { router.push({ name:'Detail', params:{ slug } }) }

function watchTrailer(movie) {
  try {
    const info = typeof movie.infoJson === 'string' ? JSON.parse(movie.infoJson) : (movie.infoJson || {})
    const trailer = info.trailer || ''
    if (trailer) { window.open(trailer, '_blank'); return }
  } catch {}
  if (movie.externalLink) window.open(movie.externalLink, '_blank')
}
</script>

<template>
  <div class="relative w-full h-[calc(100vh-4rem)] overflow-hidden bg-black" v-if="movies.length">
    <!-- Background crossfade via Vue Transition -->
    <Transition name="hero-crossfade" mode="out-in">
      <div :key="active" class="absolute inset-0 z-0"
           :style="{ backgroundImage: `url(${movies[active].wideCoverUrl||movies[active].coverUrl||''})`, backgroundSize:'cover', backgroundPosition:'center' }">
        <div class="absolute inset-0 bg-gradient-to-r from-gray-950 via-gray-950/70 to-transparent" />
        <div class="absolute inset-0 bg-gradient-to-t from-gray-950 via-transparent to-transparent" />
      </div>
    </Transition>

    <div class="relative z-10 h-full flex flex-col justify-center px-12 max-w-4xl">
      <span class="text-red-400 text-xs tracking-widest uppercase mb-4">{{ meta.label }}</span>
      <h1 class="text-5xl sm:text-7xl font-black tracking-tighter mb-3 drop-shadow-lg">{{ movies[active].title }}</h1>
      <p class="text-gray-300 text-lg max-w-xl mb-8 leading-relaxed line-clamp-2">{{ movies[active].description }}</p>
      <div class="flex gap-4">
        <button @click="goDetail(movies[active].slug)"
          class="px-6 py-3 bg-red-600 hover:bg-red-500 rounded-xl text-sm font-semibold transition-colors shadow-lg shadow-red-900/30">
          查看详情
        </button>
        <button @click="watchTrailer(movies[active])"
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
  <div v-else class="w-full h-[calc(100vh-4rem)] flex flex-col items-center justify-center bg-gradient-to-b from-red-950 to-gray-950 gap-3">
    <span class="text-gray-500 text-4xl">🎬</span>
    <span class="text-gray-400 text-sm">管理员尚未配置轮播作品</span>
  </div>
</template>

<style scoped>
.hero-crossfade-enter-active,
.hero-crossfade-leave-active {
  transition: opacity 0.8s ease;
}
.hero-crossfade-enter-from,
.hero-crossfade-leave-to {
  opacity: 0;
}
</style>
