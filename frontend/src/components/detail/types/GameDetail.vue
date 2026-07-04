<script setup>
import { computed, ref } from 'vue'
import { Icon } from '@iconify/vue'
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

const gameFeatures = computed(() => {
  try {
    const f = props.info.features
    if (Array.isArray(f)) return f
    if (typeof f === 'string') return JSON.parse(f)
    return []
  } catch { return [] }
})

const gameDlc = computed(() => {
  try {
    const d = props.info.dlc
    if (Array.isArray(d)) return d
    if (typeof d === 'string') return JSON.parse(d)
    return []
  } catch { return [] }
})

const langFullAudio = ref(false)
const langBasic = ref(false)
const parsedLanguages = computed(() => {
  const raw = props.info.languages
  let items = []
  if (Array.isArray(raw)) items = raw
  else if (typeof raw === 'string') items = raw.split(',').map(s => s.trim()).filter(Boolean)
  
  const fullAudio = []
  const basic = []
  for (const item of items) {
    const name = item.replace('*', '').trim()
    if (item.includes('*')) fullAudio.push(name)
    else basic.push(name)
  }
  return { fullAudio, basic, all: [...basic, ...fullAudio] }
})
const allLanguages = computed(() => parsedLanguages.value.all)
const hasDemo = computed(() => !!props.info.demo_url)
const hasBenchmark = computed(() => !!props.info.benchmark_url)
</script>

