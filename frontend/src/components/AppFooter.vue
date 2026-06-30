<template>
  <footer class="bg-gray-950 border-t border-gray-800 w-full">
    <div class="flex flex-col md:flex-row justify-between items-center w-full px-6 py-12 max-w-[90%] mx-auto gap-6">
      <div class="flex flex-col items-center md:items-start gap-3">
        <span class="text-xl font-black text-white tracking-tight">
          <span class="text-blue-500">◆</span> DemoNet
        </span>
        <p class="text-sm text-gray-500 text-center md:text-left max-w-md">
          &copy; 2026 DemoNet — 试玩派对。聚合精品体验内容，让每一次探索都值得。
        </p>
      </div>
      <div class="flex flex-wrap justify-center gap-6">
        <router-link to="/list" class="text-xs font-semibold tracking-wider text-gray-500 hover:text-white transition-colors uppercase">
          探索
        </router-link>
        <router-link to="/search" class="text-xs font-semibold tracking-wider text-gray-500 hover:text-white transition-colors uppercase">
          搜索
        </router-link>
        <span class="text-xs font-semibold tracking-wider text-gray-500 cursor-default">策展理念</span>
        <router-link to="/login" class="text-xs font-semibold tracking-wider text-gray-500 hover:text-white transition-colors uppercase">
          登录
        </router-link>
      </div>
    </div>
  </footer>
</template>
