<script setup>
import { computed, ref, watch } from 'vue'
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

const showBoardgameRules = ref(false)
const rulePage = ref(0)
const ruleFlipAnim = ref('next')
const ruleThumbs = ref(null)
const lightboxPage = ref(null)

const ruleImages = computed(() => {
  const raw = props.info.rule_images
  if (!raw) return []
  if (Array.isArray(raw)) return raw
  return typeof raw === 'string' ? raw.split(',').map(u => u.trim()).filter(Boolean) : []
})
const ruleHasContent = computed(() => props.info.rule_text || ruleImages.value.length > 0)
const lightboxSrc = computed(() => lightboxPage.value != null ? ruleImages.value[lightboxPage.value] : null)

function openLightbox(idx) { lightboxPage.value = idx }
function closeLightbox() { lightboxPage.value = null }
function lightboxPrev() { if (lightboxPage.value > 0) lightboxPage.value-- }
function lightboxNext() { if (lightboxPage.value < ruleImages.value.length - 1) lightboxPage.value++ }

function onLightboxKey(e) {
  if (e.key === 'ArrowLeft') { e.preventDefault(); lightboxPrev() }
  else if (e.key === 'ArrowRight') { e.preventDefault(); lightboxNext() }
  else if (e.key === 'Escape') { e.preventDefault(); closeLightbox() }
}

watch(lightboxPage, (val) => {
  if (val != null) window.addEventListener('keydown', onLightboxKey)
  else window.removeEventListener('keydown', onLightboxKey)
})

