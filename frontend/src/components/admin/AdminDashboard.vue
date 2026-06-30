<script setup>
import { ref, onMounted } from 'vue'
import { getAdminStats } from '../../api/admin'
import { TYPE_LIST, getMeta } from '../../constants/types'

const stats = ref(null)
const loading = ref(false)

async function loadStats() {
  loading.value = true
  try {
    stats.value = await getAdminStats()
  } catch (e) {
    console.error('Failed to load stats:', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadStats)

defineExpose({ refresh: loadStats })
</script>

<template>
  <div v-loading="loading">
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
      <div class="bg-gray-800 rounded-lg p-4 border border-gray-700">
        <div class="text-2xl font-bold text-white">{{ stats?.total ?? '-' }}</div>
        <div class="text-xs text-gray-300 mt-1">内容总数</div>
      </div>
      <div class="bg-gray-800 rounded-lg p-4 border border-gray-700">
        <div class="text-2xl font-bold text-green-400">{{ stats?.online ?? '-' }}</div>
        <div class="text-xs text-gray-300 mt-1">已上线</div>
      </div>
      <div class="bg-gray-800 rounded-lg p-4 border border-gray-700">
        <div class="text-2xl font-bold text-amber-400">{{ stats?.pending ?? '-' }}</div>
        <div class="text-xs text-gray-300 mt-1">待审核</div>
      </div>
      <div class="bg-gray-800 rounded-lg p-4 border border-gray-700">
        <div class="text-2xl font-bold text-blue-400">{{ stats?.tagCount ?? '-' }}</div>
        <div class="text-xs text-gray-300 mt-1">标签数</div>
      </div>
    </div>

    <h3 class="text-sm font-semibold text-gray-200 mb-3">各品类分布</h3>
    <div class="grid grid-cols-2 md:grid-cols-5 gap-3">
      <div v-for="t in TYPE_LIST" :key="t" class="bg-gray-800/50 rounded-lg p-3 border border-gray-700/50">
        <div class="text-lg"><TypeIcon :type="t" size="22" /></div>
        <div class="text-sm text-white font-medium mt-1">{{ getMeta(t).label }}</div>
        <div class="text-xs text-gray-300">
          {{ stats?.byType?.find(b => b.type === t)?.count ?? 0 }} 项
        </div>
      </div>
    </div>
  </div>
</template>
