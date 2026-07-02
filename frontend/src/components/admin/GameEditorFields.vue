<script setup>
import { ref, computed, watch } from 'vue'
import TypeIcon from '../TypeIcon.vue'

const props = defineProps({ modelValue: Object })
const emit = defineEmits(['update:modelValue'])

const data = computed(() => props.modelValue || {})

// ---- helpers ----
function set(k, v) {
  const obj = JSON.parse(JSON.stringify(data.value))
  if (k.includes('.')) {
    const [parent, child] = k.split('.')
    if (!obj[parent]) obj[parent] = {}
    obj[parent][child] = v
  } else {
    obj[k] = v
  }
  emit('update:modelValue', obj)
}

function setVideos(platform, raw) {
  if (!raw || !raw.trim()) { set('videos.' + platform, ''); return }
  raw = raw.trim()
  if (platform === 'steam') { set('videos.steam', raw); return }
  if (platform === 'bilibili') {
    if (raw.includes('player.bilibili.com')) set('videos.bilibili', raw)
    else if (raw.startsWith('BV')) set('videos.bilibili', '//player.bilibili.com/player.html?bvid=' + raw)
    else set('videos.bilibili', '//player.bilibili.com/player.html?bvid=' + raw)
    return
  }
  if (platform === 'youtube') {
    if (raw.includes('youtube.com/embed/')) set('videos.youtube', raw)
    else if (raw.includes('watch?v=')) {
      const m = raw.match(/[?&]v=([a-zA-Z0-9_-]{11})/)
      if (m) set('videos.youtube', 'https://www.youtube.com/embed/' + m[1])
      else set('videos.youtube', raw)
    } else if (raw.includes('youtu.be/')) {
      const m = raw.match(/youtu\.be\/([a-zA-Z0-9_-]{11})/)
      if (m) set('videos.youtube', 'https://www.youtube.com/embed/' + m[1])
      else set('videos.youtube', raw)
    } else if (/^[a-zA-Z0-9_-]{11}$/.test(raw)) set('videos.youtube', 'https://www.youtube.com/embed/' + raw)
    else set('videos.youtube', raw)
  }
}

// ---- Screenshots ----
function addScreenshot() {
  const ss = Array.isArray(data.value.screenshots) ? [...data.value.screenshots] : []
  ss.push('')
  set('screenshots', ss)
}
function removeScreenshot(idx) {
  const ss = [...data.value.screenshots]
  ss.splice(idx, 1)
  set('screenshots', ss)
}
function updateScreenshot(idx, val) {
  const ss = [...data.value.screenshots]
  ss[idx] = val
  set('screenshots', ss)
}

// ---- DLC ----
const dlcList = computed(() => {
  try {
    const d = data.value.dlc
    if (Array.isArray(d)) return d.map(x => typeof x === 'object' ? { ...x } : { id: x, name: 'App ' + x })
    if (typeof d === 'string') return JSON.parse(d)
    return []
  } catch { return [] }
})
function addDlc() { set('dlc', [...dlcList.value, { id: '', name: '' }]) }
function removeDlc(idx) {
  const d = [...dlcList.value]
  d.splice(idx, 1)
  set('dlc', d)
}
function updateDlc(idx, field, val) {
  const d = [...dlcList.value]
  d[idx] = { ...d[idx], [field]: val }
  set('dlc', d)
}

// ---- Features ----
const featuresStr = ref((data.value.features || []).join(', '))
watch(featuresStr, (val) => {
  const arr = val.split(',').map(s => s.trim()).filter(Boolean)
  set('features', arr)
})

// ---- Languages ----
const langList = computed(() => {
  const raw = data.value.languages || ''
  return raw.split(',').map(s => s.trim()).filter(Boolean)
})
function addLang() { set('languages', (data.value.languages || '') + ', ') }
function updateLang(i, val) {
  const langs = [...langList.value]
  langs[i] = val
  set('languages', langs.join(', '))
}
function removeLang(i) {
  const langs = [...langList.value]
  langs.splice(i, 1)
  set('languages', langs.join(', '))
}
function toggleAudio(i) {
  const langs = [...langList.value]
  const name = langs[i].replace('*', '').trim()
  langs[i] = langs[i].includes('*') ? name : name + '*'
  set('languages', langs.join(', '))
}
</script>

