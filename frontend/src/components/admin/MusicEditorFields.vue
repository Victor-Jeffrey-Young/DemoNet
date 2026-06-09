<script setup>
const props = defineProps({ modelValue: Object })
const emit = defineEmits(['update:modelValue'])

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

function set(k, v) {
  const obj = JSON.parse(JSON.stringify(props.modelValue || {}))
  if (k.startsWith('videos.')) {
    const platform = k.split('.')[1]
    v = normalizeVideo(v, platform)
    if (!obj.videos) obj.videos = {}
    obj.videos[platform] = v
  } else {
    obj[k] = v
  }
  emit('update:modelValue', obj)
}
</script>

<template>
  <h4 class="text-sm font-medium text-fuchsia-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span>🎵</span> 音乐信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="艺人" size="default">
      <el-input :model-value="modelValue?.artist || ''" @input="set('artist', $event)" placeholder="如: 周杰伦" />
    </el-form-item>
    <el-form-item label="年份">
      <el-input :model-value="modelValue?.year || ''" @input="set('year', $event)" placeholder="如: 2004" />
    </el-form-item>
    <el-form-item label="风格">
      <el-input :model-value="modelValue?.genre || ''" @input="set('genre', $event)" placeholder="如: 流行, R&B" />
    </el-form-item>
    <el-form-item label="曲目数">
      <el-input :model-value="modelValue?.tracks || ''" @input="set('tracks', $event)" placeholder="如: 10" />
    </el-form-item>
    <el-form-item label="试听链接" class="col-span-2">
      <el-input :model-value="modelValue?.preview_url || ''" @input="set('preview_url', $event)" placeholder="iTunes preview URL 或其他试听地址" />
    </el-form-item>
    <el-form-item label="Bilibili 视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.bilibili || ''" @input="set('videos.bilibili', $event)" placeholder="BV 号或 B站链接" />
    </el-form-item>
    <el-form-item label="YouTube 视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.youtube || ''" @input="set('videos.youtube', $event)" placeholder="YouTube 链接或 ID" />
    </el-form-item>
  </div>
</template>
