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
  </div>
</template>
