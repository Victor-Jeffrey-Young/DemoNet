<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { useRoute } from 'vue-router'
import { getSceneBySlug } from '../api/scene'
import TypeIcon from '../components/TypeIcon.vue'
import { getMeta } from '../constants/types'

const route = useRoute()
const scene = ref(null)
const loading = ref(true)
const notFound = ref(false)
const failed = ref(false)

function parseConstraints(value) {
  if (!value) return {}
  if (typeof value === 'object') return value
  try { return JSON.parse(value) } catch { return {} }
}

const constraints = computed(() => parseConstraints(scene.value?.constraintsJson))

function formatRange(range, suffix) {
  if (!range) return ''
  if (range.min != null && range.max != null) return range.min === range.max ? `${range.min}${suffix}` : `${range.min}–${range.max}${suffix}`
  if (range.min != null) return `${range.min}${suffix}起`
  if (range.max != null) return `${range.max}${suffix}内`
  return ''
}

const conditionItems = computed(() => {
  const value = constraints.value
  const locations = { online: '在线', offline: '线下', either: '线上或线下' }
  const location = Array.isArray(value.location_modes)
    ? value.location_modes.map((mode) => locations[mode]).filter(Boolean).join(' / ')
    : ''
  const budget = value.budget?.label || formatRange(value.budget, value.budget?.currency === 'CNY' ? ' 元' : '')
  return [
    { icon: 'lucide:users', label: '人数', value: formatRange(value.people, ' 人') },
    { icon: 'lucide:clock-3', label: '时长', value: formatRange(value.time_minutes, ' 分钟') },
    { icon: 'lucide:map-pin', label: '地点', value: location },
    { icon: 'lucide:wallet-cards', label: '预算', value: budget },
  ].filter((item) => item.value)
})

async function loadScene() {
  loading.value = true
  notFound.value = false
  failed.value = false
  scene.value = null
  try {
    scene.value = await getSceneBySlug(route.params.slug)
  } catch (error) {
    if (error.response?.status === 404) notFound.value = true
    else failed.value = true
  } finally {
    loading.value = false
  }
}

onMounted(loadScene)
watch(() => route.params.slug, loadScene)
</script>