<template>
  <div class="game-editor space-y-5">

    <!-- ====== 开发信息 ====== -->
    <section>
      <h4 class="text-sm font-semibold text-emerald-400 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
        <span><TypeIcon type="game" size="16" /></span> 开发信息
      </h4>
      <div class="grid grid-cols-4 gap-3">
        <el-form-item label="开发商" size="small" class="col-span-2">
          <el-input :model-value="data.developer || ''" @input="set('developer', $event)" placeholder="Supergiant Games" />
        </el-form-item>
        <el-form-item label="发行商" size="small" class="col-span-2">
          <el-input :model-value="data.publisher || ''" @input="set('publisher', $event)" placeholder="发行商" />
        </el-form-item>
        <el-form-item label="发行日期" size="small">
          <el-input :model-value="data.release_date || ''" @input="set('release_date', $event)" placeholder="2024-05-06" />
        </el-form-item>
        <el-form-item label="价格" size="small">
          <el-input :model-value="data.price || ''" @input="set('price', $event)" placeholder="CNY 268.00" />
        </el-form-item>
        <el-form-item label="类型" size="small">
          <el-input :model-value="data.genre || ''" @input="set('genre', $event)" placeholder="Action, RPG" />
        </el-form-item>
        <el-form-item label="平台" size="small">
          <el-input :model-value="data.platform || ''" @input="set('platform', $event)" placeholder="PC, PS5, Xbox" />
        </el-form-item>
        <el-form-item label="免费" size="small">
          <el-checkbox :model-value="!!data.free" @change="set('free', $event)" />
        </el-form-item>
        <el-form-item label="试玩版" size="small">
          <el-checkbox :model-value="!!data.demo_available" @change="set('demo_available', $event)" />
        </el-form-item>
      </div>
      <el-form-item label="DEMO 链接" size="small" class="mt-2" v-if="data.demo_available">
        <el-input :model-value="data.demo_url || ''" @input="set('demo_url', $event)" placeholder="steam://install/APPID 或 https://..." />
      </el-form-item>
    </section>

    <!-- ====== 视频 ====== -->
    <section>
      <h4 class="text-sm font-semibold text-emerald-400 mb-3 border-t border-gray-700 pt-4">视频</h4>
      <div class="grid grid-cols-1 gap-3">
        <el-form-item label="Steam 视频" size="small">
          <el-input :model-value="data?.videos?.steam || ''" @input="setVideos('steam', $event)" placeholder="Steam mp4/movie_max.mp4 URL" />
        </el-form-item>
        <el-form-item label="YouTube" size="small">
          <el-input :model-value="data?.videos?.youtube || ''" @input="setVideos('youtube', $event)" placeholder="粘贴链接或 video ID" />
        </el-form-item>
        <el-form-item label="Bilibili" size="small">
          <el-input :model-value="data?.videos?.bilibili || ''" @input="setVideos('bilibili', $event)" placeholder="粘贴 BV 号或链接" />
        </el-form-item>
      </div>
    </section>

    <!-- ====== 游戏特性 ====== -->
    <section>
      <h4 class="text-sm font-semibold text-emerald-400 mb-3 border-t border-gray-700 pt-4">游戏特性</h4>
      <el-form-item size="small">
        <el-input :model-value="featuresStr" @input="featuresStr = $event" placeholder="逗号分隔: 单人, 多人, Steam 云, 控制器支持..." type="textarea" :rows="2" />
      </el-form-item>
    </section>

    <!-- ====== 截图库 ====== -->
    <section>
      <h4 class="text-sm font-semibold text-emerald-400 mb-3 border-t border-gray-700 pt-4 flex items-center justify-between">
        截图 ({{ (data.screenshots || []).length }})
        <el-button size="small" type="primary" @click="addScreenshot" plain>+ 添加</el-button>
      </h4>
      <div v-if="(data.screenshots || []).length" class="space-y-2">
        <div v-for="(ss, i) in data.screenshots" :key="i" class="flex items-start gap-2">
          <img v-if="ss" :src="ss" class="w-16 h-10 object-cover rounded border border-gray-600 shrink-0" />
          <span v-else class="w-16 h-10 bg-gray-700 rounded border border-gray-600 shrink-0 flex items-center justify-center text-gray-500 text-xs">无图</span>
          <el-input :model-value="ss" @input="updateScreenshot(i, $event)" placeholder="https://...jpg" size="small" class="flex-1" />
          <el-button size="small" type="danger" @click="removeScreenshot(i)" plain>×</el-button>
        </div>
      </div>
      <div v-else class="text-xs text-gray-500">暂无截图</div>
    </section>

    <!-- ====== DLC ====== -->
    <section>
      <h4 class="text-sm font-semibold text-emerald-400 mb-3 border-t border-gray-700 pt-4 flex items-center justify-between">
        DLC ({{ dlcList.length }})
        <el-button size="small" type="primary" @click="addDlc" plain>+ 添加</el-button>
      </h4>
      <div v-if="dlcList.length" class="space-y-2">
        <div v-for="(d, i) in dlcList" :key="i" class="flex items-center gap-2">
          <el-input :model-value="d.name" @input="updateDlc(i, 'name', $event)" placeholder="DLC 名称" size="small" class="flex-1" />
          <el-input :model-value="d.id" @input="updateDlc(i, 'id', $event)" placeholder="AppID" size="small" style="width:100px" />
          <el-button size="small" type="danger" @click="removeDlc(i)" plain>×</el-button>
        </div>
      </div>
      <div v-else class="text-xs text-gray-500">暂无 DLC</div>
    </section>

    <!-- ====== 语言支持 ====== -->
    <section>
      <h4 class="text-sm font-semibold text-emerald-400 mb-3 border-t border-gray-700 pt-4 flex items-center justify-between">
        语言支持 ({{ langList.length }})
        <el-button size="small" type="primary" @click="addLang" plain>+ 添加</el-button>
      </h4>
      <div v-if="langList.length" class="flex flex-wrap gap-2">
        <span v-for="(lang, i) in langList" :key="i"
          :class="lang.includes('*') ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30' : 'bg-gray-700/50 text-gray-300 border-gray-600/30'"
          class="inline-flex items-center gap-1 text-xs px-2 py-1 rounded-full border cursor-pointer hover:opacity-80 transition"
          @click="toggleAudio(i)">
          <input :value="lang.replace('*','')" @input="updateLang(i, $event.target.value)" class="bg-transparent border-none outline-none w-auto text-inherit text-xs" style="width:80px" />
          <span v-if="lang.includes('*')" class="text-[10px] opacity-70">🎤</span>
          <button type="button" @click.stop="removeLang(i)" class="text-gray-500 hover:text-red-400 ml-0.5">×</button>
        </span>
      </div>
      <div v-else class="text-xs text-gray-500">点击 + 添加语言，点击标签切换全语音标记</div>
    </section>

    <!-- ====== 系统需求 ====== -->
    <section>
      <h4 class="text-sm font-semibold text-emerald-400 mb-3 border-t border-gray-700 pt-4">系统配置要求</h4>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
        <el-form-item label="最低配置" size="small">
          <el-input :model-value="data.min_requirements || ''" @input="set('min_requirements', $event)" type="textarea" :rows="6" placeholder="操作系统: Windows 10&#10;处理器: Intel i5-3570K&#10;内存: 8 GB&#10;显卡: GTX 1050" />
        </el-form-item>
        <el-form-item label="推荐配置" size="small">
          <el-input :model-value="data.rec_requirements || ''" @input="set('rec_requirements', $event)" type="textarea" :rows="6" placeholder="操作系统: Windows 10&#10;处理器: Intel i7-9700K&#10;内存: 16 GB&#10;显卡: RTX 2060" />
        </el-form-item>
      </div>
    </section>

  </div>
</template>

<style scoped>
.game-editor .el-form-item { margin-bottom: 0; }
.game-editor .el-form-item :deep(.el-form-item__label) {
  color: #9ca3af;
  font-size: 12px;
  margin-bottom: 2px;
}
</style>
