<script setup>
import { onMounted } from 'vue'
import AppNavbar from './components/AppNavbar.vue'
import AppFooter from './components/AppFooter.vue'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
onMounted(() => { if (auth.isLoggedIn) auth.fetchUser() })
</script>

<template>
  <div class="bg-gray-950 text-white min-h-screen flex flex-col">
    <AppNavbar />
    <div class="flex-1"><router-view /></div>
    <AppFooter />
  </div>
</template>
