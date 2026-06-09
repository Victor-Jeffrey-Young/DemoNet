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

const categoryOptions = ['耳机', '键盘', '鼠标', '显示器', '手机', '平板', '笔记本', '音箱', '相机', '手表', '掌机', '其他']
</script>

<template>
  <h4 class="text-sm font-medium text-cyan-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span>📱</span> 数码信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="品牌" size="default">
      <el-input :model-value="modelValue?.brand || ''" @input="set('brand', $event)" placeholder="如: Apple, Sony" />
    </el-form-item>
    <el-form-item label="类别">
      <el-select :model-value="modelValue?.category || ''" @update:model-value="set('category', $event)" placeholder="选择类别" :teleported="false" popper-class="admin-select-drop" style="width:100%">
        <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
      </el-select>
    </el-form-item>
    <el-form-item label="年份">
      <el-input :model-value="modelValue?.year || ''" @input="set('year', $event)" placeholder="如: 2024" />
    </el-form-item>
    <el-form-item label="特性">
      <el-input :model-value="modelValue?.features || ''" @input="set('features', $event)" placeholder="逗号分隔，如: 主动降噪,蓝牙5.2,30h续航" />
    </el-form-item>
    <el-form-item label="B站视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.bilibili || ''" @input="setVideo('bilibili', $event)" placeholder="BV 号或 B站链接" />
    </el-form-item>
    <el-form-item label="YouTube 视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.youtube || ''" @input="setVideo('youtube', $event)" placeholder="YouTube 链接或 ID" />
    </el-form-item>
  </div>
</template>
