<script setup>
import { computed } from 'vue'
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

const platformButtons = computed(() => {
  const links = props.info?.links || {}
  const result = []
  if (links.appleMusic) result.push({ label: 'Apple Music', url: links.appleMusic, color: 'fuchsia' })
  if (links.spotify) result.push({ label: 'Spotify', url: links.spotify, color: 'green' })
  if (links.qqMusic) result.push({ label: 'QQ音乐', url: links.qqMusic, color: 'blue' })
  return result
})
</script>

<template>
  <div>
    <div v-if="platformButtons.length > 0" class="flex gap-3 flex-wrap mb-6 mt-6">
      <a v-for="btn in platformButtons" :key="btn.label" :href="btn.url" target="_blank"
          class="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl text-white font-semibold text-sm transition-colors shadow-lg"
          :class="btn.color === 'fuchsia' ? 'bg-fuchsia-600 hover:bg-fuchsia-500 shadow-fuchsia-900/20' : btn.color === 'green' ? 'bg-green-700 hover:bg-green-600 shadow-green-900/20' : 'bg-blue-700 hover:bg-blue-600 shadow-blue-900/20'">
          <TypeIcon :type="item.type" size="16" /> 在 {{ btn.label }} 中打开
      </a>
    </div>

    <div v-if="info.preview_url" class="mt-4 p-4 bg-fuchsia-900/10 border border-fuchsia-500/10 rounded-lg">
        <div class="flex items-center gap-3">
            <span class="text-xs text-fuchsia-400 font-medium"><TypeIcon :type="item.type" size="14" /> 试听预览</span>
            <audio controls :src="info.preview_url" class="h-8 w-full max-w-md" style="accent-color: #d946ef" />
        </div>
    </div>
  </div>
</template>
