<script setup>
import { uploadImage } from '../../api/admin'
import { ElMessage } from 'element-plus'

const props = defineProps({ modelValue: Object, itemId: Number })
const emit = defineEmits(['update:modelValue'])

function set(k, v) {
  const obj = JSON.parse(JSON.stringify(props.modelValue || {}))
  obj[k] = v
  emit('update:modelValue', obj)
}

async function uploadRuleImages(event) {
  const files = event.target.files
  if (!files.length) return
  if (!props.itemId) { ElMessage.warning('请先保存内容后再上传规则图片'); return }
  const existing = (props.modelValue?.rule_images || '').trim()
  const urls = existing ? existing.split(',').map(u => u.trim()).filter(Boolean) : []
  for (const file of files) {
    try {
      const fd = new FormData(); fd.append('file', file); fd.append('type', 'cover')
      const res = await uploadImage(props.itemId, fd)
      urls.push(res.url)
    } catch (e) {
      ElMessage.error(`上传 ${file.name} 失败: ` + (e.response?.data?.error || '未知错误'))
    }
  }
  set('rule_images', urls.join(','))
  if (files.length > 0) ElMessage.success(`已上传 ${files.length} 张图片`)
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
</script>

<template>
  <h4 class="text-sm font-medium text-amber-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span>🎲</span> 桌游信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="玩家人数" size="default">
      <el-input :model-value="modelValue?.players || ''" @input="set('players', $event)" placeholder="如: 2-4" />
    </el-form-item>
    <el-form-item label="游戏时长">
      <el-input :model-value="modelValue?.playtime || ''" @input="set('playtime', $event)" placeholder="如: 60-120min" />
    </el-form-item>
    <el-form-item label="重度 (BGG Weight)">
      <el-input :model-value="modelValue?.weight || ''" @input="set('weight', $event)" placeholder="如: 3.2（1-5 分制）" />
    </el-form-item>
    <el-form-item label="规则说明" class="col-span-2">
      <el-input :model-value="modelValue?.rule_text || ''" @input="set('rule_text', $event)" type="textarea" :rows="8" placeholder="输入规则文本，详情页可展开查看..." />
    </el-form-item>
    <el-form-item label="规则图片" class="col-span-2">
      <div class="flex items-start gap-2 w-full">
        <div class="flex-1">
          <el-input :model-value="modelValue?.rule_images || ''" @input="set('rule_images', $event)" type="textarea" :rows="3"
            placeholder="逗号分隔的图片 URL，或使用下方按钮多选上传" />
          <div v-if="(modelValue?.rule_images || '')" class="flex flex-wrap gap-1.5 mt-2">
            <img
              v-for="(u, i) in (modelValue?.rule_images || '').split(',').map(s => s.trim()).filter(Boolean)" :key="i"
              :src="u" class="w-12 h-16 object-cover rounded border border-gray-600 cursor-pointer hover:border-amber-400 transition-colors"
              @click="window.open(u, '_blank')" />
            <span class="text-[10px] text-gray-500 self-center ml-1">{{ (modelValue?.rule_images || '').split(',').filter(Boolean).length }} 张</span>
          </div>
        </div>
        <label class="el-button el-button--default cursor-pointer shrink-0">
          📷 多选上传
          <input type="file" accept="image/*,.webp,.avif,.heic,.heif" multiple class="hidden" @change="uploadRuleImages" />
        </label>
      </div>
    </el-form-item>
    <el-form-item label="B站视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.bilibili || ''" @input="setVideo('bilibili', $event)" placeholder="规则教学/实况视频" />
    </el-form-item>
    <el-form-item label="YouTube 视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.youtube || ''" @input="setVideo('youtube', $event)" placeholder="YouTube 链接或 ID" />
    </el-form-item>
  </div>
</template>
