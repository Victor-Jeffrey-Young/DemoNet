<script setup>
import { ref, onMounted } from 'vue'
import { triggerSteamFetch, triggerTMDBFetch, getPendingItems, approveItem, rejectItem } from '../api/admin'

const pending = ref([])
const steamAppIds = ref('')
const tmdbQuery = ref('')
const message = ref('')
const loading = ref(false)

onMounted(() => loadPending())

async function loadPending() {
  try {
    const data = await getPendingItems({ page: 1, size: 50 })
    pending.value = data.records || []
  } catch (e) { console.error(e) }
}

async function fetchSteam() {
  const ids = steamAppIds.value.split(',').map(s => Number(s.trim())).filter(Boolean)
  if (ids.length === 0) return
  loading.value = true
  try {
    const res = await triggerSteamFetch(ids)
    message.value = res.message
    steamAppIds.value = ''
    setTimeout(() => loadPending(), 3000)
  } catch (e) {
    message.value = '触发失败: ' + (e.response?.data?.message || e.message)
  } finally { loading.value = false }
}

async function fetchTMDB() {
  if (!tmdbQuery.value.trim()) return
  loading.value = true
  try {
    const res = await triggerTMDBFetch(tmdbQuery.value.trim())
    message.value = res.message
    tmdbQuery.value = ''
    setTimeout(() => loadPending(), 3000)
  } catch (e) {
    message.value = '触发失败: ' + (e.response?.data?.message || e.message)
  } finally { loading.value = false }
}

async function approve(id) {
  await approveItem(id)
  pending.value = pending.value.filter(i => i.id !== id)
}

async function reject(id) {
  await rejectItem(id)
  pending.value = pending.value.filter(i => i.id !== id)
}
</script>

<template>
  <div class="min-h-screen bg-gray-950 text-white">
    <main class="max-w-5xl mx-auto px-6 py-10">
      <h1 class="text-2xl font-bold mb-8">管理后台</h1>

      <div v-if="message" class="mb-6 p-3 bg-gray-800 border border-gray-700 rounded-lg text-sm text-gray-300">
        {{ message }}
        <button @click="message = ''" class="ml-2 text-gray-500 hover:text-white">✕</button>
      </div>

      <section class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-12">
        <div class="bg-gray-900 rounded-2xl border border-gray-800 p-6">
          <h2 class="text-lg font-semibold mb-4">🎮 Steam 抓取</h2>
          <div class="flex gap-2">
            <input v-model="steamAppIds" placeholder="AppID, 如 730,570" type="text"
              class="flex-1 bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 text-sm focus:outline-none focus:border-blue-500" />
            <button @click="fetchSteam" :disabled="loading"
              class="bg-green-600 hover:bg-green-500 px-4 py-2 rounded-lg text-sm font-medium transition disabled:opacity-50">
              抓取
            </button>
          </div>
          <p class="text-xs text-gray-500 mt-2">输入 Steam AppID (逗号分隔)，通过 MQ 异步抓取</p>
        </div>

        <div class="bg-gray-900 rounded-2xl border border-gray-800 p-6">
          <h2 class="text-lg font-semibold mb-4">🎬 TMDB 搜索</h2>
          <div class="flex gap-2">
            <input v-model="tmdbQuery" placeholder="电影名, 如 Interstellar" type="text"
              class="flex-1 bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 text-sm focus:outline-none focus:border-blue-500" />
            <button @click="fetchTMDB" :disabled="loading"
              class="bg-blue-600 hover:bg-blue-500 px-4 py-2 rounded-lg text-sm font-medium transition disabled:opacity-50">
              搜索
            </button>
          </div>
          <p class="text-xs text-gray-500 mt-2">搜索 TMDB 电影，通过 MQ 异步抓取</p>
        </div>
      </section>

      <section>
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-lg font-semibold">待审核内容 ({{ pending.length }})</h2>
          <button @click="loadPending" class="text-sm text-blue-400 hover:underline">刷新</button>
        </div>

        <div v-if="pending.length === 0" class="text-center text-gray-500 py-10">暂无待审核内容</div>

        <div v-else class="space-y-3">
          <div v-for="item in pending" :key="item.id"
            class="bg-gray-900 rounded-xl border border-gray-800 p-4 flex items-start justify-between gap-4">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="text-xs px-2 py-0.5 rounded bg-gray-800 text-gray-400">{{ item.source }}</span>
                <span class="text-xs text-gray-600">{{ item.type }}</span>
              </div>
              <h3 class="font-semibold">{{ item.title }}</h3>
              <p class="text-gray-400 text-sm mt-1 line-clamp-2">{{ item.description }}</p>
            </div>
            <div class="flex gap-2 shrink-0">
              <button @click="approve(item.id)"
                class="bg-green-600 hover:bg-green-500 px-3 py-1.5 rounded-lg text-sm transition">上线</button>
              <button @click="reject(item.id)"
                class="bg-gray-700 hover:bg-red-600 px-3 py-1.5 rounded-lg text-sm transition">丢弃</button>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>
