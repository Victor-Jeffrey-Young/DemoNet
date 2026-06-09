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

const gradeOptions = ['PG', 'MG', 'RG', 'PGU', 'HMM', 'FM', 'EG', 'HG', 'SD']
const materialOptions = ['PS', 'ABS', 'PE', 'PP', 'LED', '合金']
</script>

<template>
  <h4 class="text-sm font-medium text-sky-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span>🧩</span> 模型信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="等级" size="default">
      <el-select :model-value="modelValue?.grade || ''" @update:model-value="set('grade', $event)" placeholder="选择等级" :teleported="false" popper-class="admin-select-drop" style="width:100%">
        <el-option v-for="g in gradeOptions" :key="g" :label="g" :value="g" />
      </el-select>
    </el-form-item>
    <el-form-item label="比例">
      <el-input :model-value="modelValue?.scale || ''" @input="set('scale', $event)" placeholder="如: 1/100" />
    </el-form-item>
    <el-form-item label="材质">
      <el-input :model-value="modelValue?.material || ''" @input="set('material', $event)" placeholder="如: PS,ABS,PE（逗号分隔）" />
    </el-form-item>
    <el-form-item label="系列">
      <el-input :model-value="modelValue?.series || ''" @input="set('series', $event)" placeholder="如: Wing EW, UC" />
    </el-form-item>
    <el-form-item label="B站视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.bilibili || ''" @input="setVideo('bilibili', $event)" placeholder="开箱/评测视频" />
    </el-form-item>
    <el-form-item label="YouTube 视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.youtube || ''" @input="setVideo('youtube', $event)" placeholder="YouTube 链接或 ID" />
    </el-form-item>
  </div>
</template>