<template>
  <main class="min-h-screen bg-gray-950 text-white">
    <div class="max-w-6xl mx-auto px-4 py-8 sm:px-6 sm:py-12">
      <nav aria-label="面包屑" class="mb-6 text-sm">
        <router-link to="/" class="text-gray-400 hover:text-white focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400 rounded">首页</router-link>
        <span class="mx-2 text-gray-600" aria-hidden="true">/</span>
        <span class="text-gray-300">场景策展</span>
      </nav>

      <section v-if="loading" aria-busy="true" aria-label="正在加载场景" class="space-y-5">
        <div class="h-8 w-2/3 rounded bg-gray-800 animate-pulse"></div>
        <div class="h-20 max-w-2xl rounded bg-gray-900 animate-pulse"></div>
        <div class="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <div v-for="index in 4" :key="index" class="h-20 rounded-lg border border-gray-800 bg-gray-900 animate-pulse"></div>
        </div>
      </section>

      <section v-else-if="notFound" class="max-w-xl py-14 text-center mx-auto" role="status">
        <Icon icon="lucide:map-off" class="mx-auto mb-4 text-amber-400" width="40" aria-hidden="true" />
        <h1 class="text-2xl font-bold">这个场景暂时不可用</h1>
        <p class="mt-3 text-sm leading-6 text-gray-400">它可能还在策展、已被隐藏，或链接地址有误。你可以先从首页继续找灵感。</p>
        <router-link to="/" class="inline-flex mt-6 rounded-lg bg-amber-500 px-4 py-2 text-sm font-semibold text-gray-950 hover:bg-amber-400 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-200">返回首页</router-link>
      </section>

      <section v-else-if="failed" class="max-w-xl py-14 text-center mx-auto" role="alert">
        <Icon icon="lucide:cloud-off" class="mx-auto mb-4 text-gray-400" width="40" aria-hidden="true" />
        <h1 class="text-2xl font-bold">场景暂时加载失败</h1>
        <p class="mt-3 text-sm leading-6 text-gray-400">没有替你猜测内容；请检查网络后重试。</p>
        <button type="button" @click="loadScene" class="mt-6 rounded-lg border border-gray-600 px-4 py-2 text-sm font-semibold text-gray-100 hover:border-gray-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400">重新加载</button>
      </section>

      <template v-else-if="scene">
        <header class="border-b border-gray-800 pb-7 sm:pb-9">
          <p class="text-xs font-semibold tracking-[0.18em] text-amber-400 uppercase">场景策展</p>
          <h1 class="mt-3 text-3xl font-bold tracking-tight sm:text-4xl">{{ scene.title }}</h1>
          <p class="mt-3 max-w-3xl text-base leading-7 text-gray-300">{{ scene.description }}</p>
          <p v-if="constraints.promise" class="mt-4 border-l-2 border-amber-400 pl-3 text-sm leading-6 text-amber-100">{{ constraints.promise }}</p>
        </header>

        <section v-if="conditionItems.length || constraints.requirements?.length" aria-labelledby="conditions-title" class="py-7 border-b border-gray-800">
          <h2 id="conditions-title" class="text-lg font-semibold">先确认这些条件</h2>
          <div v-if="conditionItems.length" class="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
            <div v-for="item in conditionItems" :key="item.label" class="rounded-lg border border-gray-800 bg-gray-900 px-3 py-3">
              <Icon :icon="item.icon" class="text-amber-400" width="18" aria-hidden="true" />
              <p class="mt-2 text-xs text-gray-500">{{ item.label }}</p>
              <p class="mt-1 text-sm font-medium text-gray-100">{{ item.value }}</p>
            </div>
          </div>
          <ul v-if="constraints.requirements?.length" class="mt-4 flex flex-wrap gap-2" aria-label="准备条件">
            <li v-for="requirement in constraints.requirements" :key="requirement" class="rounded-full border border-gray-700 px-3 py-1 text-xs text-gray-300">{{ requirement }}</li>
          </ul>
        </section>

        <section aria-labelledby="choices-title" class="pt-7">
          <div class="flex items-baseline justify-between gap-4">
            <div><h2 id="choices-title" class="text-xl font-semibold">可从这里开始</h2><p class="mt-1 text-sm text-gray-400">每项都说明了它为何适合这个场景。</p></div>
            <span class="shrink-0 text-sm text-gray-500">{{ scene.items?.length || 0 }} 项</span>
          </div>
          <div v-if="scene.items?.length" class="mt-5 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <router-link v-for="item in scene.items" :key="item.id" :to="{ name: 'Detail', params: { slug: item.slug } }" class="group overflow-hidden rounded-xl border border-gray-800 bg-gray-900 transition hover:-translate-y-0.5 hover:border-gray-600 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400">
              <div class="aspect-video bg-gray-800 relative">
                <img v-if="item.wideCoverUrl || item.coverUrl" :src="item.wideCoverUrl || item.coverUrl" :alt="item.title + ' 封面'" class="h-full w-full object-cover transition duration-300 group-hover:scale-105">
                <div v-else class="h-full flex items-center justify-center"><TypeIcon :type="item.type" size="32" /></div>
              </div>
              <div class="p-4"><span class="text-xs text-amber-300">{{ getMeta(item.type)?.label || item.type }}</span><h3 class="mt-1 font-semibold text-gray-100 group-hover:text-amber-300">{{ item.title }}</h3><p v-if="item.reason" class="mt-2 text-sm leading-6 text-gray-400">{{ item.reason }}</p></div>
            </router-link>
          </div>
          <div v-else class="mt-5 rounded-lg border border-dashed border-gray-700 px-5 py-10 text-center" role="status"><p class="font-medium text-gray-200">这个场景正在补充经过核验的内容</p><p class="mt-2 text-sm text-gray-500">为了避免推荐凑数，准备完成前不会展示未经确认的条目。</p></div>
        </section>
      </template>
    </div>
  </main>
</template>
