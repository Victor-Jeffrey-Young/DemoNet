<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getUserItems, removeUserItem } from '../api/auth'
import TypeIcon from '../components/TypeIcon.vue'

const router = useRouter()
const auth = useAuthStore()
const items = ref([])
const activeTab = ref('want_to_play')

const statusLabels = {
  want_to_play: '想玩',
  played: '已体验',
  loved: '最爱',
  dropped: '弃了',
}



onMounted(() => {
  auth.fetchUser()
  loadItems()
})

async function loadItems() {
  try {
    items.value = await getUserItems(activeTab.value || undefined)
  } catch (e) {
    console.error(e)
  }
}

function switchTab(tab) {
  activeTab.value = tab
  loadItems()
}

async function handleRemove(itemId) {
  try {
    await removeUserItem(itemId)
    items.value = items.value.filter(i => i.item_id !== itemId)
  } catch (e) {
    console.error(e)
  }
}

function goDetail(slug) {
  if (slug) router.push({ name: 'Detail', params: { slug } })
}
</script>

<template>
  <div class="min-h-[70vh] max-w-3xl mx-auto px-6 py-10">
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold">{{ auth.user?.username }}</h1>
        <p class="text-gray-400 text-sm">{{ auth.user?.email }}</p>
      </div>
      <button @click="auth.logout(); router.push('/')"
        class="text-sm text-gray-400 hover:text-white transition">退出登录</button>
    </div>

    <div class="flex gap-2 mb-6 flex-wrap">
      <button v-for="(label, key) in statusLabels" :key="key"
        @click="switchTab(key)"
        :class="activeTab === key ? 'bg-blue-600 text-white' : 'bg-gray-800 text-gray-400'"
        class="text-sm px-3 py-1.5 rounded-full transition">{{ label }}</button>
    </div>

    <div v-if="items.length === 0" class="text-center text-gray-500 py-20">暂无记录</div>

    <div v-else class="space-y-3">
      <div v-for="item in items" :key="item.id"
        class="bg-gray-900 rounded-xl border border-gray-800 p-4 flex items-center justify-between cursor-pointer hover:border-gray-600 transition"
        @click="goDetail(item.slug)">
        <div class="flex items-center gap-3 min-w-0">
          <span class="text-lg shrink-0"><TypeIcon :type="item.type" size="20" /></span>
          <div class="min-w-0">
            <div class="font-medium truncate">{{ item.title }}</div>
            <div class="text-xs text-gray-500 mt-0.5">{{ statusLabels[item.status] }}</div>
          </div>
        </div>
        <button @click.stop="handleRemove(item.item_id)"
          class="shrink-0 text-gray-500 hover:text-red-400 text-sm ml-3">移除</button>
      </div>
    </div>
  </div>
</template>
