<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const props = defineProps({
  screenshots: {
    type: Array,
    default: () => []
  }
})

const ssLightbox = ref(null)
const ssLightboxUrl = computed(() => ssLightbox.value != null ? props.screenshots[ssLightbox.value] : null)

function ssPrev() { if (ssLightbox.value > 0) ssLightbox.value-- }
function ssNext() { if (ssLightbox.value < props.screenshots.length - 1) ssLightbox.value++ }

const ssKeyHandler = (e) => {
  if (e.key === 'ArrowLeft') { e.preventDefault(); ssPrev() }
  else if (e.key === 'ArrowRight') { e.preventDefault(); ssNext() }
  else if (e.key === 'Escape') { ssLightbox.value = null }
}

watch(ssLightbox, (val) => {
  if (val != null) {
    window.addEventListener('keydown', ssKeyHandler)
  } else {
    window.removeEventListener('keydown', ssKeyHandler)
  }
})

onUnmounted(() => {
  window.removeEventListener('keydown', ssKeyHandler)
})
</script>

<template>
  <div v-if="screenshots.length" class="px-8 pt-6 pb-6">
    <div class="flex gap-2 overflow-x-auto hide-scrollbar py-1 pl-1">
      <img
        v-for="(ss, idx) in screenshots"
        :key="idx"
        :src="ss"
        @click="ssLightbox = idx"
        class="shrink-0 w-40 h-24 object-cover rounded-lg cursor-pointer hover:ring-2 hover:ring-blue-400 transition-all opacity-80 hover:opacity-100"
        :class="ssLightbox === idx ? 'ring-2 ring-blue-400 opacity-100' : ''"
      />
    </div>

    <!-- Screenshot lightbox -->
    <Teleport to="body">
      <div v-if="ssLightbox != null" @click.self="ssLightbox = null"
          class="fixed inset-0 z-[100] bg-black/90 flex items-center justify-center select-none">
          <!-- Previous arrow -->
          <button v-if="ssLightbox > 0" @click="ssPrev"
              class="absolute left-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 backdrop-blur-sm flex items-center justify-center transition z-10">
              <el-icon :size="28" class="text-white"><ArrowLeft /></el-icon>
          </button>
          <!-- Image -->
          <img :src="ssLightboxUrl" class="max-w-[90vw] max-h-[88vh] object-contain rounded shadow-2xl cursor-zoom-out" @click="ssLightbox = null" />
          <!-- Next arrow -->
          <button v-if="ssLightbox < screenshots.length - 1" @click="ssNext"
              class="absolute right-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 backdrop-blur-sm flex items-center justify-center transition z-10">
              <el-icon :size="28" class="text-white"><ArrowRight /></el-icon>
          </button>
          <!-- Close hint -->
          <div class="absolute bottom-6 left-1/2 -translate-x-1/2 text-xs text-white/40">
              {{ ssLightbox + 1 }} / {{ screenshots.length }} · 点击关闭 · 键盘 ← →
          </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.hide-scrollbar::-webkit-scrollbar {
    display: none;
}
.hide-scrollbar {
    -ms-overflow-style: none;
    scrollbar-width: none;
}
</style>
