<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const categories = [
  { key: 'game', label: '游戏' },
  { key: 'movie', label: '电影' },
  { key: 'anime', label: '动漫' },
  { key: 'boardgame', label: '桌游' },
  { key: 'model', label: '模型' },
  { key: 'book', label: '书籍' },
  { key: 'music', label: '音乐' },
  { key: 'digital', label: '数码' },
]
</script>

<template>
  <nav class="bg-gray-950 border-b border-gray-800 sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
      <router-link to="/" class="text-xl font-bold tracking-tight hover:text-blue-400 transition">
        DemoNet
      </router-link>
      <div class="flex items-center gap-6">
        <router-link
          v-for="cat in categories"
          :key="cat.key"
          :to="`/list/${cat.key}`"
          class="text-sm text-gray-400 hover:text-white transition hidden md:block"
        >
          {{ cat.label }}
        </router-link>
        <template v-if="auth.isLoggedIn">
          <router-link to="/profile" class="text-sm text-blue-400 hover:text-blue-300 transition">
            {{ auth.user?.username || '我的' }}
          </router-link>
        </template>
        <template v-else>
          <router-link to="/login" class="text-sm text-blue-400 hover:text-blue-300 transition">登录</router-link>
          <router-link to="/register"
            class="text-sm bg-blue-600 hover:bg-blue-500 text-white px-3 py-1 rounded-lg transition">注册</router-link>
        </template>
      </div>
    </div>
  </nav>
</template>
