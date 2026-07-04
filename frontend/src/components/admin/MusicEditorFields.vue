<script setup>
import TypeIcon from '../TypeIcon.vue'
import AdminVideoFields from './AdminVideoFields.vue'

const props = defineProps({ info: Object })
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
    <el-form-item label="试听链接 (Spotify/Apple Music)">
      <el-input v-model="info.preview_url" placeholder="填入播放器嵌入代码或链接" />
    </el-form-item>
    <el-form-item label="曲目列表" class="col-span-2">
      <el-input v-model="info.tracks" type="textarea" :rows="4" placeholder="1. Song A&#10;2. Song B" />
    </el-form-item>
  </div>
  
  <h4 class="text-sm font-semibold text-fuchsia-400 mt-4 mb-3 border-t border-gray-700 pt-4">MV 或 现场视频</h4>
  <AdminVideoFields v-model="info.videos" />
</template>
