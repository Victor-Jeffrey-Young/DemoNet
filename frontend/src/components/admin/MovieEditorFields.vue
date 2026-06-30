<script setup>
import TypeIcon from '../TypeIcon.vue'
const props = defineProps({ modelValue: Object })
const emit = defineEmits(['update:modelValue'])

function set(k, v) {
  const obj = JSON.parse(JSON.stringify(props.modelValue || {}))
  if (k.includes('.')) {
    const [parent, child] = k.split('.')
    if (!obj[parent]) obj[parent] = {}
    obj[parent][child] = v
  } else {
    obj[k] = v
  }
  emit('update:modelValue', obj)
}

function normalizeVideo(raw, platform) {
  if (!raw || !raw.trim()) return raw
  raw = raw.trim()
  if (platform === 'bilibili') {
    if (raw.includes('player.bilibili.com')) return raw
    if (raw.startsWith('BV')) return '//player.bilibili.com/player.html?bvid=' + raw
    return '//player.bilibili.com/player.html?bvid=' + raw
  }
  if (platform === 'youtube') {
    if (raw.includes('youtube.com/embed/')) return raw
    if (raw.includes('watch?v=')) {
      const m = raw.match(/[?&]v=([a-zA-Z0-9_-]{11})/)
      if (m) return 'https://www.youtube.com/embed/' + m[1]
    }
    if (raw.includes('youtu.be/')) {
      const m = raw.match(/youtu\.be\/([a-zA-Z0-9_-]{11})/)
      if (m) return 'https://www.youtube.com/embed/' + m[1]
    }
    if (/^[a-zA-Z0-9_-]{11}$/.test(raw)) return 'https://www.youtube.com/embed/' + raw
    return raw
  }
  return raw
}

function handleVideoInput(platform, value) {
  set('videos.' + platform, normalizeVideo(value, platform))
}
</script>

<template>
  <h4 class="text-sm font-medium text-red-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span><TypeIcon type="movie" size="16" /></span> 电影信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="导演" size="default">
      <el-input :model-value="modelValue?.director || ''" @input="set('director', $event)" placeholder="如: 克里斯托弗·诺兰" />
    </el-form-item>
    <el-form-item label="年份">
      <el-input :model-value="modelValue?.year || ''" @input="set('year', $event)" placeholder="如: 2023" />
    </el-form-item>
    <el-form-item label="时长">
      <el-input :model-value="modelValue?.duration || ''" @input="set('duration', $event)" placeholder="如: 180min" />
    </el-form-item>
    <el-form-item label="类型">
      <el-input :model-value="modelValue?.genre || ''" @input="set('genre', $event)" placeholder="如: 剧情, 历史, 传记" />
    </el-form-item>
    <el-form-item label="YouTube 预告片" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.youtube || ''" @update:model-value="handleVideoInput('youtube', $event)"
        placeholder="粘贴 YouTube 视频链接或 ID" />
    </el-form-item>
    <el-form-item label="Bilibili 视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.bilibili || ''" @update:model-value="handleVideoInput('bilibili', $event)"
        placeholder="粘贴 BV 号或 B站链接" />
    </el-form-item>
  </div>
</template>
