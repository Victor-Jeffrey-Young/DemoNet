<script setup>
import TypeIcon from '../TypeIcon.vue'
import AdminVideoFields from './AdminVideoFields.vue'

const props = defineProps({ info: Object })

const platforms = [
  { key: 'apple', label: '🍎 Apple Music' },
  { key: 'spotify', label: '🎵 Spotify' },
  { key: 'preview', label: '🔊 试听' },
]

function ensureLinks() {
  if (!info.links) info.links = {}
}
</script>

<template>
  <h4 class="text-sm font-medium text-fuchsia-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span><TypeIcon type="music" size="16" /></span> 音乐信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="艺术家/乐队">
      <el-input v-model="info.artist" placeholder="如: Taylor Swift" />
    </el-form-item>
    <el-form-item label="发行年份">
      <el-input v-model="info.year" placeholder="如: 2024" />
    </el-form-item>
    <el-form-item label="音乐流派">
      <el-input v-model="info.genre" placeholder="如: Pop, Rock, Jazz" />
    </el-form-item>
    <el-form-item label="试听音频预览">
      <el-input v-model="info.preview_url" placeholder="填入 30 秒试听 URL" />
    </el-form-item>
    <el-form-item label="曲目列表" class="col-span-2">
      <el-input v-model="info.tracks" type="textarea" :rows="4" placeholder="1. Song A&#10;2. Song B" />
    </el-form-item>
  </div>

  <h4 class="text-sm font-medium text-fuchsia-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    平台链接
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="Apple Music 链接">
      <el-input v-model="info.links.appleMusic" @focusin="ensureLinks" placeholder="https://music.apple.com/cn/album/..." />
    </el-form-item>
    <el-form-item label="Spotify 链接">
      <el-input v-model="info.links.spotify" @focusin="ensureLinks" placeholder="https://open.spotify.com/album/..." />
    </el-form-item>
  </div>

  <el-form-item label="默认播放平台">
    <div class="flex items-center gap-1 overflow-x-auto scrollbar-hide">
      <button v-for="p in platforms" :key="p.key"
        @click="info.preferred_platform = p.key"
        :class="(info.preferred_platform || '') === p.key
          ? 'bg-white/15 text-white ring-1 ring-white/20'
          : 'bg-white/5 text-gray-400 hover:bg-white/10 hover:text-white'"
        class="text-[11px] px-2.5 py-1 rounded-md transition-all whitespace-nowrap cursor-pointer">
        {{ p.label }}
      </button>
    </div>
  </el-form-item>
  
  <h4 class="text-sm font-semibold text-fuchsia-400 mt-4 mb-3 border-t border-gray-700 pt-4">MV 或 现场视频</h4>
  <AdminVideoFields v-model="info.videos" />
</template>
