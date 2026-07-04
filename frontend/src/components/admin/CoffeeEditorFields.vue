<script setup>
import TypeIcon from '../TypeIcon.vue'
import AdminVideoFields from './AdminVideoFields.vue'

const props = defineProps({ info: Object })
</script>

<template>
  <h4 class="text-sm font-medium text-orange-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span><TypeIcon type="coffee" size="16" /></span> 咖啡豆信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="产地">
      <el-input v-model="info.origin" placeholder="如: 埃塞俄比亚 耶加雪菲" />
    </el-form-item>
    <el-form-item label="烘焙度">
      <el-input v-model="info.roast" placeholder="如: 浅焙 / 中深焙" />
    </el-form-item>
    <el-form-item label="处理法">
      <el-input v-model="info.process" placeholder="如: 水洗 / 日晒 / 蜜处理" />
    </el-form-item>
    <el-form-item label="豆种">
      <el-input v-model="info.variety" placeholder="如: 瑰夏 / 波旁" />
    </el-form-item>
    <el-form-item label="风味描述" class="col-span-2">
      <el-input v-model="info.flavor" placeholder="如: 柑橘, 茉莉花, 蜂蜜" />
    </el-form-item>
  </div>
  
  <h4 class="text-sm font-semibold text-orange-400 mt-4 mb-3 border-t border-gray-700 pt-4">冲煮指南或评测视频</h4>
  <AdminVideoFields v-model="info.videos" />
</template>
