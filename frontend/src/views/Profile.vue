<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getUserItems, removeUserItem } from '../api/auth'
import TypeIcon from '../components/TypeIcon.vue'
import { getMeta } from '../constants/types'

const router = useRouter()
const auth = useAuthStore()
const allItems = ref([])
const activeTab = ref('want_to_play')
const loading = ref(true)

const statusLabels = {
  want_to_play: '想玩',
  played: '已体验',
  loved: '最爱',
  dropped: '弃了',
}

onMounted(async () => {
  await auth.fetchUser()
  loadItems()
})

async function loadItems() {
  loading.value = true
  try {
    // 移除参数，一次性拉取所有收藏以便计算统计数据
    allItems.value = await getUserItems() || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function switchTab(tab) {
  activeTab.value = tab
}

async function handleRemove(itemId) {
  try {
    await removeUserItem(itemId)
    allItems.value = allItems.value.filter(i => i.item_id !== itemId)
  } catch (e) {
    console.error(e)
  }
}

function goDetail(slug) {
  if (slug) router.push({ name: 'Detail', params: { slug } })
}

const displayItems = computed(() => {
  return allItems.value.filter(i => i.status === activeTab.value)
})

const stats = computed(() => {
  const s = { total: allItems.value.length, want: 0, played: 0, loved: 0 }
  allItems.value.forEach(i => {
    if (i.status === 'want_to_play') s.want++
    if (i.status === 'played') s.played++
    if (i.status === 'loved') s.loved++
  })
  return s
})

const avatarUrl = computed(() => {
  if (auth.user?.avatar) return auth.user.avatar
  const seed = auth.user?.username || 'user'
  // 使用 DiceBear 生成一个可爱的机器人头像作为默认头像
  return `https://api.dicebear.com/7.x/bottts/svg?seed=${seed}&backgroundColor=1f2937`
})

function formatDate(ds) {
  if (!ds) return ''
  return new Date(ds).toLocaleDateString()
}
</script>

<template>
  <div class="min-h-screen bg-gray-950 text-white pb-20">
    <!-- Profile Banner & Header -->
    <div class="relative h-48 md:h-64 bg-gradient-to-br from-blue-900 via-gray-900 to-black overflow-hidden border-b border-gray-800">
      <div class="absolute inset-0 opacity-20 bg-[url('https://www.transparenttextures.com/patterns/stardust.png')]"></div>
      <div class="absolute inset-0 bg-gradient-to-t from-gray-950 via-gray-950/50 to-transparent"></div>
    </div>

    <div class="max-w-[90%] mx-auto px-4 md:px-6 relative -mt-16 md:-mt-24">
      <div class="flex flex-col md:flex-row items-center md:items-end gap-6 mb-12">
        <div class="w-32 h-32 md:w-40 md:h-40 rounded-full border-4 border-gray-950 bg-gray-800 overflow-hidden shadow-2xl shrink-0">
          <img :src="avatarUrl" class="w-full h-full object-cover" />
        </div>
        <div class="flex-grow text-center md:text-left mb-2">
          <h1 class="text-3xl md:text-4xl font-black text-white mb-1">{{ auth.user?.username }}</h1>
          <p class="text-gray-400 text-sm mb-4">{{ auth.user?.email }} · 加入于 {{ formatDate(auth.user?.created_at) }}</p>
          <div class="flex flex-wrap justify-center md:justify-start gap-3 md:gap-4">
            <div class="bg-gray-900 border border-gray-800 rounded-lg px-4 py-2 flex items-center gap-2 shadow-lg">
              <span class="text-gray-500 text-xs font-semibold">总收藏</span>
              <span class="text-lg font-bold text-white">{{ stats.total }}</span>
            </div>
            <div class="bg-gray-900 border border-gray-800 rounded-lg px-4 py-2 flex items-center gap-2 shadow-lg">
              <span class="text-blue-500 text-xs font-semibold">已体验</span>
              <span class="text-lg font-bold text-white">{{ stats.played }}</span>
            </div>
            <div class="bg-gray-900 border border-gray-800 rounded-lg px-4 py-2 flex items-center gap-2 shadow-lg">
              <span class="text-red-500 text-xs font-semibold">最爱</span>
              <span class="text-lg font-bold text-white">{{ stats.loved }}</span>
            </div>
          </div>
        </div>
        <div class="shrink-0 mb-2 mt-4 md:mt-0">
          <button @click="auth.logout(); router.push('/')"
            class="bg-gray-800 hover:bg-gray-700 text-gray-300 text-sm px-6 py-3 rounded-xl transition font-medium border border-gray-700 shadow-lg">
            退出登录
          </button>
        </div>
      </div>

      <!-- Main Content -->
      <div class="flex flex-col md:flex-row gap-10">
        <!-- Sidebar Navigation -->
        <aside class="w-full md:w-56 shrink-0">
          <div class="flex flex-row md:flex-col gap-2 overflow-x-auto custom-scrollbar pb-2 md:pb-0">
            <button v-for="(label, key) in statusLabels" :key="key"
              @click="switchTab(key)"
              :class="activeTab === key ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/20' : 'bg-gray-900 text-gray-400 hover:bg-gray-800 hover:text-gray-200'"
              class="flex items-center justify-between px-4 py-3.5 rounded-xl transition-all whitespace-nowrap text-sm font-medium border border-transparent"
              :style="activeTab === key ? '' : 'border-color: #1f2937;'">
              <span>{{ label }}</span>
              <span class="bg-gray-950/50 px-2.5 py-0.5 rounded-md text-xs font-bold">{{ allItems.filter(i => i.status === key).length }}</span>
            </button>
          </div>
        </aside>

        <!-- Collection Grid -->
        <main class="flex-grow min-w-0">
          <div class="mb-6 pb-4 border-b border-gray-800/50 flex items-center justify-between">
            <h2 class="text-xl font-bold text-gray-200">{{ statusLabels[activeTab] }}</h2>
          </div>

          <div v-if="loading" class="text-center py-20 text-gray-500">加载数据中...</div>
          
          <div v-else-if="displayItems.length === 0" class="text-center py-32 bg-gray-900/30 rounded-3xl border border-gray-800/50 shadow-inner">
            <div class="text-6xl mb-4 opacity-50 grayscale">📭</div>
            <p class="text-lg text-gray-300 font-medium">列表空空如也</p>
            <p class="text-sm text-gray-500 mt-2">快去探索并添加你感兴趣的内容吧</p>
            <button @click="router.push('/')" class="mt-8 bg-gray-800 hover:bg-gray-700 text-gray-300 px-6 py-2.5 rounded-xl transition font-medium text-sm border border-gray-700">探索发现</button>
          </div>

          <div v-else class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-5">
            <div v-for="item in displayItems" :key="item.id"
              class="group relative bg-gray-900 rounded-2xl overflow-hidden cursor-pointer border border-gray-800 hover:border-blue-500/50 transition-all shadow-lg hover:shadow-2xl hover:-translate-y-1.5 duration-300"
              @click="goDetail(item.slug)">
              
              <div class="aspect-[3/4] relative bg-gray-800 w-full">
                <img v-if="item.poster_url || item.cover_url" :src="item.poster_url || item.cover_url" class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105" />
                <div v-else class="absolute inset-0 flex items-center justify-center text-gray-600 bg-gray-900">
                  <TypeIcon :type="item.type" size="40" />
                </div>
                
                <div class="absolute inset-0 bg-gradient-to-t from-gray-950 via-gray-950/20 to-transparent opacity-90 group-hover:opacity-70 transition-opacity"></div>
                
                <div class="absolute top-3 left-3 flex gap-1 z-10">
                  <span class="text-[9px] px-2 py-1 rounded-lg bg-gray-950/80 text-gray-300 backdrop-blur-sm border border-gray-700/50 uppercase tracking-widest font-bold flex items-center gap-1.5 shadow-lg">
                    <TypeIcon :type="item.type" size="12" :class="`text-${getMeta(item.type).accent}-400`" />
                    {{ getMeta(item.type).label }}
                  </span>
                </div>
                
                <button @click.stop="handleRemove(item.item_id)"
                  class="absolute top-3 right-3 w-8 h-8 rounded-full bg-red-900/90 text-red-300 backdrop-blur-sm border border-red-700/50 flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-red-600 hover:text-white transition-all transform scale-90 group-hover:scale-100 z-10 shadow-lg"
                  title="移除收藏">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
                </button>
              </div>
              
              <div class="p-4 absolute bottom-0 left-0 right-0 z-10">
                <h3 class="font-bold text-white text-sm md:text-base leading-tight line-clamp-2 drop-shadow-md group-hover:text-blue-300 transition-colors">{{ item.title }}</h3>
                <p class="text-[11px] text-gray-400 mt-1.5 flex items-center gap-1.5 opacity-80">
                  收藏于 {{ formatDate(item.created_at) }}
                </p>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { height: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #374151; border-radius: 2px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #4b5563; }
</style>