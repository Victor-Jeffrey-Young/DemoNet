<script setup>
import { ref, computed } from 'vue'

const brewDose = ref(15)
const brewRatio = ref('1:15')
const brewTemp = ref('92°C')

const brewRatioNum = computed(() => {
  const parts = brewRatio.value.split(':')
  return parts.length === 2 ? parseFloat(parts[1]) : 15
})

const brewTime = computed(() => {
  const dose = brewDose.value
  if (dose <= 15) return '2:00-2:30'
  if (dose <= 20) return '2:30-3:00'
  return '3:00-3:30'
})
</script>

<template>
  <div class="mt-4 p-4 bg-amber-900/10 border border-amber-500/10 rounded-lg">
    <div class="text-xs text-amber-400 font-medium mb-3 flex items-center gap-2">
      ☕ 冲煮参数计算器
    </div>
    <div class="flex flex-wrap items-end gap-3 mb-3">
      <div class="flex-1 min-w-[120px]">
        <label class="text-[10px] text-amber-500/70 block mb-1">咖啡粉量 (g)</label>
        <input v-model.number="brewDose" type="number" min="10" max="60" step="0.5"
          class="w-full bg-amber-900/30 border border-amber-500/20 rounded px-2.5 py-1.5 text-sm text-amber-200 focus:outline-none focus:border-amber-400/50" />
      </div>
      <div class="flex-1 min-w-[120px]">
        <label class="text-[10px] text-amber-500/70 block mb-1">粉水比</label>
        <select v-model="brewRatio" class="w-full bg-amber-900/30 border border-amber-500/20 rounded px-2.5 py-1.5 text-sm text-amber-200 focus:outline-none">
          <option value="1:15">1:15 (标准)</option>
          <option value="1:16">1:16 (偏淡)</option>
          <option value="1:14">1:14 (浓郁)</option>
          <option value="1:13">1:13 (极浓)</option>
        </select>
      </div>
      <div class="flex-1 min-w-[120px]">
        <label class="text-[10px] text-amber-500/70 block mb-1">水温</label>
        <select v-model="brewTemp" class="w-full bg-amber-900/30 border border-amber-500/20 rounded px-2.5 py-1.5 text-sm text-amber-200 focus:outline-none">
          <option value="88°C">88°C (深烘)</option>
          <option value="90°C">90°C (中深烘)</option>
          <option value="92°C">92°C (中烘/标准)</option>
          <option value="94°C">94°C (浅烘)</option>
        </select>
      </div>
    </div>
    <div class="grid grid-cols-2 gap-2 text-xs">
      <div class="bg-amber-900/20 rounded p-2">
        <span class="text-amber-500/70">注水量</span>
        <span class="text-amber-200 font-semibold float-right">{{ Math.round(brewDose * brewRatioNum) }}g</span>
      </div>
      <div class="bg-amber-900/20 rounded p-2">
        <span class="text-amber-500/70">水温</span>
        <span class="text-amber-200 font-semibold float-right">{{ brewTemp }}</span>
      </div>
      <div class="bg-amber-900/20 rounded p-2">
        <span class="text-amber-500/70">建议时间</span>
        <span class="text-amber-200 font-semibold float-right">{{ brewTime }}</span>
      </div>
      <div class="bg-amber-900/20 rounded p-2">
        <span class="text-amber-500/70">粉水比</span>
        <span class="text-amber-200 font-semibold float-right">{{ brewRatio }}</span>
      </div>
    </div>
  </div>
</template>
