<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItems } from '../api/item'
import { getTags } from '../api/tag'
import AppCard from '../components/AppCard.vue'
import SearchBar from '../components/SearchBar.vue'

const route = useRoute()
const router = useRouter()

const items = ref([])
const current = ref(1)
const total = ref(0)
const loading = ref(false)
const allTags = ref([])

const typeLabels = {
  game:'游戏', movie:'电影', anime:'动漫', boardgame:'桌游', model:'模型', book:'书籍', music:'音乐', digital:'数码',
}
const types = ['game','movie','anime','boardgame','model','book','music','digital']

const activeType = ref('')
const activeTags = ref([])

function toggleTag(tagName) {
  const idx = activeTags.value.indexOf(tagName)
  if (idx >= 0) {
    activeTags.value.splice(idx, 1)
  } else {
    activeTags.value.push(tagName)
  }
  fetchData(1)
}

function clearTags() {
  activeTags.value = []
  fetchData(1)
}

async function fetchData(page = 1) {
  loading.value = true
  try {
    const params = {
      page,
      size: 12,
      type: activeType.value || undefined,
      keyword: route.query.keyword || undefined,
    }
    if (activeTags.value.length > 0) {
      params.tags = activeTags.value.join(',')
    }
    const data = await getItems(params)
    items.value = data.records || []
    current.value = data.current || 1
    total.value = data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

watch(() => route.params.type, (newType) => {
  activeType.value = newType || ''
  fetchData(1)
})

watch(() => route.query.keyword, () => fetchData(1))

onMounted(async () => {
  activeType.value = route.params.type || ''
  try { allTags.value = await getTags() } catch (e) { /* */ }
  fetchData()
})

function goType(type) {
  activeTags.value = []
  router.push({ name: 'List', params: { type: type || '' }, query: {} })
}

function goPage(page) {
  fetchData(page)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const totalPages = computed(() => Math.ceil(total.value / 12) || 1)
</script>

<template>
  <div class="min-h-screen bg-gray-950 text-white">
    <main class="max-w-7xl mx-auto px-6 py-10">
      <h1 class="text-2xl font-bold mb-6">
        {{ activeType ? (typeLabels[activeType] || activeType) : '全部内容' }}
      </h1>

      <div class="flex flex-col sm:flex-row items-start sm:items-center gap-4 mb-4">
        <div class="flex gap-2 flex-wrap">
          <button
            v-for="t in types" :key="t"
            @click="goType(activeType === t ? '' : t)"
            :class="activeType === t ? 'bg-blue-600 text-white' : 'bg-gray-800 text-gray-400 hover:text-white'"
            class="text-xs px-3 py-1.5 rounded-full transition"
          >{{ typeLabels[t] }}</button>
        </div>
        <div class="sm:ml-auto w-full sm:w-64">
          <SearchBar />
        </div>
      </div>

      <div v-if="allTags.length > 0" class="flex items-center gap-2 mb-8 flex-wrap">
        <span class="text-xs text-gray-500 mr-1">标签:</span>
        <button
          v-for="tag in allTags" :key="tag.id"
          @click="toggleTag(tag.name)"
          :class="activeTags.includes(tag.name) ? 'bg-blue-600 text-white' : 'bg-gray-800 text-gray-400 hover:text-gray-200'"
          class="text-xs px-2.5 py-1 rounded-full transition"
        >{{ tag.name }}</button>
        <button v-if="activeTags.length > 0" @click="clearTags"
          class="text-xs text-red-400 hover:text-red-300 ml-2">清除</button>
      </div>

      <div v-if="loading" class="text-center text-gray-500 py-20">加载中...</div>
      <div v-else-if="items.length === 0" class="text-center text-gray-500 py-20">暂无内容</div>

      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-10">
        <AppCard v-for="item in items" :key="item.id" :item="item" />
      </div>

      <div v-if="totalPages > 1" class="flex justify-center gap-2">
        <button v-for="p in totalPages" :key="p" @click="goPage(p)"
          :class="p === current ? 'bg-blue-600' : 'bg-gray-800 hover:bg-gray-700'"
          class="w-10 h-10 rounded-lg text-sm font-medium transition">{{ p }}</button>
      </div>
    </main>
  </div>
</template>
