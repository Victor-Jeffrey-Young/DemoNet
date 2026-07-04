<script setup>
import TypeIcon from '../TypeIcon.vue'
import AdminVideoFields from './AdminVideoFields.vue'

const props = defineProps({ info: Object })
</script>

<template>
  <h4 class="text-sm font-medium text-sky-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span><TypeIcon type="model" size="16" /></span> 模型信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="级别/系列 (如 RG, MG)">
      <el-input v-model="info.grade" placeholder="如: Master Grade (MG)" />
    </el-form-item>
    <el-form-item label="比例">
      <el-input v-model="info.scale" placeholder="如: 1/100" />
    </el-form-item>
    <el-form-item label="材质">
      <el-input v-model="info.material" placeholder="如: ABS, PVC" />
    </el-form-item>
    <el-form-item label="所属作品">
      <el-input v-model="info.series" placeholder="如: 机动战士高达SEED" />
    </el-form-item>
  </div>
  
  <h4 class="text-sm font-semibold text-sky-400 mt-4 mb-3 border-t border-gray-700 pt-4">开箱/评测视频</h4>
  <AdminVideoFields v-model="info.videos" />
</template>
