<script setup>
import TypeIcon from '../TypeIcon.vue'
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

const originOptions = ['埃塞俄比亚', '哥伦比亚', '危地马拉', '肯尼亚', '巴西', '印尼', '哥斯达黎加', '巴拿马', '中国云南', '其他']
const roastOptions = ['浅烘', '中浅烘', '中烘', '中深烘', '深烘']
const processOptions = ['水洗', '日晒', '蜜处理', '湿刨法', '厌氧发酵', '其他']
</script>

<template>
  <h4 class="text-sm font-medium text-orange-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span><TypeIcon type="coffee" size="16" /></span> 咖啡信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="产地" size="default">
      <el-select :model-value="modelValue?.origin || ''" @update:model-value="set('origin', $event)" placeholder="选择产地" :teleported="false" style="width:100%">
        <el-option v-for="o in originOptions" :key="o" :label="o" :value="o" />
      </el-select>
    </el-form-item>
    <el-form-item label="烘焙度">
      <el-select :model-value="modelValue?.roast || ''" @update:model-value="set('roast', $event)" placeholder="选择烘焙度" :teleported="false" style="width:100%">
        <el-option v-for="o in roastOptions" :key="o" :label="o" :value="o" />
      </el-select>
    </el-form-item>
    <el-form-item label="处理法">
      <el-select :model-value="modelValue?.process || ''" @update:model-value="set('process', $event)" placeholder="选择处理法" :teleported="false" style="width:100%">
        <el-option v-for="o in processOptions" :key="o" :label="o" :value="o" />
      </el-select>
    </el-form-item>
    <el-form-item label="品种">
      <el-input :model-value="modelValue?.variety || ''" @input="set('variety', $event)" placeholder="如: 原生种, 瑰夏" />
    </el-form-item>
    <el-form-item label="风味" class="col-span-2">
      <el-input :model-value="modelValue?.flavor || ''" @input="set('flavor', $event)" placeholder="逗号分隔，如: 柑橘,茉莉,蜂蜜,柠檬酸" />
    </el-form-item>
    <el-form-item label="B站视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.bilibili || ''" @input="setVideo('bilibili', $event)" placeholder="BV 号或 B站链接" />
    </el-form-item>
    <el-form-item label="YouTube 视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.youtube || ''" @input="setVideo('youtube', $event)" placeholder="YouTube 链接或 ID" />
    </el-form-item>
  </div>
</template>
