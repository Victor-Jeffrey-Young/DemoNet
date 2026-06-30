<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItems } from '../api/item'
import { getTags } from '../api/tag'
import { getMeta, TYPE_LIST } from '../constants/types'
import AppCard from '../components/AppCard.vue'
import TypeIcon from '../components/TypeIcon.vue'
import SearchBar from '../components/SearchBar.vue'

const route = useRoute()
const router = useRouter()
const items = ref([])
const current = ref(1)
const total = ref(0)
const loading = ref(false)
const allTags = ref([])
const activeTags = ref([])
const keyword = ref(route.query.keyword || '')
const activeType = ref(route.query.type || '')

const tagType = typeof TYPE_LIST[0] === 'string' ? 'string' : 'object'
const types = TYPE_LIST

function toggleTag(name) {
  const idx = activeTags.value.indexOf(name)
  idx >= 0 ? activeTags.value.splice(idx,1) : activeTags.value.push(name)
  fetchData(1)
}

async function fetchData(page = 1) {
  loading.value = true
  try {
    const params = { page, size:12 }
    if (activeType.value) params.type = activeType.value
    if (keyword.value) params.keyword = keyword.value
    if (activeTags.value.length) params.tags = activeTags.value.join(',')
    const data = await getItems(params)
    items.value = data.records || []
    current.value = data.current || 1
    total.value = data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

watch(() => route.query.keyword, (v) => { keyword.value = v || ''; fetchData(1) })
watch(() => route.query.type, (v) => { activeType.value = v || ''; fetchData(1) })

onMounted(async () => {
  try { allTags.value = await getTags() } catch {}
  fetchData()
})

function setType(t) {
  activeType.value = activeType.value===t ? '' : t
  router.replace({ query: { ...route.query, type: activeType.value || undefined } })
}

function setKeyword(v) {
  keyword.value = v
  router.replace({ query: { ...route.query, keyword: v || undefined } })
}

function clearAll() {
  activeTags.value = []
  activeType.value = ''
  keyword.value = ''
  router.replace({ query: {} })
  fetchData(1)
}

function goPage(p) { fetchData(p); window.scrollTo({top:0,behavior:'smooth'}) }
const totalPages = computed(() => Math.ceil(total.value/12) || 1)
</script>

<template>
  <div class="min-h-screen bg-gray-950 text-white">
    <main class="max-w-7xl mx-auto px-6 py-10">
      <h1 class="text-2xl font-bold mb-6">发现</h1>

      <div class="mb-6">
        <SearchBar @search="setKeyword" />
      </div>

      <div class="flex gap-2 mb-4 flex-wrap">
        <button v-for="t in types" :key="t" @click="setType(t)"
          :class="activeType===t ? `bg-${getMeta(t).accent}-600 text-white` : 'bg-gray-800 text-gray-400 hover:text-white'"
          class="text-xs px-3 py-1.5 rounded-full transition">
          <TypeIcon :type="t" size="16" /> {{ getMeta(t).label }}
        </button>
      </div>

      <div v-if="allTags.length" class="flex gap-2 mb-8 flex-wrap">
        <button v-for="tag in allTags" :key="tag.id" @click="toggleTag(tag.name)"
          :class="activeTags.includes(tag.name) ? 'bg-blue-600 text-white' : 'bg-gray-800 text-gray-400 hover:text-gray-200'"
          class="text-xs px-2.5 py-1 rounded-full transition">{{ tag.name }}</button>
        <button v-if="activeType || activeTags.length || keyword"
          @click="clearAll" class="text-xs text-red-400 hover:text-red-300 ml-2">清除全部</button>
      </div>

      <div v-if="loading" class="text-center text-gray-500 py-20">加载中...</div>
      <div v-else-if="items.length===0" class="text-center text-gray-500 py-20">暂无结果</div>

      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-10">
        <AppCard v-for="item in items" :key="item.id" :item="item" />
      </div>

      <div v-if="totalPages>1" class="flex justify-center gap-2">
        <button v-for="p in totalPages" :key="p" @click="goPage(p)"
          :class="p===current ? 'bg-blue-600' : 'bg-gray-800 hover:bg-gray-700'"
          class="w-10 h-10 rounded-lg text-sm font-medium transition">{{ p }}</button>
      </div>
    </main>
  </div>
</template>
