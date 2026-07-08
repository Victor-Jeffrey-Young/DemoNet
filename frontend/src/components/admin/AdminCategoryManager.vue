<script setup>
import { ref, onMounted } from 'vue'
import { getCategorySettings, updateCategorySettings } from '../../api/admin'
import { ElMessage } from 'element-plus'
import TypeIcon from '../TypeIcon.vue'
import { TYPE_META } from '../../constants/types'

const settings = ref([])
const loading = ref(false)
const saving = ref(false)

onMounted(() => load())

async function load() {
  loading.value = true
  try { settings.value = await getCategorySettings() } catch { settings.value = [] }
  loading.value = false
}

function toggle(type) {
  const s = settings.value.find(s => s.type === type)
  if (s) s.visible = s.visible ? 0 : 1
}

function moveUp(idx) {
  if (idx <= 0) return
  const arr = settings.value
  ;[arr[idx - 1], arr[idx]] = [arr[idx], arr[idx - 1]]
}

function moveDown(idx) {
  if (idx >= settings.value.length - 1) return
  const arr = settings.value
  ;[arr[idx], arr[idx + 1]] = [arr[idx + 1], arr[idx]]
}

async function save() {
  saving.value = true
  const body = settings.value.map((s, i) => ({
    type: s.type,
    visible: s.visible ? 1 : 0,
    sortOrder: i,
  }))
  try {
    await updateCategorySettings(body)
    ElMessage.success('已保存')
  } catch (e) {
    ElMessage.error(e.response?.data?.error || e.response?.data?.message || '保存失败')
  }
  saving.value = false
}

function refresh() { load() }
defineExpose({ refresh })
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <p class="text-sm text-gray-400">拖拽或使用上下按钮调整品类排序，点击切换显示/隐藏</p>
      <el-button type="primary"  @click="save" :loading="saving" :disabled="saving">保存更改</el-button>
    </div>
    <div v-if="loading" class="text-gray-500 text-sm py-8 text-center">加载中...</div>
    <div v-else class="space-y-2 max-w-2xl">
      <div v-for="(s, idx) in settings" :key="s.type"
        class="flex items-center gap-3 px-4 py-3 rounded-xl border transition-all duration-200"
        :class="s.visible
          ? 'bg-gray-800 border-gray-600'
          : 'bg-gray-900/50 border-gray-700/30 opacity-50'"
      >
        <div class="flex flex-col gap-0.5 shrink-0">
          <button @click="moveUp(idx)" :disabled="idx === 0"
            class="w-6 h-5 flex items-center justify-center text-gray-500 hover:text-white disabled:opacity-20 disabled:cursor-not-allowed transition text-xs leading-none">▲</button>
          <button @click="moveDown(idx)" :disabled="idx >= settings.length - 1"
            class="w-6 h-5 flex items-center justify-center text-gray-500 hover:text-white disabled:opacity-20 disabled:cursor-not-allowed transition text-xs leading-none">▼</button>
        </div>
        <div @click="toggle(s.type)" class="flex items-center gap-3 flex-1 cursor-pointer min-w-0">
          <div class="w-9 h-9 rounded-full bg-gray-700 flex items-center justify-center shrink-0">
            <TypeIcon :type="s.type" size="18" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="text-sm font-medium text-white truncate">{{ TYPE_META[s.type]?.label || s.type }}</div>
          </div>
          <div class="text-xs shrink-0 mr-2"
            :class="s.visible ? 'text-emerald-400' : 'text-gray-500'">
            {{ s.visible ? '显示' : '隐藏' }}
          </div>
          <div :class="s.visible ? 'bg-emerald-500' : 'bg-gray-600'"
            class="w-3.5 h-3.5 rounded-full transition-colors shrink-0" />
        </div>
      </div>
    </div>
  </div>
</template>
