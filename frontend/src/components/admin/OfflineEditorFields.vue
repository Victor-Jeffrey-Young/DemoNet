<script setup>
import TypeIcon from '../TypeIcon.vue'
import AdminVideoFields from './AdminVideoFields.vue'

const props = defineProps({ info: Object })
</script>

<template>
  <h4 class="text-sm font-medium text-indigo-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span><TypeIcon type="offline" size="16" /></span> 线下体验信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="活动类型">
      <el-input v-model="info.event_type" placeholder="如: 密室逃脱 / 剧本杀 / 展览" />
    </el-form-item>
    <el-form-item label="地点/场馆">
      <el-input v-model="info.venue" placeholder="如: 上海新国际博览中心" />
    </el-form-item>
    <el-form-item label="日期">
      <el-input v-model="info.date" placeholder="如: 2024.10.01 - 2024.10.07" />
    </el-form-item>
    <el-form-item label="时间">
      <el-input v-model="info.time" placeholder="如: 10:00 - 18:00" />
    </el-form-item>
    <el-form-item label="人均价格">
      <el-input v-model="info.price" placeholder="如: ￥298 / 人" />
    </el-form-item>
    <el-form-item label="建议人数">
      <el-input v-model="info.capacity" placeholder="如: 4-8人" />
    </el-form-item>
    <el-form-item label="难度/恐怖程度">
      <el-input v-model="info.difficulty" placeholder="如: 微恐 / 烧脑" />
    </el-form-item>
    <el-form-item label="活动亮点">
      <el-input v-model="info.highlights" placeholder="如: 沉浸式演艺, 实景搜证" />
    </el-form-item>
  </div>
  
  <h4 class="text-sm font-semibold text-indigo-400 mt-4 mb-3 border-t border-gray-700 pt-4">宣传视频或 VLOG</h4>
  <AdminVideoFields v-model="info.videos" />
</template>
