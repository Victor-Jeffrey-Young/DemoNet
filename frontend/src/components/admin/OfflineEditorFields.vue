<script setup>
const props = defineProps({ modelValue: Object })
const emit = defineEmits(['update:modelValue'])

function set(k, v) {
  const obj = JSON.parse(JSON.stringify(props.modelValue || {}))
  obj[k] = v
  emit('update:modelValue', obj)
}

function normalizeVideo(raw, platform) {
  if (!raw || !raw.trim()) return raw; raw = raw.trim()
  if (platform === 'bilibili') return raw.includes('player.bilibili.com') ? raw : '//player.bilibili.com/player.html?bvid=' + raw
  if (platform === 'youtube') {
    if (raw.includes('youtube.com/embed/')) return raw
    const m = raw.match(/[?&]v=([a-zA-Z0-9_-]{11})/); if (m) return 'https://www.youtube.com/embed/' + m[1]
    const m2 = raw.match(/youtu\.be\/([a-zA-Z0-9_-]{11})/); if (m2) return 'https://www.youtube.com/embed/' + m2[1]
    if (/^[a-zA-Z0-9_-]{11}$/.test(raw)) return 'https://www.youtube.com/embed/' + raw
    return raw
  }
  return raw
}

function setVideo(k, v) {
  const obj = JSON.parse(JSON.stringify(props.modelValue || {}))
  if (!obj.videos) obj.videos = {}
  obj.videos[k] = normalizeVideo(v, k)
  emit('update:modelValue', obj)
}

const eventTypeOptions = ['密室逃脱', '沉浸式剧场', '展览', '市集', 'Live', '工作坊', '城市探索', '其他']
</script>

<template>
  <h4 class="text-sm font-medium text-indigo-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span>🏛️</span> 活动信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="活动类型" size="default">
      <el-select :model-value="modelValue?.event_type || ''" @update:model-value="set('event_type', $event)" placeholder="选择类型" :teleported="false" popper-class="admin-select-drop" style="width:100%">
        <el-option v-for="o in eventTypeOptions" :key="o" :label="o" :value="o" />
      </el-select>
    </el-form-item>
    <el-form-item label="地点">
      <el-input :model-value="modelValue?.venue || ''" @input="set('venue', $event)" placeholder="如: 上海西岸美术馆 B1" />
    </el-form-item>
    <el-form-item label="日期">
      <el-input :model-value="modelValue?.date || ''" @input="set('date', $event)" placeholder="如: 2024.12.20 - 2025.03.15" />
    </el-form-item>
    <el-form-item label="时间">
      <el-input :model-value="modelValue?.time || ''" @input="set('time', $event)" placeholder="如: 10:00-18:00（周一闭馆）" />
    </el-form-item>
    <el-form-item label="价格">
      <el-input :model-value="modelValue?.price || ''" @input="set('price', $event)" placeholder="如: ¥128/人" />
    </el-form-item>
    <el-form-item label="人数">
      <el-input :model-value="modelValue?.capacity || ''" @input="set('capacity', $event)" placeholder="如: 4-6人/场" />
    </el-form-item>
    <el-form-item label="难度/恐怖度" class="col-span-2">
      <el-input :model-value="modelValue?.difficulty || ''" @input="set('difficulty', $event)" placeholder="如: ★★★☆（密室适用，可留空）" />
    </el-form-item>
    <el-form-item label="亮点" class="col-span-2">
      <el-input :model-value="modelValue?.highlights || ''" @input="set('highlights', $event)" placeholder="逗号分隔，如: 互动装置,沉浸式,适合拍照" />
    </el-form-item>
    <el-form-item label="B站视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.bilibili || ''" @input="setVideo('bilibili', $event)" placeholder="BV 号或 B站链接" />
    </el-form-item>
    <el-form-item label="YouTube 视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.youtube || ''" @input="setVideo('youtube', $event)" placeholder="YouTube 链接或 ID" />
    </el-form-item>
  </div>
</template>
