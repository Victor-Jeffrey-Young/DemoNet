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
          class="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-fuchsia-600 hover:bg-fuchsia-500 text-white font-semibold text-sm transition-colors shadow-lg shadow-fuchsia-900/20">
          <TypeIcon :type="item.type" size="16" /> 在 Apple Music 中打开
      </a>
    </div>

    <!-- Music: Audio preview (already handled in MediaHero mostly, but keep here just in case they want a separate player below) -->
    <div v-if="info.preview_url" class="mt-4 p-4 bg-fuchsia-900/10 border border-fuchsia-500/10 rounded-lg">
        <div class="flex items-center gap-3">
            <span class="text-xs text-fuchsia-400 font-medium"><TypeIcon :type="item.type" size="14" /> 试听预览</span>
            <audio controls :src="info.preview_url" class="h-8 w-full max-w-md" style="accent-color: #d946ef" />
        </div>
    </div>
  </div>
</template>