function ruleNext() { if (rulePage.value < ruleImages.value.length - 1) { ruleFlipAnim.value = 'next'; rulePage.value++; scrollThumb(); } }
function rulePrev() { if (rulePage.value > 0) { ruleFlipAnim.value = 'prev'; rulePage.value--; scrollThumb(); } }
function scrollThumb() {
  if (!ruleThumbs.value) return
  const el = ruleThumbs.value.children[rulePage.value]
  if (el) el.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' })
}
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

    <!-- Boardgame: Rulebook viewer -->
    <div v-if="ruleHasContent" class="mt-4 bg-amber-900/10 border border-amber-500/10 rounded-lg overflow-hidden">
        <button
            @click="showBoardgameRules = !showBoardgameRules; if(showBoardgameRules) rulePage = 0"
            class="w-full flex items-center justify-between p-4 text-left hover:bg-amber-900/10 transition-colors"
        >
            <span class="text-xs text-amber-400 font-medium flex items-center gap-2">
                <TypeIcon :type="item.type" size="16" /> 规则书
                <span v-if="ruleImages.length" class="text-amber-600 font-normal">({{ ruleImages.length }} 页)</span>
                <span class="text-amber-600 font-normal">(点击展开)</span>
            </span>
            <svg :class="showBoardgameRules ? 'rotate-180' : ''" class="w-3.5 h-3.5 text-amber-500 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
        </button>

        <div v-show="showBoardgameRules" class="border-t border-amber-500/10">
            <!-- Page viewer -->
            <div v-if="ruleImages.length" class="px-4 py-4">
                <div class="relative mx-auto max-w-2xl">
                    <div class="rounded-lg overflow-hidden shadow-2xl shadow-amber-900/20 flex items-center justify-center min-h-[200px]"
                        :style="{ background: 'linear-gradient(135deg, #1a1206, #2d1a05)' }">
                        <Transition :name="ruleFlipAnim === 'next' ? 'page-next' : 'page-prev'" mode="out-in">
                            <img :key="rulePage" :src="ruleImages[rulePage]"
                                class="max-h-[60vh] max-w-full object-contain cursor-zoom-in"
                                @click="openLightbox(rulePage)"
                                :style="{ aspectRatio: 'auto' }" />
                        </Transition>
                    </div>

                    <!-- Navigation arrows -->
                    <button v-if="rulePage > 0" @click="rulePrev"
                        class="absolute left-2 top-1/2 -translate-y-1/2 w-7 h-7 rounded-full bg-black/30 hover:bg-amber-900/60 flex items-center justify-center text-amber-300/60 hover:text-amber-200 transition-all">
                        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
                    </button>
                    <button v-if="rulePage < ruleImages.length - 1" @click="ruleNext"
                        class="absolute right-2 top-1/2 -translate-y-1/2 w-7 h-7 rounded-full bg-black/30 hover:bg-amber-900/60 flex items-center justify-center text-amber-300/60 hover:text-amber-200 transition-all">
                        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
                    </button>

                    <div class="text-center mt-2">
                        <span class="text-xs text-amber-500 font-mono">第 {{ rulePage + 1 }} 页 / 共 {{ ruleImages.length }} 页</span>
                    </div>
                </div>

                <!-- Thumbnail strip -->
                <div ref="ruleThumbs" class="rule-thumb-strip flex gap-2 mt-4 overflow-x-auto pb-2 justify-center">
                    <img
                        v-for="(src, i) in ruleImages" :key="i" :src="src"
                        @click="ruleFlipAnim = i > rulePage ? 'next' : 'prev'; rulePage = i"
                        :class="i === rulePage ? 'ring-2 ring-amber-400 opacity-100' : 'opacity-50 hover:opacity-80 ring-amber-400/0'"
                        class="w-14 h-20 object-cover rounded cursor-pointer shrink-0 transition-all hover:ring-1"
                    />
                </div>
            </div>

            <!-- Rule text -->
            <div v-if="info.rule_text" class="px-4 pt-3 pb-4">
                <pre class="text-xs text-amber-200/80 whitespace-pre-wrap font-sans leading-relaxed overflow-auto max-h-64 bg-amber-900/20 rounded-lg p-3">{{ info.rule_text }}</pre>
            </div>
        </div>
    </div>

    <!-- Rule image lightbox -->
    <Teleport to="body">
        <div v-if="lightboxPage != null" @click.self="closeLightbox" class="fixed inset-0 z-50 bg-black/95 flex items-center justify-center">
            <button @click.stop="lightboxPrev" v-if="lightboxPage > 0"
                class="absolute left-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 border border-white/20 flex items-center justify-center text-white/80 hover:text-white transition-all">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
            </button>
            <button @click.stop="lightboxNext" v-if="lightboxPage < ruleImages.length - 1"
                class="absolute right-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 border border-white/20 flex items-center justify-center text-white/80 hover:text-white transition-all">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
            </button>
            <img :src="lightboxSrc" class="max-w-[85vw] max-h-[88vh] object-contain rounded shadow-2xl" />
            <div class="absolute bottom-6 text-white/50 text-sm font-mono">{{ lightboxPage + 1 }} / {{ ruleImages.length }}</div>
            <button @click.stop="closeLightbox" class="absolute top-4 right-4 text-white/60 hover:text-white text-2xl leading-none">✕</button>
        </div>
    </Teleport>
  </div>
</template>

<style scoped>
.page-next-enter-active,
.page-next-leave-active,
.page-prev-enter-active,
.page-prev-leave-active {
  transition: all 0.35s ease;
}
.page-next-enter-from {
  opacity: 0;
  transform: translateX(60px) rotateY(-10deg) scale(0.95);
}
.page-next-leave-to {
  opacity: 0;
  transform: translateX(-60px) rotateY(10deg) scale(0.95);
}
.page-prev-enter-from {
  opacity: 0;
  transform: translateX(-60px) rotateY(10deg) scale(0.95);
}
.page-prev-leave-to {
  opacity: 0;
  transform: translateX(60px) rotateY(-10deg) scale(0.95);
}
.rule-thumb-strip::-webkit-scrollbar {
  height: 4px;
}
.rule-thumb-strip::-webkit-scrollbar-track {
  background: transparent;
}
.rule-thumb-strip::-webkit-scrollbar-thumb {
  background: rgba(217, 119, 6, 0.3);
  border-radius: 4px;
}
.rule-thumb-strip::-webkit-scrollbar-thumb:hover {
  background: rgba(217, 119, 6, 0.5);
}
</style>
