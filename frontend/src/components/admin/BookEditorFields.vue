<script setup>
import TypeIcon from '../TypeIcon.vue'
import AdminVideoFields from './AdminVideoFields.vue'

const props = defineProps({ info: Object })
</script>

<template>
  <h4 class="text-sm font-medium text-amber-500 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span><TypeIcon type="book" size="16" /></span> 书籍信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="作者">
      <el-input v-model="info.author" placeholder="如: J.K. Rowling" />
    </el-form-item>
    <el-form-item label="出版年份">
      <el-input v-model="info.year" placeholder="如: 1997" />
    </el-form-item>
    <el-form-item label="页数">
      <el-input v-model="info.pages" placeholder="如: 350" />
    </el-form-item>
    <el-form-item label="分类">
      <el-input v-model="info.category" placeholder="如: 奇幻, 文学" />
    </el-form-item>
  </div>
  
  <h4 class="text-sm font-semibold text-amber-500 mt-4 mb-3 border-t border-gray-700 pt-4">相关视频</h4>
  <AdminVideoFields v-model="info.videos" />
</template>
