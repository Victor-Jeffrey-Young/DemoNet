<script setup>
import TypeIcon from '../TypeIcon.vue'
import AdminVideoFields from './AdminVideoFields.vue'

const props = defineProps({ info: Object })
</script>

<template>
  <h4 class="text-sm font-medium text-red-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span><TypeIcon type="movie" size="16" /></span> 电影信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="导演" size="default">
      <el-input v-model="info.director" placeholder="如: 克里斯托弗·诺兰" />
    </el-form-item>
    <el-form-item label="年份">
      <el-input v-model="info.year" placeholder="如: 2023" />
    </el-form-item>
    <el-form-item label="时长">
      <el-input v-model="info.duration" placeholder="如: 180min" />
    </el-form-item>
    <el-form-item label="类型">
      <el-input v-model="info.genre" placeholder="如: 剧情, 历史, 传记" />
    </el-form-item>
  </div>
  
  <h4 class="text-sm font-semibold text-red-400 mt-4 mb-3 border-t border-gray-700 pt-4">视频预告</h4>
  <AdminVideoFields v-model="info.videos" />
</template>
