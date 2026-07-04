<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItems } from '../api/item'
import { getTags } from '../api/tag'
import { getMeta, TYPE_LIST } from '../constants/types'
import AppCard from '../components/AppCard.vue'
import TypeIcon from '../components/TypeIcon.vue'
import SearchBar from '../components/SearchBar.vue'
import { Loading } from '@element-plus/icons-vue'

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
const tagSearch = ref('')
const showMobileFilters = ref(false)

const filteredAllTags = computed(() => {
  if (!tagSearch.value) return allTags.value
  const q = tagSearch.value.toLowerCase()
  return allTags.value.filter(t => t.name.toLowerCase().includes(q))
})


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
    const params = { page, size:60 }
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
const totalPages = computed(() => Math.ceil(total.value/60) || 1)
</script>

<template>
  <div class="min-h-screen bg-gray-950 text-white flex flex-col">
    <main class="flex-grow max-w-[90%] mx-auto w-full px-0 sm:px-2 lg:px-6 py-8 flex flex-col md:flex-row gap-6 md:gap-8">
      
      <!-- Sidebar / Filters -->
      <aside class="w-full md:w-64 shrink-0 space-y-6">
        <div>
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-2xl font-bold">探索发现</h2>
            <button @click="showMobileFilters = !showMobileFilters" class="md:hidden flex items-center gap-1.5 text-sm bg-gray-800 hover:bg-gray-700 px-3 py-1.5 rounded-lg text-gray-300 transition-colors border border-gray-700/50">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"></path></svg>
              <span>{{ showMobileFilters ? '收起筛选' : '展开筛选' }}</span>
            </button>
          </div>
          <SearchBar @search="setKeyword" class="mb-2" />
        </div>

        <div :class="showMobileFilters ? 'block' : 'hidden md:block'" class="space-y-6 bg-gray-900/40 md:bg-transparent p-4 md:p-0 rounded-xl md:rounded-none border border-gray-800/50 md:border-none">
          <div>
            <h3 class="text-xs sm:text-sm font-semibold text-gray-400 uppercase tracking-widest mb-3">品类</h3>
            <div class="flex flex-col gap-1">
              <button @click="setType('')"
                class="flex items-center justify-between px-3 py-2.5 rounded-lg transition-colors text-sm font-medium"
                :class="!activeType ? 'bg-blue-600/20 text-blue-400' : 'text-gray-400 hover:text-white hover:bg-gray-800'">
                <span>全部内容</span>
              </button>
              <button v-for="t in types" :key="t" @click="setType(t)"
                class="flex items-center justify-between px-3 py-2.5 rounded-lg transition-colors text-sm font-medium"
                :class="activeType===t ? `bg-gray-800 text-white` : 'text-gray-400 hover:text-white hover:bg-gray-800'">
                <span class="flex items-center gap-2.5">
                  <TypeIcon :type="t" size="18" :class="activeType===t ? `text-${getMeta(t).accent}-400` : 'opacity-70'" />
                  {{ getMeta(t).label }}
                </span>
              </button>
            </div>
          </div>

          <div>
            <div class="flex items-center justify-between mb-3">
              <h3 class="text-xs sm:text-sm font-semibold text-gray-400 uppercase tracking-widest">标签筛选</h3>
              <button v-if="activeTags.length > 0" @click="activeTags = []; fetchData(1)" class="text-xs text-blue-400 hover:text-blue-300 bg-blue-900/20 px-2 py-1 rounded">清空已选</button>
            </div>
            
            <input v-model="tagSearch" placeholder="搜索标签..." class="w-full bg-gray-950 md:bg-gray-900 border border-gray-800 rounded-lg px-3 py-2 text-sm text-gray-200 mb-3 focus:outline-none focus:border-gray-600 transition" />
            
            <div class="max-h-[240px] sm:max-h-[280px] overflow-y-auto pr-2 custom-scrollbar flex flex-wrap gap-2">
              <button v-for="tag in filteredAllTags" :key="tag.id" @click="toggleTag(tag.name)"
                :class="activeTags.includes(tag.name) ? 'bg-blue-600 text-white border-blue-500 shadow-lg shadow-blue-900/20' : 'bg-gray-950 md:bg-gray-900 text-gray-400 border-gray-800 hover:border-gray-600 hover:text-gray-200'"
                class="text-xs px-3 py-1.5 rounded-lg border transition-all flex-grow-0 text-left">
                {{ tag.name }}
              </button>
              <div v-if="filteredAllTags.length === 0" class="text-xs text-gray-500 py-2 w-full text-center">未找到标签</div>
            </div>
          </div>
        </div>
      </aside>

      <!-- Main Content -->
      <div class="flex-grow min-w-0">
        <div class="flex items-end justify-between mb-4 sm:mb-6 border-b border-gray-800/50 pb-3 sm:pb-4 mt-2 md:mt-0">
          <h2 class="text-base sm:text-lg font-bold text-gray-200">浏览结果</h2>
          <span class="text-xs sm:text-sm text-gray-500">共检索到 {{ total }} 项</span>
        </div>

        <!-- Active Filters Chips -->
        <div v-if="activeType || activeTags.length || keyword" class="flex flex-wrap gap-2 mb-6 p-2.5 sm:p-3 bg-gray-900/30 rounded-xl border border-gray-800/30 items-center">
          <span class="text-[10px] sm:text-xs text-gray-500 mr-1 hidden sm:inline-block">过滤条件:</span>
          
          <span v-if="keyword" class="bg-gray-800 text-gray-300 text-[10px] sm:text-xs px-2 py-1 sm:px-2.5 sm:py-1.5 rounded-lg flex items-center gap-1.5 border border-gray-700">
            <span class="text-gray-500 hidden sm:inline-block">搜索</span> "{{ keyword }}"
            <button @click="setKeyword('')" class="hover:text-white ml-0.5 sm:ml-1">&times;</button>
          </span>
          
          <span v-if="activeType" class="bg-gray-800 text-gray-300 text-[10px] sm:text-xs px-2 py-1 sm:px-2.5 sm:py-1.5 rounded-lg flex items-center gap-1.5 border border-gray-700">
            <span class="text-gray-500 hidden sm:inline-block">品类</span> {{ getMeta(activeType).label }}
            <button @click="setType('')" class="hover:text-white ml-0.5 sm:ml-1">&times;</button>
          </span>
          
          <span v-for="tag in activeTags" :key="tag" class="bg-blue-900/40 text-blue-300 text-[10px] sm:text-xs px-2 py-1 sm:px-2.5 sm:py-1.5 rounded-lg flex items-center gap-1.5 border border-blue-800/50">
            <span class="text-blue-500/70 hidden sm:inline-block">标签</span> {{ tag }}
            <button @click="toggleTag(tag)" class="hover:text-white ml-0.5 sm:ml-1">&times;</button>
          </span>
          
          <button @click="clearAll" class="text-[10px] sm:text-xs text-red-400 hover:text-red-300 ml-auto px-2 font-medium">重置全部</button>
        </div>

        <!-- Results Grid -->
        <div v-if="loading" class="text-center text-gray-500 py-20 sm:py-32 flex flex-col items-center gap-3">
          <el-icon class="is-loading" :size="28"><Loading /></el-icon>
          <span class="text-sm">正在检索数据库...</span>
        </div>
        
        <div v-else-if="items.length===0" class="text-center text-gray-500 py-16 sm:py-24 bg-gray-900/20 rounded-2xl border border-gray-800/30 mt-4 mx-2 sm:mx-0">
          <p class="text-base sm:text-lg mb-2 text-gray-300">没有找到相关内容</p>
          <p class="text-xs sm:text-sm text-gray-500">尝试减少筛选条件或更换搜索关键词</p>
          <button @click="clearAll" class="mt-5 sm:mt-6 text-xs sm:text-sm bg-gray-800 hover:bg-gray-700 text-gray-300 px-4 sm:px-5 py-2 sm:py-2.5 rounded-lg transition font-medium">清除所有筛选</button>
        </div>

        <div v-else>
          <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3 sm:gap-5 mb-10">
            <AppCard v-for="item in items" :key="item.id" :item="item" />
          </div>

          <!-- Pagination -->
          <div v-if="totalPages>1" class="flex flex-wrap justify-center gap-2 mt-8 sm:mt-12 mb-8 px-2">
            <button v-for="p in totalPages" :key="p" @click="goPage(p)"
              :class="p===current ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/20' : 'bg-gray-800 text-gray-400 hover:bg-gray-700 hover:text-white'"
              class="min-w-8 sm:min-w-10 h-8 sm:h-10 px-2 sm:px-3 rounded-lg sm:rounded-xl text-xs sm:text-sm font-medium transition">{{ p }}</button>
          </div>
        </div>
      </div>

    </main>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #374151; border-radius: 2px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #4b5563; }
</style>
