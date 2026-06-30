<script setup>
import { ref, onMounted, watch } from 'vue'
import { getCarouselItems, saveCarouselOrder, removeFromCarousel, getAdminItems } from '../../api/admin'
import { ElMessage } from 'element-plus'
import { TYPE_LIST, getMeta } from '../../constants/types'
import TypeIcon from '../../components/TypeIcon.vue'

const activeType = ref('game')
const carouselItems = ref([])
const poolItems = ref([])
const poolTotal = ref(0)
const loading = ref(false)
const saving = ref(false)
const searchKeyword = ref('')
const poolPage = ref(1)

async function loadCarousel() {
  try {
    carouselItems.value = await getCarouselItems(activeType.value) || []
  } catch (e) { console.error(e) }
}

async function loadPool() {
  loading.value = true
  try {
    const params = { type: activeType.value, status: 1, size: 50, page: poolPage.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await getAdminItems(params)
    const carouselIds = new Set(carouselItems.value.map(i => i.id))
    poolItems.value = (res.records || []).filter(i => !carouselIds.has(i.id))
    poolTotal.value = res.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function handleAdd(item) {
  const ids = [...carouselItems.value.map(i => i.id), item.id]
  await saveCarouselOrder(activeType.value, ids)
  await loadCarousel()
  await loadPool()
  ElMessage.success(`${item.title} 已加入轮播`)
}

async function handleRemove(item) {
  await removeFromCarousel(activeType.value, item.id)
  await loadCarousel()
  await loadPool()
  ElMessage.success(`${item.title} 已从轮播移除`)
}

async function handleMoveUp(index) {
  if (index === 0) return
  const ids = carouselItems.value.map(i => i.id)
  ;[ids[index - 1], ids[index]] = [ids[index], ids[index - 1]]
  await saveCarouselOrder(activeType.value, ids)
  await loadCarousel()
  ElMessage.success('顺序已更新')
}

async function handleMoveDown(index) {
  if (index === carouselItems.value.length - 1) return
  const ids = carouselItems.value.map(i => i.id)
  ;[ids[index], ids[index + 1]] = [ids[index + 1], ids[index]]
  await saveCarouselOrder(activeType.value, ids)
  await loadCarousel()
  ElMessage.success('顺序已更新')
}

function handleTypeChange() {
  poolPage.value = 1
  searchKeyword.value = ''
  loadCarousel()
  loadPool()
}

let searchTimer = null
watch(searchKeyword, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { poolPage.value = 1; loadPool() }, 300)
})

onMounted(() => { loadCarousel(); loadPool() })
defineExpose({ refresh() { loadCarousel(); loadPool() } })
</script>

<template>
  <div>
    <!-- Type tabs -->
    <div class="flex gap-1 mb-5 bg-gray-800/50 rounded-lg p-1 w-fit flex-wrap">
      <button
        v-for="t in TYPE_LIST" :key="t"
        @click="activeType = t; handleTypeChange()"
        :class="['px-3 py-1.5 rounded text-sm font-medium transition', activeType === t ? 'bg-blue-600 text-white' : 'text-gray-400 hover:text-white']"
      ><TypeIcon :type="t" size="16" /> {{ getMeta(t).label }}</button>
    </div>

    <!-- Current Carousel Sequence -->
    <div class="bg-gray-800/50 rounded-lg p-4 border border-gray-700 mb-5">
      <h4 class="text-sm font-semibold text-gray-200 mb-3">当前轮播序列 ({{ carouselItems.length }})</h4>
      <div v-if="carouselItems.length === 0" class="text-sm text-gray-500 py-6 text-center">尚未配置轮播作品，从下方可选池中添加</div>
      <div v-else class="space-y-2">
        <div
          v-for="(item, index) in carouselItems" :key="item.id"
          class="flex items-center gap-3 bg-gray-800 rounded-lg p-2 border border-gray-700/50 hover:border-gray-600 transition group"
        >
          <span class="text-xs font-bold text-gray-500 w-6 text-center">{{ index + 1 }}</span>
          <img
            v-if="item.wideCoverUrl || item.coverUrl"
            :src="item.wideCoverUrl || item.coverUrl"
            class="w-16 h-10 rounded object-cover"
          />
          <div v-else class="w-16 h-10 rounded bg-gray-700 flex items-center justify-center text-lg">
            <TypeIcon :type="item.type" size="18" />
          </div>
          <span class="flex-1 text-sm text-white truncate">{{ item.title }}</span>
          <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition">
            <button @click="handleMoveUp(index)" :disabled="index === 0"
              class="w-7 h-7 rounded bg-gray-700 hover:bg-gray-600 disabled:opacity-30 text-xs transition">▲</button>
            <button @click="handleMoveDown(index)" :disabled="index === carouselItems.length - 1"
              class="w-7 h-7 rounded bg-gray-700 hover:bg-gray-600 disabled:opacity-30 text-xs transition">▼</button>
            <button @click="handleRemove(item)"
              class="w-7 h-7 rounded bg-red-900/40 hover:bg-red-700/60 text-red-400 text-xs transition">✕</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Candidate Pool -->
    <div class="bg-gray-800/50 rounded-lg p-4 border border-gray-700">
      <div class="flex items-center gap-3 mb-3">
        <h4 class="text-sm font-semibold text-gray-200">可选作品池</h4>
        <el-input v-model="searchKeyword" placeholder="搜索..." clearable size="small" style="width:160px" />
      </div>
      <div v-loading="loading" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
        <div
          v-for="item in poolItems" :key="item.id"
          class="bg-gray-800 rounded-lg border border-gray-700/50 hover:border-blue-500/40 transition group cursor-pointer"
        >
          <div class="aspect-[2/1] bg-gray-900 rounded-t-lg overflow-hidden relative">
            <img
              v-if="item.wideCoverUrl || item.coverUrl"
              :src="item.wideCoverUrl || item.coverUrl"
              class="w-full h-full object-cover group-hover:scale-105 transition duration-300"
            />
            <div v-else class="w-full h-full flex items-center justify-center text-2xl opacity-30">
              <TypeIcon :type="item.type" size="24" />
            </div>
          </div>
          <div class="p-2">
            <p class="text-xs text-gray-300 truncate mb-2">{{ item.title }}</p>
            <button @click="handleAdd(item)"
              class="w-full py-1 rounded text-xs font-medium bg-blue-600/20 hover:bg-blue-600 text-blue-300 hover:text-white transition">
              + 加入轮播
            </button>
          </div>
        </div>
        <div v-if="poolItems.length === 0 && !loading" class="col-span-full text-center py-10 text-sm text-gray-500">
          暂无可选作品
        </div>
      </div>
    </div>
  </div>
</template>
