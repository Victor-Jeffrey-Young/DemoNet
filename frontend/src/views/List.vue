<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItems } from '../api/item'
import { getMeta, TYPE_LIST } from '../constants/types'
import AppCard from '../components/AppCard.vue'
import TypeIcon from '../components/TypeIcon.vue'
import GameHero from '../components/category/GameHero.vue'
import GameCard from '../components/category/GameCard.vue'
import MovieHero from '../components/category/MovieHero.vue'
import MovieCard from '../components/category/MovieCard.vue'
import MoviePosterCard from '../components/category/MoviePosterCard.vue'
import AnimeHero from '../components/category/AnimeHero.vue'
import AnimeCard from '../components/category/AnimeCard.vue'
import ModelHero from '../components/category/ModelHero.vue'
import ModelCard from '../components/category/ModelCard.vue'
import BoardgameHero from '../components/category/BoardgameHero.vue'
import BoardgameCard from '../components/category/BoardgameCard.vue'
import BookHero from '../components/category/BookHero.vue'
import BookCard from '../components/category/BookCard.vue'
import MusicHero from '../components/category/MusicHero.vue'
import MusicCard from '../components/category/MusicCard.vue'
import DigitalHero from '../components/category/DigitalHero.vue'
import DigitalCard from '../components/category/DigitalCard.vue'
import CoffeeHero from '../components/category/CoffeeHero.vue'
import CoffeeCard from '../components/category/CoffeeCard.vue'
import OfflineHero from '../components/category/OfflineHero.vue'
import OfflineCard from '../components/category/OfflineCard.vue'

const route = useRoute()
const router = useRouter()
const items = ref([])
const current = ref(1)
const total = ref(0)
const loading = ref(false)
const activeType = ref('')
const movieViewMode = ref('poster')

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
const isAnime = computed(() => activeType.value === 'anime')
const isModel = computed(() => activeType.value === 'model')
const isBoardgame = computed(() => activeType.value === 'boardgame')
const isBook = computed(() => activeType.value === 'book')
const isMusic = computed(() => activeType.value === 'music')
const isDigital = computed(() => activeType.value === 'digital')
const isCoffee = computed(() => activeType.value === 'coffee')
const isOffline = computed(() => activeType.value === 'offline')
const CardComponent = computed(() => {
  if (isGame.value) return GameCard
  if (isMovie.value) return movieViewMode.value === 'poster' ? MoviePosterCard : MovieCard
  if (isAnime.value) return AnimeCard
  if (isModel.value) return ModelCard
  if (isBoardgame.value) return BoardgameCard
  if (isBook.value) return BookCard
  if (isMusic.value) return MusicCard
  if (isDigital.value) return DigitalCard
  if (isCoffee.value) return CoffeeCard
  if (isOffline.value) return OfflineCard
  return AppCard
})
</script>

<template>
  <div class="min-h-screen text-white" :class="[
    meta.light ? 'bg-gray-100 text-gray-900' : '',
    isBoardgame ? 'bg-[#1a1206]' : '',
    isAnime ? 'bg-[#0c0a1a]' : '',
    !meta.light && !isBoardgame && !isAnime ? 'bg-gray-950' : '',
  ]">
    <GameHero v-if="isGame && showHero" />
    <MovieHero v-if="isMovie && showHero" />
    <AnimeHero v-if="isAnime && showHero" />
    <ModelHero v-if="isModel && showHero" />
    <BoardgameHero v-if="isBoardgame && showHero" />
    <BookHero v-if="isBook && showHero" />
    <MusicHero v-if="isMusic && showHero" />
    <DigitalHero v-if="isDigital && showHero" />
    <CoffeeHero v-if="isCoffee && showHero" />
    <OfflineHero v-if="isOffline && showHero" />

    <main class="max-w-[90%] mx-auto px-6 py-10">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h1 class="text-2xl font-bold"><TypeIcon :type="activeType" size="24" /> {{ meta.label }}</h1>
          <p class="text-sm mt-1" :class="meta.light ? 'text-gray-500' : 'text-gray-400'">共 {{ total }} 条内容</p>
        </div>
        <div v-if="isMovie" class="flex items-center gap-1 bg-gray-800 rounded-lg p-0.5">
          <button :class="['px-3 py-1.5 rounded text-sm transition', movieViewMode==='poster'?'bg-red-600 text-white':'text-gray-400 hover:text-white']"
            @click="movieViewMode='poster'">海报</button>
          <button :class="['px-3 py-1.5 rounded text-sm transition', movieViewMode==='list'?'bg-red-600 text-white':'text-gray-400 hover:text-white']"
            @click="movieViewMode='list'">横卡</button>
        </div>
      </div>

      <div v-if="loading" class="text-center py-20" :class="meta.light ? 'text-gray-400' : 'text-gray-500'">加载中...</div>
      <div v-else-if="items.length===0" class="text-center py-20" :class="meta.light ? 'text-gray-400' : 'text-gray-500'">暂无内容</div>

      <div v-else
        :class="[
          isMovie && movieViewMode==='list' ? 'space-y-4' : '',
          isMovie && movieViewMode==='poster' ? 'grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-5' : '',
          isAnime ? 'grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4' : '',
          isModel ? 'space-y-4' : '',
          isBoardgame ? 'grid grid-cols-1 lg:grid-cols-2 gap-5' : '',
          isBook ? 'grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-5' : '',
          isMusic ? 'grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4' : '',
          isDigital ? 'grid grid-cols-1 lg:grid-cols-2 gap-5' : '',
          isCoffee ? 'grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-5' : '',
          isOffline ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5' : '',
          !isMovie && !isAnime && !isModel && !isBoardgame && !isBook && !isMusic && !isDigital && !isCoffee && !isOffline ? 'grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 2xl:grid-cols-6 gap-5' : '',
        ]"
        class="mb-10">
        <component :is="CardComponent" v-for="item in items" :key="item.id" :item="item" />
      </div>

      <!-- Book shelf bar -->
      <div v-if="isBook" class="h-2 rounded-b mb-10 -mt-10"
        style="background: linear-gradient(180deg, #4a2c0e 0%, #2d1a05 100%); box-shadow: 0 3px 8px rgba(0,0,0,0.3)" />

      <div v-if="totalPages>1" class="flex justify-center gap-2">
        <button v-for="p in totalPages" :key="p" @click="goPage(p)"
          :class="p===current ? meta.pagBg : 'bg-gray-800 hover:bg-gray-700'"
          class="w-10 h-10 rounded-lg text-sm font-medium transition">{{ p }}</button>
      </div>
    </main>
  </div>
</template>
