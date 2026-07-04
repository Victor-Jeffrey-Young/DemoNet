<script setup>
import TypeIcon from '../TypeIcon.vue'
import AdminVideoFields from './AdminVideoFields.vue'

const props = defineProps({ info: Object })
</script>

<template>
  <h4 class="text-sm font-medium text-amber-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span><TypeIcon type="boardgame" size="16" /></span> 桌游信息
  </h4>
  <div class="grid grid-cols-3 gap-4">
    <el-form-item label="游玩人数">
      <el-input v-model="info.players" placeholder="如: 2-4人" />
    </el-form-item>
    <el-form-item label="游玩时间">
      <el-input v-model="info.playtime" placeholder="如: 45-60分钟" />
    </el-form-item>
    <el-form-item label="重度 (难度)">
      <el-input v-model="info.weight" placeholder="如: 2.5 / 5.0" />
    </el-form-item>
    <el-form-item label="核心机制与规则" class="col-span-3">
      <el-input v-model="info.rule_text" type="textarea" :rows="3" placeholder="简述桌游的核心玩法机制" />
    </el-form-item>
    <el-form-item label="规则图片链接" class="col-span-3">
      <el-input v-model="info.rule_images" placeholder="多张图片用逗号分隔" />
    </el-form-item>
  </div>
  
  <h4 class="text-sm font-semibold text-amber-400 mt-4 mb-3 border-t border-gray-700 pt-4">教学/开箱视频</h4>
  <AdminVideoFields v-model="info.videos" />
</template>
