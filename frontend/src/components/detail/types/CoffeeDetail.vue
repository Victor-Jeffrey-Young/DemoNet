<script setup>
import { computed } from 'vue'
import TypeIcon from '../../TypeIcon.vue'
import ScaRadarChart from '../widgets/ScaRadarChart.vue'
import BrewCalculator from '../widgets/BrewCalculator.vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  info: {
    type: Object,
    default: () => ({})
  }
})

const coffeeFlavors = computed(() => {
  const raw = props.info.flavor_notes || props.info.flavor
  if (!raw) return []
  if (Array.isArray(raw)) return raw
  return typeof raw === 'string' ? raw.split(",").map((f) => f.trim()).filter(Boolean) : []
})

const hasScaScores = computed(() => {
  return typeof props.info.total_cup_points === 'number' && props.info.total_cup_points > 0
})
</script>

<template>
  <div>
    <!-- Action buttons: external link -->
    <div v-if="item.externalLink" class="flex gap-3 mb-6 mt-6">
      <a :href="item.externalLink" target="_blank"
          class="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl border border-gray-600 hover:border-gray-400 text-gray-300 hover:text-white text-sm transition-all">
          <TypeIcon :type="item.type" size="16" /> 了解更多 →
      </a>
    </div>

    <!-- Coffee: Flavor notes -->
    <div v-if="coffeeFlavors.length" class="mt-4 p-4 bg-amber-900/10 border border-amber-500/10 rounded-lg">
      <div class="flex flex-wrap items-center gap-2">
          <span class="text-xs text-amber-400 font-medium mr-2">
              <TypeIcon :type="item.type" size="14" /> 风味</span
          >
          <span
              v-for="f in coffeeFlavors"
              :key="f"
              class="text-xs px-2.5 py-1 rounded-full bg-amber-500/10 text-amber-300 border border-amber-500/20"
              >{{ f }}</span
          >
      </div>
    </div>

    <!-- Coffee: SCA Radar Chart -->
    <ScaRadarChart v-if="hasScaScores" :info="info" />

    <!-- Coffee: Brew Guide & Calculator -->
    <BrewCalculator />
  </div>
</template>
