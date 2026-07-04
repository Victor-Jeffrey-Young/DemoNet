<script setup>
import { computed } from 'vue'
import { uploadImage } from '../../api/admin'
import { ElMessage } from 'element-plus'
import TypeIcon from '../TypeIcon.vue'
import AdminVideoFields from './AdminVideoFields.vue'
defineOptions({ inheritAttrs: false })

const props = defineProps({ info: Object, itemId: Number })

// ---- Screenshots ----
function addScreenshot() {
  if (!Array.isArray(props.info.screenshots)) props.info.screenshots = []
  props.info.screenshots.push('')
}
function removeScreenshot(idx) {
  props.info.screenshots.splice(idx, 1)
}
async function uploadScreenshot(idx, event) {
  const file = event.target.files[0]
  if (!file || !props.itemId) { ElMessage.warning('请先保存内容后再上传截图'); return }
  const fd = new FormData(); fd.append('file', file); fd.append('type', 'cover')
  try {
    const res = await uploadImage(props.itemId, fd)
    props.info.screenshots[idx] = res.url
    ElMessage.success('上传成功')
  } catch (e) { ElMessage.error(e.response?.data?.error || '上传失败') }
}

// ---- DLC ----
const dlcList = computed({
  get: () => {
    const d = props.info.dlc
    if (Array.isArray(d)) return d.map(x => typeof x === 'object' ? { ...x } : { id: x, name: 'App ' + x })
    try { return typeof d === 'string' ? JSON.parse(d) : [] } catch { return [] }
  },
  set: (val) => { props.info.dlc = val }
})
function addDlc() { dlcList.value = [...dlcList.value, { id: '', name: '' }] }
function removeDlc(idx) {
  const d = [...dlcList.value]
  d.splice(idx, 1)
  dlcList.value = d
}
function updateDlc(idx, field, val) {
  const d = [...dlcList.value]
  d[idx] = { ...d[idx], [field]: val }
  dlcList.value = d
}

// ---- Languages ----
const langList = computed({
  get: () => {
    const raw = props.info.languages || ''
    return raw ? raw.split(',').map(s => s.trim()) : []
  },
  set: (val) => { props.info.languages = val.join(', ') }
})
function addLang() {
  langList.value = [...langList.value, '新语言']
}
function updateLang(i, val) {
  const langs = [...langList.value]
  langs[i] = val
  langList.value = langs
}
function removeLang(i) {
  const langs = [...langList.value]
  langs.splice(i, 1)
  langList.value = langs
}
function toggleAudio(i) {
  const langs = [...langList.value]
  const name = langs[i].replace('*', '').trim()
  langs[i] = langs[i].includes('*') ? name : name + '*'
  langList.value = langs
}

