<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { TYPE_LIST, getMeta } from '../constants/types'
import TypeIcon from './TypeIcon.vue'

const auth = useAuthStore()
const scrolled = ref(false)
const mobileMenuOpen = ref(false)

function onScroll() {
  scrolled.value = window.scrollY > 20
}
onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<template>
  <nav 
    class="bg-gray-950/95 backdrop-blur-sm border-b border-gray-800/50 sticky top-0 z-50 transition-all duration-300"
    :class="scrolled ? 'shadow-lg shadow-black/20' : ''"
  >
    <div class="max-w-[90%] mx-auto px-6 flex items-center justify-between transition-all duration-300"
      :class="scrolled ? 'h-14' : 'h-16'"
    >
      <div class="flex items-center gap-1">
        <router-link to="/" class="text-lg font-black tracking-tight mr-3 hover:text-blue-400 transition flex items-center gap-1.5">
          <span class="text-blue-500">◆</span> DemoNet
        </router-link>
        <router-link v-for="t in TYPE_LIST" :key="t" :to="`/list/${t}`"
          class="text-xs text-gray-500 hover:text-white px-2 py-1 rounded transition-colors hidden lg:inline">
          {{ getMeta(t).label }}
        </router-link>
      </div>
      
      <!-- Desktop nav -->
      <div class="hidden md:flex items-center gap-3">
        <router-link to="/search" class="text-gray-400 hover:text-white p-1.5 transition" title="发现">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </router-link>
        <template v-if="auth.isLoggedIn">
          <router-link v-if="auth.isAdmin" to="/admin"
            class="text-xs text-amber-400 hover:text-amber-300 transition px-2 py-1 rounded-lg hover:bg-gray-800 font-medium">
            管理后台
          </router-link>
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

      <!-- Mobile hamburger -->
      <button @click="mobileMenuOpen = !mobileMenuOpen" class="md:hidden text-gray-400 hover:text-white p-1.5">
        <svg v-if="!mobileMenuOpen" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
        <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>

    <!-- Mobile menu -->
    <Transition name="slide">
      <div v-if="mobileMenuOpen" class="md:hidden border-t border-gray-800 bg-gray-950/98 backdrop-blur-sm">
        <div class="px-6 py-4 grid grid-cols-3 gap-2">
          <router-link v-for="t in TYPE_LIST" :key="t" :to="`/list/${t}`"
            @click="mobileMenuOpen = false"
            class="text-xs text-gray-400 hover:text-white hover:bg-gray-800 px-3 py-2 rounded-lg transition text-center">
            <TypeIcon :type="t" size="18" />
            {{ getMeta(t).label }}
          </router-link>
        </div>
        <div class="px-6 pb-4 flex items-center gap-3 md:hidden">
          <router-link to="/search" @click="mobileMenuOpen = false" class="text-xs text-gray-400 hover:text-white transition py-1">🔍 搜索</router-link>
          <template v-if="!auth.isLoggedIn">
            <router-link to="/login" @click="mobileMenuOpen = false" class="text-xs text-gray-400 hover:text-white transition py-1 ml-auto">登录</router-link>
            <router-link to="/register" @click="mobileMenuOpen = false" class="text-xs bg-blue-600 text-white px-3 py-1 rounded-lg">注册</router-link>
          </template>
          <template v-else>
            <router-link to="/profile" @click="mobileMenuOpen = false" class="text-xs text-gray-400 hover:text-white transition py-1 ml-auto">个人中心</router-link>
          </template>
        </div>
      </div>
    </Transition>
  </nav>
</template>

<style scoped>
.slide-enter-active, .slide-leave-active {
  transition: all 0.2s ease;
}
.slide-enter-from, .slide-leave-to {
  opacity: 0;
  max-height: 0;
  overflow: hidden;
}
.slide-enter-to, .slide-leave-from {
  max-height: 500px;
}
</style>
