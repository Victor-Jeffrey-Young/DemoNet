<script setup>
import { useAuthStore } from '../stores/auth'
import { TYPE_LIST, getMeta } from '../constants/types'

const auth = useAuthStore()
</script>

<template>
  <nav class="bg-gray-950/95 backdrop-blur-sm border-b border-gray-800/50 sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
      <div class="flex items-center gap-1">
        <router-link to="/" class="text-lg font-black tracking-tight mr-3 hover:text-blue-400 transition">DemoNet</router-link>
        <router-link v-for="t in TYPE_LIST" :key="t" :to="`/list/${t}`"
          class="text-xs text-gray-500 hover:text-white px-2 py-1 rounded transition-colors hidden lg:inline">
          {{ getMeta(t).emoji }} {{ getMeta(t).label }}
        </router-link>
      </div>
      <div class="flex items-center gap-3">
        <router-link to="/search" class="text-gray-400 hover:text-white p-1.5 transition" title="发现">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </router-link>
        <template v-if="auth.isLoggedIn">
          <router-link to="/profile"
            class="flex items-center gap-2 text-xs text-gray-400 hover:text-white transition px-2 py-1 rounded-lg hover:bg-gray-800">
            <span class="w-7 h-7 rounded-full bg-blue-600 flex items-center justify-center text-white font-semibold text-xs">
              {{ (auth.user?.username || '?')[0].toUpperCase() }}
            </span>
            {{ auth.user?.username || '个人中心' }}
          </router-link>
        </template>
        <template v-else>
          <router-link to="/login" class="text-xs text-gray-400 hover:text-white transition px-2">登录</router-link>
          <router-link to="/register" class="text-xs bg-blue-600 hover:bg-blue-500 text-white px-3 py-1.5 rounded-lg transition">注册</router-link>
        </template>
      </div>
    </div>
  </nav>
</template>
