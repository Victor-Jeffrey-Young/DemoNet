<script setup>
import TypeIcon from '../TypeIcon.vue'
import AdminVideoFields from './AdminVideoFields.vue'

const props = defineProps({ info: Object })
</script>

<template>
  <h4 class="text-sm font-medium text-violet-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span><TypeIcon type="anime" size="16" /></span> 动漫信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="制作公司">
      <el-input v-model="info.studio" placeholder="如: Kyoto Animation" />
    </el-form-item>
    <el-form-item label="首播年份">
      <el-input v-model="info.year" placeholder="如: 2024" />
    </el-form-item>
    <el-form-item label="原作来源">
      <el-input v-model="info.origin" placeholder="如: 漫画改编, 原创" />
    </el-form-item>
    <el-form-item label="集数">
      <el-input v-model="info.episodes" placeholder="如: 24集" />
    </el-form-item>
    <el-form-item label="类型" class="col-span-2">
      <el-input v-model="info.genre" placeholder="如: 热血, 奇幻" />
    </el-form-item>
  </div>
  
  <h4 class="text-sm font-semibold text-violet-400 mt-4 mb-3 border-t border-gray-700 pt-4">视频 PV</h4>
  <AdminVideoFields v-model="info.videos" />
</template>
