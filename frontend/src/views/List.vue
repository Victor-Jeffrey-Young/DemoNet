<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItems } from '../api/item'
import { getMeta, TYPE_LIST } from '../constants/types'
import AppCard from '../components/AppCard.vue'
import GameHero from '../components/category/GameHero.vue'
import GameCard from '../components/category/GameCard.vue'
import MovieHero from '../components/category/MovieHero.vue'
import MovieCard from '../components/category/MovieCard.vue'

const route = useRoute()
const router = useRouter()
const items = ref([])
const current = ref(1)
const total = ref(0)
const loading = ref(false)
const activeType = ref('')

const types = TYPE_LIST

async function fetchData(page = 1) {
  loading.value = true
  try {
    const data = await getItems({ page, size:12, type: activeType.value })
    items.value = data.records || []
    current.value = data.current || 1
    total.value = data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

watch(() => route.params.type, (t) => {
  activeType.value = t || ''
  fetchData(1)
})

onMounted(() => {
  activeType.value = route.params.type || ''
  fetchData()
})

function goPage(p) { fetchData(p); window.scrollTo({top:0,behavior:'smooth'}) }
const totalPages = computed(() => Math.ceil(total.value/12) || 1)
const meta = computed(() => getMeta(activeType.value))
const showHero = computed(() => !!activeType.value && current.value === 1)
const isGame = computed(() => activeType.value === 'game')
const isMovie = computed(() => activeType.value === 'movie')
const CardComponent = computed(() => {
  if (isGame.value) return GameCard
  if (isMovie.value) return MovieCard
  return AppCard
})
</script>

<template>
  <div class="min-h-screen text-white" :class="meta.light ? 'bg-gray-100 text-gray-900' : 'bg-gray-950'">
    <GameHero v-if="isGame && showHero" />
    <MovieHero v-if="isMovie && showHero" />

    <main class="max-w-7xl mx-auto px-6 py-10">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h1 class="text-2xl font-bold">{{ meta.emoji }} {{ meta.label }}</h1>
          <p class="text-sm mt-1" :class="meta.light ? 'text-gray-500' : 'text-gray-400'">共 {{ total }} 条内容</p>
        </div>
      </div>

      <div v-if="loading" class="text-center py-20" :class="meta.light ? 'text-gray-400' : 'text-gray-500'">加载中...</div>
      <div v-else-if="items.length===0" class="text-center py-20" :class="meta.light ? 'text-gray-400' : 'text-gray-500'">暂无内容</div>

      <div v-else :class="isMovie ? 'space-y-4' : 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6'"
           class="mb-10">
        <component :is="CardComponent" v-for="item in items" :key="item.id" :item="item" />
      </div>

      <div v-if="totalPages>1" class="flex justify-center gap-2">
        <button v-for="p in totalPages" :key="p" @click="goPage(p)"
          :class="p===current ? `bg-${meta.accent}-600` : 'bg-gray-800 hover:bg-gray-700'"
          class="w-10 h-10 rounded-lg text-sm font-medium transition">{{ p }}</button>
      </div>
    </main>
  </div>
</template>
