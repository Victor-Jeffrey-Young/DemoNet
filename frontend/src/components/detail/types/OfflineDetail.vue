<script setup>
import TypeIcon from '../../TypeIcon.vue'

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

    <!-- Offline: Event detail card -->
    <div class="mt-4 p-4 bg-indigo-900/10 border border-indigo-500/10 rounded-lg">
        <div class="flex items-center gap-2 mb-3">
            <span class="text-xs text-indigo-400 font-medium">📋 活动详情</span>
        </div>
        <div class="space-y-2">
            <p v-if="info.venue" class="text-xs text-gray-300 flex items-center gap-2">
                <span class="text-indigo-400">📍</span> {{ info.venue }}
            </p>
            <p v-if="info.date" class="text-xs text-gray-300 flex items-center gap-2">
                <span class="text-indigo-400">📅</span> {{ info.date }}
            </p>
            <p v-if="info.time" class="text-xs text-gray-300 flex items-center gap-2">
                <span class="text-indigo-400">🕐</span> {{ info.time }}
            </p>
            <p v-if="info.price" class="text-xs text-gray-300 flex items-center gap-2">
                <span class="text-indigo-400">💰</span> {{ info.price }}
            </p>
            <p v-if="info.capacity" class="text-xs text-gray-300 flex items-center gap-2">
                <span class="text-indigo-400">👥</span> {{ info.capacity }}
            </p>
            <p v-if="info.difficulty" class="text-xs text-gray-300 flex items-center gap-2">
                <span class="text-indigo-400">⭐</span> {{ info.difficulty }}
            </p>
        </div>
        <div v-if="info.highlights" class="flex flex-wrap gap-1.5 mt-3 pt-3 border-t border-indigo-500/10">
            <span class="text-[10px] text-indigo-400">✨</span>
            <span
                v-for="h in (Array.isArray(info.highlights) ? info.highlights : (typeof info.highlights === 'string' ? info.highlights.split(',') : [])).map((s) => s.trim()).filter(Boolean)"
                :key="h"
                class="text-xs px-2 py-0.5 rounded-full bg-indigo-500/10 text-indigo-300 border border-indigo-500/20"
                >{{ h }}</span
            >
        </div>
    </div>
  </div>
</template>