<template>
  <div>
    <!-- Action buttons: external link + demo + benchmark -->
    <div v-if="item.externalLink || hasDemo || hasBenchmark" class="flex flex-wrap gap-2 sm:gap-3 mb-6 mt-4 sm:mt-6">
      <a v-if="item.externalLink" :href="item.externalLink" target="_blank"
          class="inline-flex items-center justify-center gap-1.5 sm:gap-2 px-4 sm:px-5 py-2 sm:py-2.5 rounded-lg sm:rounded-xl border border-gray-600 hover:border-gray-400 text-gray-300 hover:text-white text-xs sm:text-sm transition-all flex-1 sm:flex-none">
          <TypeIcon :type="item.type" size="14" class="sm:w-4 sm:h-4" /> 了解更多 →
      </a>
      <a v-if="hasDemo" :href="info.demo_url" target="_blank"
          class="inline-flex items-center justify-center gap-1.5 sm:gap-2 px-4 sm:px-5 py-2 sm:py-2.5 rounded-lg sm:rounded-xl border border-emerald-600 hover:bg-emerald-600 text-emerald-400 hover:text-white text-xs sm:text-sm transition-all flex-1 sm:flex-none">
          <TypeIcon :type="item.type" size="14" class="sm:w-4 sm:h-4" /> 免费试玩
      </a>
      <a v-if="hasBenchmark" :href="info.benchmark_url" target="_blank"
          class="inline-flex items-center justify-center gap-1.5 sm:gap-2 px-4 sm:px-5 py-2 sm:py-2.5 rounded-lg sm:rounded-xl border border-gray-600 hover:border-gray-400 text-gray-300 hover:text-white text-xs sm:text-sm transition-all flex-1 sm:flex-none">
          <TypeIcon :type="item.type" size="14" class="sm:w-4 sm:h-4" /> 性能测试
      </a>
    </div>

    <!-- Steam review count -->
    <div v-if="item.recommendations > 0" class="mb-4 flex items-center gap-2 text-sm">
      <Icon icon="ph:fire" width="16" height="16" class="text-amber-400" />
      <span class="text-gray-500">Steam 好评</span>
      <span class="text-gray-300 font-semibold">{{ item.recommendations.toLocaleString() }}</span>
    </div>

    <!-- Steam: Language Support -->
    <div class="mb-6 mt-6">
      <h4 class="text-xs font-semibold text-gray-300 uppercase tracking-wider mb-3">语言支持</h4>
      <div v-if="allLanguages.length" class="space-y-2">
        <div class="flex items-start gap-3">
            <span class="text-[11px] text-gray-500 w-12 shrink-0">界面</span>
            <div class="text-[13px] text-gray-400 leading-relaxed">
                {{ langBasic ? allLanguages.join(', ') : allLanguages.slice(0, 8).join(', ') }}
                <button v-if="allLanguages.length > 8" @click="langBasic = !langBasic"
                    class="text-blue-400 hover:text-blue-300 ml-1 text-[12px]">{{ langBasic ? '收起' : '等' + allLanguages.length + '种' }}</button>
            </div>
        </div>
        <div class="flex items-start gap-3">
            <span class="text-[11px] text-gray-500 w-12 shrink-0">字幕</span>
            <div class="text-[13px] text-gray-400 leading-relaxed">
                {{ langBasic ? allLanguages.join(', ') : allLanguages.slice(0, 8).join(', ') }}
                <button v-if="allLanguages.length > 8" @click="langBasic = !langBasic"
                    class="text-blue-400 hover:text-blue-300 ml-1 text-[12px]">{{ langBasic ? '收起' : '等' + allLanguages.length + '种' }}</button>
            </div>
        </div>
        <div v-if="parsedLanguages.fullAudio.length" class="flex items-start gap-3">
            <span class="text-[11px] text-gray-500 w-12 shrink-0">音频</span>
            <div class="text-[13px] text-gray-400 leading-relaxed">
                {{ langFullAudio ? parsedLanguages.fullAudio.join(', ') : parsedLanguages.fullAudio.slice(0, 8).join(', ') }}
                <button v-if="parsedLanguages.fullAudio.length > 8" @click="langFullAudio = !langFullAudio"
                    class="text-blue-400 hover:text-blue-300 ml-1 text-[12px]">{{ langFullAudio ? '收起' : '等' + parsedLanguages.fullAudio.length + '种' }}</button>
            </div>
        </div>
      </div>
      <div v-else class="text-[13px] text-gray-500">暂不支持</div>
    </div>

    <!-- Steam: Features -->
    <div v-if="gameFeatures.length" class="mb-6">
      <h4 class="text-xs font-semibold text-gray-300 uppercase tracking-wider mb-2">游戏特性</h4>
      <div class="flex flex-wrap gap-2">
          <span v-for="f in gameFeatures" :key="f" class="text-[11px] px-2 py-1 rounded-full bg-gray-700/50 text-gray-300 border border-gray-600/30">{{ f }}</span>
      </div>
    </div>

    <!-- Steam: DLC -->
    <div v-if="gameDlc.length" class="mb-6">
      <h4 class="text-xs font-semibold text-gray-300 uppercase tracking-wider mb-2">DLC ({{ gameDlc.length }})</h4>
      <div class="flex flex-wrap gap-2">
          <a v-for="d in gameDlc" :key="typeof d === 'object' ? d.id : d"
              :href="'https://store.steampowered.com/app/' + (typeof d === 'object' ? d.id : d)" target="_blank"
              class="text-[11px] px-2 py-1 rounded bg-gray-800/50 text-blue-400 hover:text-blue-300 border border-gray-700/50 hover:border-blue-700/50 transition">
              {{ typeof d === 'object' ? d.name : 'App ' + d }}
          </a>
      </div>
    </div>

    <!-- Steam: System Requirements -->
    <div v-if="info.min_requirements || info.rec_requirements" class="mb-6 mt-6">
      <h4 class="text-sm font-semibold text-gray-200 mb-3">系统配置要求</h4>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div v-if="info.min_requirements" class="bg-gray-800/50 rounded-lg p-4 border border-gray-700/50">
              <div class="text-xs font-medium text-gray-400 mb-2">最低配置</div>
              <div class="text-xs text-gray-300 whitespace-pre-line leading-relaxed">{{ info.min_requirements }}</div>
          </div>
          <div v-if="info.rec_requirements" class="bg-gray-800/50 rounded-lg p-4 border border-gray-700/50">
              <div class="text-xs font-medium text-gray-400 mb-2">推荐配置</div>
              <div class="text-xs text-gray-300 whitespace-pre-line leading-relaxed">{{ info.rec_requirements }}</div>
          </div>
      </div>
    </div>
  </div>
</template>
