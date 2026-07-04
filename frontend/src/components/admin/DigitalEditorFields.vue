<script setup>
import TypeIcon from '../TypeIcon.vue'
import AdminVideoFields from './AdminVideoFields.vue'

const props = defineProps({ info: Object })
</script>

<template>
  <h4 class="text-sm font-medium text-cyan-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span><TypeIcon type="digital" size="16" /></span> 数码产品信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="品牌">
      <el-input v-model="info.brand" placeholder="如: Apple, Sony" />
    </el-form-item>
    <el-form-item label="分类">
      <el-input v-model="info.category" placeholder="如: 智能手机, 相机" />
    </el-form-item>
    <el-form-item label="发布年份">
      <el-input v-model="info.year" placeholder="如: 2024" />
    </el-form-item>
    <el-form-item label="核心参数/卖点" class="col-span-2">
      <el-input v-model="info.features" type="textarea" :rows="3" placeholder="如: M4 芯片, OLED 屏幕" />
    </el-form-item>
  </div>
  
  <h4 class="text-sm font-semibold text-cyan-400 mt-4 mb-3 border-t border-gray-700 pt-4">开箱/评测视频</h4>
  <AdminVideoFields v-model="info.videos" />
</template>
