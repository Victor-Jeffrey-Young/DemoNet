<script setup>
import { useRouter } from 'vue-router'
import { computed } from 'vue'

const props = defineProps({ item: { type: Object, required: true } })
const router = useRouter()

function go() { router.push({ name: 'Detail', params: { slug: props.item.slug } }) }

const info = computed(() => {
  try { return typeof props.item.infoJson === 'string' ? JSON.parse(props.item.infoJson) : (props.item.infoJson || {}) } catch { return {} }
})
const year = computed(() => info.value.year || '')
const director = computed(() => info.value.director || '')
const duration = computed(() => info.value.duration || '')
const genre = computed(() => info.value.genre || '')
</script>

<template>
  <div
    class="group cursor-pointer"
    style="perspective: 1200px; width: 100%; max-width: 280px; margin: 0 auto;"
    @click="go"
  >
    <div
      class="relative w-full aspect-[2/3] transition-transform duration-[0.8s] ease-[cubic-bezier(0.175,0.885,0.32,1.275)] group-hover:[transform:rotateY(-180deg)]"
      style="transform-style: preserve-3d;"
    >
      <!-- Front: Poster -->
      <div
        class="absolute inset-0 rounded-xl overflow-hidden"
        style="backface-visibility: hidden; -webkit-backface-visibility: hidden;"
      >
        <div
          v-if="item.coverUrl"
          class="w-full h-full bg-cover bg-center"
          :style="{ backgroundImage: `url(${item.coverUrl})` }"
        />
        <div v-else class="w-full h-full bg-gradient-to-br from-red-900 to-gray-900 flex items-center justify-center text-5xl">
          🎬
        </div>
        <!-- Gradient overlay + title -->
        <div class="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/90 via-black/40 to-transparent pt-16 pb-4 px-3">
          <h3 class="text-white text-sm font-bold leading-snug line-clamp-2 text-shadow">{{ item.title }}</h3>
          <span v-if="year" class="text-red-400 text-xs mt-1 block">{{ year }}</span>
        </div>
        <!-- Glossy reflection line -->
        <div class="absolute inset-0 rounded-xl overflow-hidden pointer-events-none"
          style="background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, transparent 50%, rgba(255,255,255,0.05) 100%);" />
      </div>

      <!-- Back: Info panel -->
      <div
        class="absolute inset-0 rounded-xl overflow-hidden bg-gray-900 border border-red-500/20 flex flex-col justify-between p-4"
        style="backface-visibility: hidden; -webkit-backface-visibility: hidden; transform: rotateY(180deg); box-shadow: 0 20px 60px rgba(0,0,0,0.5);"
      >
        <!-- Top accent bar -->
        <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-red-500 via-red-600 to-red-400" />

        <div>
          <h3 class="text-white font-bold text-sm leading-snug mb-2 line-clamp-2">{{ item.title }}</h3>

          <div v-if="year || duration" class="flex items-center gap-2 text-xs text-gray-400 mb-2">
            <span v-if="year">{{ year }}</span>
            <span v-if="duration">· {{ duration }}</span>
          </div>

          <div v-if="director" class="text-xs text-gray-300 mb-1 flex items-center gap-1">
            <span class="text-red-400/60">导演</span> {{ director }}
          </div>

          <div v-if="genre" class="flex flex-wrap gap-1 mt-2">
            <span
              v-for="g in genre.split(/[,，]\s*/)" :key="g"
              class="text-[10px] px-1.5 py-0.5 rounded bg-red-900/30 text-red-300 border border-red-500/15"
            >{{ g }}</span>
          </div>

          <p class="text-gray-400 text-[11px] leading-relaxed mt-3 line-clamp-4">{{ item.description }}</p>
        </div>

        <span class="text-red-400 text-xs mt-2 flex items-center gap-1 group-hover:gap-2 transition-all">
          查看详情 <span class="inline-block transition-transform">→</span>
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.text-shadow {
  text-shadow: 0 1px 4px rgba(0,0,0,0.8);
}
</style>