function toggleFree(v) {
  props.info.free = v
  if (v) props.info.price = 'Free'
  else props.info.price = ''
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
        <el-form-item label="开发商" class="col-span-2">
          <el-input v-model="info.developer" placeholder="Supergiant Games" />
        </el-form-item>
        <el-form-item label="发行商" class="col-span-2">
          <el-input v-model="info.publisher" placeholder="发行商" />
        </el-form-item>
        <el-form-item label="发行日期">
          <el-input v-model="info.release_date" placeholder="2024-05-06" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="info.price" placeholder="CNY 268.00" :disabled="!!info.free" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="info.genre" placeholder="Action, RPG" />
        </el-form-item>
        <el-form-item label="平台">
          <el-input v-model="info.platform" placeholder="PC, PS5, Xbox" />
        </el-form-item>
        <el-form-item label="免费">
          <el-checkbox :model-value="!!info.free" @update:model-value="toggleFree" />
        </el-form-item>
        <el-form-item label="试玩版">
          <el-checkbox v-model="info.demo_available" />
        </el-form-item>
      </div>
      <el-form-item label="DEMO 链接" class="mt-2" v-if="info.demo_available">
        <el-input v-model="info.demo_url" placeholder="steam://install/APPID 或 https://..." />
      </el-form-item>
      <div class="flex items-center gap-4 mt-2">
        <el-form-item label="性能测试工具">
          <el-checkbox v-model="info.has_benchmark" />
        </el-form-item>
      </div>
      <el-form-item label="测试工具链接" v-if="info.has_benchmark">
        <el-input v-model="info.benchmark_url" placeholder="steam://run/APPID 或 https://..." />
      </el-form-item>
    </section>

    <!-- ====== 视频 ====== -->
    <section>
      <h4 class="text-sm font-semibold text-emerald-400 mb-3 border-t border-gray-700 pt-4">视频</h4>
      <AdminVideoFields v-model="info.videos" :show-steam="true" />
    </section>

    <!-- ====== 截图库 ====== -->
    <section>
      <h4 class="text-sm font-semibold text-emerald-400 mb-3 border-t border-gray-700 pt-4 flex items-center justify-between">
        截图 ({{ (info.screenshots || []).length }})
        <el-button type="primary" @click="addScreenshot" plain>+ 添加</el-button>
      </h4>
      <div v-if="(info.screenshots || []).length" class="space-y-2">
        <div v-for="(ss, i) in info.screenshots" :key="i" class="flex items-start gap-2">
          <img v-if="ss" :src="ss" class="w-16 h-10 object-cover rounded border border-gray-600 shrink-0" />
          <span v-else class="w-16 h-10 bg-gray-700 rounded border border-gray-600 shrink-0 flex items-center justify-center text-gray-500 text-xs">无图</span>
          <el-input v-model="info.screenshots[i]" placeholder="https://...jpg 或上传" class="flex-1" />
          <label class="el-button el-button--default cursor-pointer shrink-0">本地上传
            <input type="file" accept="image/*,.webp" class="hidden" @change="uploadScreenshot(i, $event)" />
          </label>
          <el-button type="danger" @click="removeScreenshot(i)" plain>×</el-button>
        </div>
      </div>
      <div v-else class="text-xs text-gray-500">暂无截图</div>
    </section>

    <!-- ====== DLC ====== -->
    <section>
      <h4 class="text-sm font-semibold text-emerald-400 mb-3 border-t border-gray-700 pt-4 flex items-center justify-between">
        DLC ({{ dlcList.length }})
        <el-button type="primary" @click="addDlc" plain>+ 添加</el-button>
      </h4>
      <div v-if="dlcList.length" class="space-y-2">
        <div v-for="(d, i) in dlcList" :key="i" class="flex items-center gap-2">
          <el-input :model-value="d.name" @input="updateDlc(i, 'name', $event)" placeholder="DLC 名称" class="flex-1" />
          <el-input :model-value="d.id" @input="updateDlc(i, 'id', $event)" placeholder="AppID" style="width:100px" />
          <el-button type="danger" @click="removeDlc(i)" plain>×</el-button>
        </div>
      </div>
      <div v-else class="text-xs text-gray-500">暂无 DLC</div>
    </section>

    <!-- ====== 语言支持 ====== -->
    <section>
      <h4 class="text-sm font-semibold text-emerald-400 mb-3 border-t border-gray-700 pt-4 flex items-center justify-between">
        语言支持 ({{ langList.length }})
        <el-button type="primary" @click="addLang" plain>+ 添加</el-button>
      </h4>
      <div v-if="langList.length" class="flex flex-wrap gap-2">
        <span v-for="(lang, i) in langList" :key="i"
          :class="lang.includes('*') ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30' : 'bg-gray-700/50 text-gray-300 border-gray-600/30'"
          class="inline-flex items-center gap-1 text-xs px-2 py-1 rounded-full border cursor-pointer hover:opacity-80 transition"
          @click="toggleAudio(i)">
          <input :value="lang.replace('*','')" @input="updateLang(i, $event.target.value)" :placeholder="lang ? '' : '输入...'" class="bg-transparent border-none outline-none text-inherit text-xs" style="min-width:60px" />
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
        <el-form-item label="最低配置">
          <el-input v-model="info.min_requirements" type="textarea" :rows="6" placeholder="操作系统: Windows 10&#10;处理器: Intel i5-3570K&#10;内存: 8 GB&#10;显卡: GTX 1050" />
        </el-form-item>
        <el-form-item label="推荐配置">
          <el-input v-model="info.rec_requirements" type="textarea" :rows="6" placeholder="操作系统: Windows 10&#10;处理器: Intel i7-9700K&#10;内存: 16 GB&#10;显卡: RTX 2060" />
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
