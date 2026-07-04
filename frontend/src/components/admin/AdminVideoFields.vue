<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({ youtube: '', bilibili: '', steam: '' })
  },
  showSteam: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const videos = computed({
  get: () => props.modelValue || { youtube: '', bilibili: '', steam: '' },
  set: (val) => emit('update:modelValue', val)
})

function normalizeVideo(raw, platform) {
  if (!raw || !raw.trim()) return ''
  raw = raw.trim()
  if (platform === 'steam') return raw
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

function updateVideo(platform, raw) {
  const normalized = normalizeVideo(raw, platform)
  const newVideos = { ...videos.value }
  newVideos[platform] = normalized
  videos.value = newVideos
}
</script>

<template>
  <div class="grid grid-cols-1 gap-3">
    <el-form-item v-if="showSteam" label="Steam 视频">
      <el-input :model-value="videos.steam || ''" @update:model-value="updateVideo('steam', $event)" placeholder="Steam mp4/movie_max.mp4 URL" />
    </el-form-item>
    <el-form-item label="YouTube">
      <el-input :model-value="videos.youtube || ''" @update:model-value="updateVideo('youtube', $event)" placeholder="粘贴链接或 video ID" />
    </el-form-item>
    <el-form-item label="Bilibili">
      <el-input :model-value="videos.bilibili || ''" @update:model-value="updateVideo('bilibili', $event)" placeholder="粘贴 BV 号或链接" />
    </el-form-item>
  </div>
</template>
