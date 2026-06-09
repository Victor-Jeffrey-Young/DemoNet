<script setup>
const props = defineProps({ modelValue: Object })
const emit = defineEmits(['update:modelValue'])

function set(k, v) {
  const obj = JSON.parse(JSON.stringify(props.modelValue || {}))
  obj[k] = v
  emit('update:modelValue', obj)
}

function normalizeVideo(raw, platform) {
  if (!raw || !raw.trim()) return raw
  raw = raw.trim()
  if (platform === 'bilibili') {
    if (raw.includes('player.bilibili.com')) return raw
    return '//player.bilibili.com/player.html?bvid=' + raw
  }
  if (platform === 'youtube') {
    if (raw.includes('youtube.com/embed/')) return raw
    const m = raw.match(/[?&]v=([a-zA-Z0-9_-]{11})/)
    if (m) return 'https://www.youtube.com/embed/' + m[1]
    if (raw.includes('youtu.be/')) {
      const m2 = raw.match(/youtu\.be\/([a-zA-Z0-9_-]{11})/)
      if (m2) return 'https://www.youtube.com/embed/' + m2[1]
    }
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

const genreOptions = ['热血', '战斗', '奇幻', '治愈', '科幻', '悬疑', '喜剧', '日常', '爱情', '运动', '美食', '冒险', '黑暗奇幻', '恐怖', '历史', '偶像', '剧情', '中国风', '机甲', '校园']
const originOptions = ['日漫', '国漫', '美漫', '韩漫', '其他']
</script>

<template>
  <h4 class="text-sm font-medium text-violet-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span>🎭</span> 动漫信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="制作社" size="default">
      <el-input :model-value="modelValue?.studio || ''" @input="set('studio', $event)" placeholder="如: MADHOUSE, ufotable" />
    </el-form-item>
    <el-form-item label="年份">
      <el-input :model-value="modelValue?.year || ''" @input="set('year', $event)" placeholder="如: 2023" />
    </el-form-item>
    <el-form-item label="类型">
      <el-select :model-value="modelValue?.genre || ''" @update:model-value="set('genre', $event)" placeholder="动漫类型" :teleported="false" popper-class="admin-select-drop" style="width:100%">
        <el-option v-for="g in genreOptions" :key="g" :label="g" :value="g" />
      </el-select>
    </el-form-item>
    <el-form-item label="原作地区">
      <el-select :model-value="modelValue?.origin || ''" @update:model-value="set('origin', $event)" :teleported="false" popper-class="admin-select-drop" style="width:100%">
        <el-option v-for="o in originOptions" :key="o" :label="o" :value="o" />
      </el-select>
    </el-form-item>
    <el-form-item label="集数">
      <el-input :model-value="modelValue?.episodes || ''" @input="set('episodes', $event)" placeholder="如: 12" />
    </el-form-item>
    <el-form-item label="B站视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.bilibili || ''" @input="setVideo('bilibili', $event)" placeholder="BV 号或 B站链接" />
    </el-form-item>
    <el-form-item label="YouTube 视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.youtube || ''" @input="setVideo('youtube', $event)" placeholder="YouTube 链接或 ID" />
    </el-form-item>
  </div>
</template>
