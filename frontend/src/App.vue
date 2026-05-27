<script setup>
import AppNavbar from './components/AppNavbar.vue'
import AppFooter from './components/AppFooter.vue'
</script>

<template>
  <div class="bg-gray-950 text-white min-h-screen flex flex-col">
    <AppNavbar />
    <div class="flex-1">
      <router-view />
    </div>
    <AppFooter />
  </div>
</template>
