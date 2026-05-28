<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getFeatured, getHotItems } from '../../api/item'
import { getMeta } from '../../constants/types'

const router = useRouter()
const meta = getMeta('coffee')
const items = ref([])
const activeIdx = ref(0)
let autoTimer = null

onMounted(async () => {
  try {
    const featured = await getFeatured({ type: 'coffee' }) || []
    items.value = featured.length ? featured : (await getHotItems({ type: 'coffee', limit: 6 })) || []
  } catch {}
  if (items.value.length > 1) startAutoPlay()
})

onUnmounted(() => clearInterval(autoTimer))

function startAutoPlay() {
  clearInterval(autoTimer)
  autoTimer = setInterval(() => { activeIdx.value = (activeIdx.value + 1) % items.value.length }, 5000)
}
function resetAutoPlay() {
  clearInterval(autoTimer)
  startAutoPlay()
}

function parseInfo(item) {
  try { return JSON.parse(item.infoJson || '{}') } catch { return {} }
}

const activeItem = computed(() => items.value[activeIdx.value] || {})
const activeInfo = computed(() => parseInfo(activeItem.value))
const activeFlavor = computed(() => {
  const raw = activeInfo.value.flavor || ''
  if (!raw) return []
  return raw.split(',').map(f => f.trim()).filter(Boolean)
})

const flavorEmojis = {
  '柑橘':'🍊','茉莉':'🌸','蜂蜜':'🍯','柠檬酸':'🍋','焦糖':'🍮','坚果':'🥜','可可':'🍫',
  '巧克力':'🍫','烟熏':'🔥','香料':'🌿','乌梅':'🫐','番茄':'🍅','黑加仑':'🍇','草药':'🌱',
  '黑巧':'🍫','杏桃':'🍑','太妃糖':'🍬','花香':'💐','佛手柑':'🍊','蜜桃':'🍑','茶感':'🍵',
  '红酒':'🍷','菠萝蜜':'🍍','黑糖':'🍯','热带果':'🥭','醇厚':'☕','明亮酸':'🍋','顺滑':'💧'
}
function flavorEmoji(f) {
  for (const [k, v] of Object.entries(flavorEmojis))
    if (f.includes(k)) return v
  return '☕'
}

function prev() { activeIdx.value = (activeIdx.value - 1 + items.value.length) % items.value.length; resetAutoPlay() }
function next() { activeIdx.value = (activeIdx.value + 1) % items.value.length; resetAutoPlay() }
function goTo(i) { activeIdx.value = i; resetAutoPlay() }
function goDetail() { router.push({ name: 'Detail', params: { slug: activeItem.value.slug } }) }
</script>

<template>
  <div v-if="items.length" class="relative overflow-hidden border-b-8 border-amber-900/60" style="background: linear-gradient(175deg, #1a3a2a 0%, #0f2818 40%, #1a2e1a 100%)">
    <!-- Chalk dust texture overlay -->
    <div class="absolute inset-0 opacity-5 pointer-events-none" style="background: repeating-linear-gradient(30deg, transparent, transparent 3px, rgba(255,255,255,0.3) 3px, rgba(255,255,255,0.3) 4px)" />

    <div class="max-w-4xl mx-auto px-6 py-12">
      <!-- Header: chalkboard style -->
      <div class="text-center mb-8">
        <p class="text-amber-200/80 text-sm font-serif italic tracking-wide mb-1">☕ Today's Recommendation</p>
        <div class="w-32 h-[1px] bg-amber-400/30 mx-auto" />
      </div>

      <div class="flex flex-col items-center">
        <!-- Product image -->
        <div class="relative w-full max-w-lg mb-8 cursor-pointer" @click="goDetail">
          <Transition name="chalk-fade" mode="out-in">
            <div :key="activeItem.id" class="aspect-[4/3] rounded-lg overflow-hidden border-4 border-amber-900/40 flex items-center justify-center" style="background: linear-gradient(135deg, #3d2b1f, #5c3d2e)">
              <img v-if="activeItem.wideCoverUrl || activeItem.coverUrl"
                :src="activeItem.wideCoverUrl || activeItem.coverUrl"
                class="w-full h-full object-cover p-2 rounded hover:scale-[1.02] transition-transform duration-700" />
              <span v-else class="text-5xl">☕</span>
            </div>
          </Transition>
        </div>

        <!-- Chalkboard info -->
        <div class="w-full max-w-lg text-center">
          <h2 class="text-2xl font-serif italic text-amber-100 mb-2" style="text-shadow: 0 1px 3px rgba(0,0,0,0.5)">{{ activeItem.title }}</h2>
          <p class="text-amber-300/70 text-sm font-serif italic">
            {{ activeInfo.origin }}<span v-if="activeInfo.process"> · {{ activeInfo.process }}</span><span v-if="activeInfo.roast"> · {{ activeInfo.roast }}</span>
          </p>
          <div v-if="activeFlavor.length" class="flex flex-wrap justify-center gap-2 mt-4">
            <span v-for="f in activeFlavor" :key="f" class="text-xs px-3 py-1 rounded-full border border-amber-500/30 text-amber-200 bg-amber-900/30">
              {{ flavorEmoji(f) }} {{ f }}
            </span>
          </div>
        </div>

        <!-- Controls -->
        <div class="flex items-center gap-5 mt-8">
          <button @click="prev" class="w-9 h-9 rounded-full flex items-center justify-center border border-amber-500/30 text-amber-400/60 hover:text-amber-200 hover:border-amber-400/60 transition-colors">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
          </button>
          <div class="flex gap-2.5">
            <button v-for="(_, i) in items" :key="i" @click="goTo(i)"
              :class="i === activeIdx ? 'w-6 h-2 bg-orange-400' : 'w-2 h-2 bg-amber-500/30 hover:bg-amber-500/50'"
              class="rounded-full transition-all duration-300" />
          </div>
          <button @click="next" class="w-9 h-9 rounded-full flex items-center justify-center border border-amber-500/30 text-amber-400/60 hover:text-amber-200 hover:border-amber-400/60 transition-colors">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
          </button>
        </div>

        <!-- Chalk decor: bottom -->
        <div class="flex items-center gap-4 mt-8 text-amber-500/20 text-xs">
          <span>✏️</span>
          <span class="w-20 h-[1px] bg-amber-500/10" />
          <span>🧹</span>
          <span class="w-20 h-[1px] bg-amber-500/10" />
          <span>✏️</span>
        </div>
      </div>
    </div>
  </div>
  <div v-else />
</template>

<style scoped>
.chalk-fade-enter-active, .chalk-fade-leave-active { transition: opacity 0.6s ease; }
.chalk-fade-enter-from, .chalk-fade-leave-to { opacity: 0; }
</style>
