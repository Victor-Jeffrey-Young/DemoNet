<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({ item: { type: Object, required: true } })
const router = useRouter()
function go() { router.push({ name: 'Detail', params: { slug: props.item.slug } }) }

const info = computed(() => {
  try { return typeof props.item.infoJson === 'string' ? JSON.parse(props.item.infoJson) : (props.item.infoJson || {}) } catch { return {} }
})
const origin = computed(() => info.value.origin || '')
const roast = computed(() => info.value.roast || '')
const process = computed(() => info.value.process || '')
const scaScore = computed(() => info.value.total_cup_points || null)
const flavor = computed(() => {
  const raw = info.value.flavor_notes || info.value.flavor || ''
  if (!raw) return []
  return raw.split(',').map(f => f.trim()).filter(Boolean)
})

const roastDots = { '浅烘':'○', '中浅烘':'◔', '中烘':'◑', '中深烘':'◕', '深烘':'●' }
const flavorEmojis = {
  '柑橘':'🍊','茉莉':'🌸','蜂蜜':'🍯','柠檬酸':'🍋','焦糖':'🍮','坚果':'🥜','可可':'🍫',
  '巧克力':'🍫','烟熏':'🔥','香料':'🌿','乌梅':'🫐','番茄':'🍅','黑加仑':'🍇','草药':'🌱',
  '黑巧':'🍫','杏桃':'🍑','太妃糖':'🍬','花香':'💐','佛手柑':'🍊','蜜桃':'🍑','茶感':'🍵',
  '红酒':'🍷','菠萝蜜':'🍍','黑糖':'🍯','热带果':'🥭','醇厚':'☕'
}
function flavorEmoji(f) {
  for (const [k, v] of Object.entries(flavorEmojis))
    if (f.includes(k)) return v
  return '☕'
}
</script>

<template>
  <div @click="go" class="group cursor-pointer flex flex-col border-4 border-amber-900/40 rounded-lg bg-gradient-to-b from-[#c4a97d] to-[#b8956a] hover:border-orange-400/60 hover:-translate-y-1 transition-all duration-300 shadow-lg hover:shadow-2xl hover:shadow-amber-900/20 overflow-hidden">
    <!-- Coffee image -->
    <div class="aspect-square relative overflow-hidden">
      <div v-if="item.wideCoverUrl || item.coverUrl"
        class="absolute inset-0 bg-cover bg-center group-hover:scale-105 transition-transform duration-500"
        :style="{ backgroundImage: `url(${item.wideCoverUrl || item.coverUrl})` }" />
      <div v-else class="absolute inset-0 flex items-center justify-center text-4xl" style="background: radial-gradient(circle at 30% 30%, #d4c5a9, #a08060)">☕</div>
      <!-- Roast indicator -->
      <div v-if="roast" class="absolute top-2 right-2 bg-black/40 backdrop-blur-sm rounded-full px-2.5 py-1 text-[11px] text-amber-200 font-medium">
        {{ roastDots[roast] || '○' }} {{ roast }}
      </div>
      <!-- SCA score badge -->
      <div v-if="scaScore" class="absolute top-2 left-2 bg-amber-500/90 backdrop-blur-sm rounded-full px-2 py-0.5 text-[11px] text-amber-950 font-bold">
        ⭐ {{ scaScore }}
      </div>
      <!-- Hover label tear -->
      <div class="absolute inset-0 bg-gradient-to-t from-amber-900/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex items-end justify-center pb-3">
        <span class="text-amber-100 text-xs font-medium tracking-wider">查看风味 →</span>
      </div>
    </div>

    <!-- Info label -->
    <div class="px-3 py-3 bg-[#f5e6c8] flex-1">
      <p v-if="origin" class="text-[10px] text-amber-700 uppercase tracking-widest font-semibold mb-0.5">{{ origin }}</p>
      <h3 class="text-sm font-bold text-gray-900 group-hover:text-orange-700 transition-colors truncate">{{ item.title }}</h3>
      <p v-if="process" class="text-[10px] text-amber-600 mt-0.5">{{ process }}</p>
      <div v-if="flavor.length" class="flex flex-wrap gap-1 mt-1.5">
        <span v-for="f in flavor.slice(0,3)" :key="f" class="text-[10px] px-1.5 py-0.5 rounded-full bg-amber-100 text-amber-800 border border-amber-200/50">
          {{ flavorEmoji(f) }} {{ f }}
        </span>
      </div>
    </div>
  </div>
</template>
