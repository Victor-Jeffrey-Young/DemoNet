<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const query = ref('')
let timer = null

function doSearch() {
  clearTimeout(timer)
  timer = setTimeout(() => {
    searchNow()
  }, 300)
}

function searchNow() {
  if (query.value.trim()) {
    router.push({ name: 'Search', query: { keyword: query.value.trim() } })
  }
}

function clear() {
  query.value = ''
  router.push({ name: 'Search' })
}
</script>

<template>
  <div class="relative">
    <input v-model="query" @input="doSearch" @keydown.enter="clearTimeout(timer); searchNow()"
      type="text" placeholder="搜索内容..."
      class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-blue-500 transition pl-10" />
    <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
    </svg>
    <button v-if="query" @click="clear" class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-white">✕</button>
  </div>
</template>
