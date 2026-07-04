<script setup>
import { ref, watch, computed, defineAsyncComponent } from 'vue'
import { createItem, updateItem, uploadImage, associateItemTags, getAdminItem } from '../../api/admin'
import { fetchSgdbPoster } from '../../api/admin'
import { ElMessage } from 'element-plus'
import { TYPE_LIST, getMeta, TYPE_INFO_DEFAULTS } from '../../constants/types'
import TypeIcon from '../TypeIcon.vue'

const editorComponents = {
  game: defineAsyncComponent(() => import('./GameEditorFields.vue')),
  movie: defineAsyncComponent(() => import('./MovieEditorFields.vue')),
  anime: defineAsyncComponent(() => import('./AnimeEditorFields.vue')),
  boardgame: defineAsyncComponent(() => import('./BoardgameEditorFields.vue')),
  model: defineAsyncComponent(() => import('./ModelEditorFields.vue')),
  book: defineAsyncComponent(() => import('./BookEditorFields.vue')),
  music: defineAsyncComponent(() => import('./MusicEditorFields.vue')),
  digital: defineAsyncComponent(() => import('./DigitalEditorFields.vue')),
  coffee: defineAsyncComponent(() => import('./CoffeeEditorFields.vue')),
  offline: defineAsyncComponent(() => import('./OfflineEditorFields.vue')),
  generic: defineAsyncComponent(() => import('./GenericEditorFields.vue')),
}

const props = defineProps({
  visible: Boolean,
  item: Object,
  allTags: Array,
})

const emit = defineEmits(['update:visible', 'saved'])

const isEdit = computed(() => !!currentId.value)
const formRef = ref(null)
const loading = ref(false)
const form = ref(getDefaultForm())
const formInfo = ref({}) // Local state for infoJson content
const selectedTagIds = ref([])
const itemTags = ref([])
const currentId = ref(null)

function getDefaultForm() {
  return {
    title: '',
    slug: '',
    type: 'game',
    description: '',
    coverUrl: '',
    wideCoverUrl: '',
    posterUrl: '',
    mediaUrl: '',
    externalLink: '',
    externalId: '',
    hotBoost: 0,
    source: 'manual',
    status: 1,
    infoJson: '{}',
  }
}

function parseInfoJson(jsonStr) {
  try {
    return JSON.parse(jsonStr || '{}')
  } catch {
    return {}
  }
}

const editorComponent = computed(() => {
  return editorComponents[form.value.type] || editorComponents.generic
})

function initInfoFields() {
  const existingInfo = parseInfoJson(form.value.infoJson)
  const defaults = TYPE_INFO_DEFAULTS[form.value.type] || {}
  formInfo.value = { ...defaults, ...existingInfo }
  
  // Ensure nested objects like videos exist
  if (defaults.videos) {
    formInfo.value.videos = { ...defaults.videos, ...(existingInfo.videos || {}) }
  }
}

function autoSlug() {
  if (!form.value.slug && form.value.title) {
    form.value.slug = form.value.title
      .toLowerCase()
      .replace(/[^a-z0-9\u4e00-\u9fff]+/g, '-')
      .replace(/^-|-$/g, '')
      + '-' + Date.now().toString(36)
  }
}

// File upload handler
async function handleUpload(event, field) {
  const file = event.target.files[0]
  if (!file) return
  if (!currentId.value) {
    ElMessage.warning('请先保存内容后再上传图片')
    return
  }

  const formData = new FormData()
  formData.append('file', file)
  const typeMap = { coverUrl: 'cover', wideCoverUrl: 'wide_cover', posterUrl: 'poster', readerUrl: 'reader' }
  formData.append('type', typeMap[field] || 'cover')

  try {
    const res = await uploadImage(currentId.value, formData)
    if (field === 'readerUrl') {
      formInfo.value.reader_url = res.url
    } else {
      form.value[field] = res.url
    }
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '上传失败')
  }
}

async function handleFetchSgdbPoster() {
  if (!currentId.value) { ElMessage.warning('请先保存内容'); return }
  try {
    const res = await fetchSgdbPoster(currentId.value)
    if (res.success) {
      form.value.posterUrl = res.posterUrl
      ElMessage.success(res.message || '封面已拉取')
    } else {
      ElMessage.warning(res.message || '未找到封面')
    }
  } catch (e) {
    const msg = e.response?.data?.message || e.response?.data?.error || '网络错误，请重试'
    ElMessage.error(msg)
  }
}

async function handleSubmit() {
  if (!form.value.title || !form.value.slug) {
    ElMessage.warning('标题和 slug 为必填项')
    return
  }

  loading.value = true
  try {
    const payload = { ...form.value }
    // Stringify local formInfo back into infoJson
    payload.infoJson = JSON.stringify(formInfo.value)
    
    delete payload.id
    delete payload.createdAt
    delete payload.updatedAt
    delete payload.__v_isRef
    delete payload.__v_raw

    let savedItem
    if (isEdit.value) {
      savedItem = await updateItem(currentId.value, payload)
    } else {
      savedItem = await createItem(payload)
      currentId.value = savedItem.id
    }

    if (selectedTagIds.value.length > 0) {
      await associateItemTags(savedItem.id, selectedTagIds.value)
    }

    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    emit('saved')
  } catch (e) {
    if (e.code === 'ECONNABORTED' || e.message?.includes('timeout')) {
      ElMessage.warning('请求超时，但数据可能已保存——请刷新页面确认')
      emit('saved')
    } else {
      ElMessage.error('保存失败: ' + (e.response?.data?.error || e.response?.data?.message || e.message))
    }
  } finally {
    loading.value = false
  }
}

function handleClose() {
  emit('update:visible', false)
}

// Load item details with tags when editing
async function loadItemDetails() {
  if (!props.item?.id) return
  try {
    const res = await getAdminItem(props.item.id)
    if (res.tags) {
      itemTags.value = res.tags
      selectedTagIds.value = res.tags.map(t => t.id)
    }
  } catch (e) {
    console.warn('Failed to load item tags, form is still editable:', e.message)
  }
}

// Watch for dialog open
watch(() => props.visible, (val) => {
  if (val) {
    currentId.value = props.item?.id || null
    if (props.item) {
      form.value = { ...getDefaultForm(), ...props.item }
      loadItemDetails()
    } else {
      form.value = getDefaultForm()
      selectedTagIds.value = []
      itemTags.value = []
    }
    initInfoFields()
  }
})

// Watch type changes to init info fields
watch(() => form.value.type, (newType, oldType) => {
  if (props.visible && newType !== oldType) {
    initInfoFields()
  }
})
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑内容' : '新增内容'"
    width="92vw"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="handleClose"
    destroy-on-close
  >
    <div class="admin-item-form max-h-[75vh] overflow-y-auto pr-2">
      <el-form label-position="top">
        <!-- ===== 基本信息 ===== -->
        <h5 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3 mt-2">基本信息</h5>
        <div class="grid grid-cols-4 gap-3 mb-4">
          <el-form-item label="标题 *" class="col-span-4">
            <el-input v-model="form.title" placeholder="输入标题" @blur="autoSlug" />
          </el-form-item>
          <el-form-item label="Slug *" class="col-span-2">
            <el-input v-model="form.slug" placeholder="url-friendly-slug" />
          </el-form-item>
          <el-form-item label="品类">
            <template v-if="isEdit">
              <span class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium border"
                :class="`bg-gray-700/50 text-gray-200 border-gray-600`">
                <TypeIcon :type="form.type" size="16" /> {{ getMeta(form.type).label }}
              </span>
            </template>
            <div v-else class="flex flex-wrap gap-1.5">
              <button v-for="t in TYPE_LIST" :key="t" @click="form.type = t; $event.preventDefault()"
                :class="form.type === t
                  ? 'bg-blue-600 text-white border-blue-500 ring-1 ring-blue-400'
                  : 'bg-gray-700 text-gray-300 border-gray-600 hover:bg-gray-600 hover:text-white'"
                class="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs border transition-colors">
                <TypeIcon :type="t" size="14" /> {{ getMeta(t).label }}
              </button>
            </div>
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上线" inactive-text="下架" />
          </el-form-item>
          <el-form-item label="描述" class="col-span-4">
            <el-input v-model="form.description" type="textarea" :rows="2" placeholder="内容描述" />
          </el-form-item>
          <el-form-item label="来源">
            <el-input v-model="form.source" placeholder="steam, igdb, manual..." />
          </el-form-item>
          <el-form-item label="外部链接">
            <el-input v-model="form.externalLink" placeholder="https://..." />
          </el-form-item>
          <el-form-item label="媒体 URL">
            <el-input v-model="form.mediaUrl" placeholder="视频链接" />
          </el-form-item>
          <el-form-item label="External ID">
            <el-input v-model="form.externalId" placeholder="AppID / TMDB ID" />
          </el-form-item>
          <el-form-item label="热门加权">
            <el-input-number v-model="form.hotBoost" :min="0" :step="50" style="width:100%" />
          </el-form-item>
        </div>

        <!-- ===== 封面图 ===== -->
        <h5 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3 mt-4">封面图</h5>
        <div class="grid grid-cols-3 gap-3 mb-4">
          <el-form-item label="横版封面">
            <el-input v-model="form.coverUrl" placeholder="https://... 或上传" />
            <label class="el-button el-button--default cursor-pointer mt-1" style="width:100%">上传
              <input type="file" accept="image/*,.webp" class="hidden" @change="handleUpload($event, 'coverUrl')" />
            </label>
            <img v-if="form.coverUrl" :src="form.coverUrl" class="w-full aspect-video object-cover rounded mt-1 border border-gray-600" />
          </el-form-item>
          <el-form-item label="宽封面">
            <el-input v-model="form.wideCoverUrl" placeholder="https://... 或上传" />
            <label class="el-button el-button--default cursor-pointer mt-1" style="width:100%">上传
              <input type="file" accept="image/*,.webp" class="hidden" @change="handleUpload($event, 'wideCoverUrl')" />
            </label>
            <img v-if="form.wideCoverUrl" :src="form.wideCoverUrl" class="w-full aspect-video object-cover rounded mt-1 border border-gray-600" />
          </el-form-item>
          <el-form-item label="竖版海报">
            <el-input v-model="form.posterUrl" placeholder="https://... 或上传" />
            <div class="flex gap-1 mt-1">
              <label class="el-button el-button--default cursor-pointer flex-1">上传
                <input type="file" accept="image/*,.webp" class="hidden" @change="handleUpload($event, 'posterUrl')" />
              </label>
              <button type="button" class="el-button el-button--primary flex-1" @click="handleFetchSgdbPoster">SGDB 拉取</button>
            </div>
            <img v-if="form.posterUrl" :src="form.posterUrl" class="w-full object-cover rounded mt-1 border border-gray-600" style="aspect-ratio: 2/3" />
          </el-form-item>
        </div>

        <!-- ===== 标签 ===== -->
        <el-form-item label="标签">
          <el-select v-model="selectedTagIds" multiple filterable placeholder="搜索或选择标签" :teleported="false" style="width:100%">
            <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id" />
          </el-select>
        </el-form-item>

        <!-- ===== 书稿上传 ===== -->
        <el-form-item v-if="form.type === 'book'" label="上传书稿 (PDF/EPUB)">
          <label class="el-button el-button--default cursor-pointer">选择文件
            <input type="file" accept=".pdf,.epub" class="hidden" @change="handleUpload($event, 'readerUrl')" />
          </label>
          <span class="text-xs text-gray-500 ml-2">{{ formInfo.reader_url || '未上传' }}</span>
        </el-form-item>

        <!-- ===== 品类详情编辑器 ===== -->
        <component :is="editorComponent" :info="formInfo" :type="form.type" :item-id="currentId" />
      </el-form>
    </div>

    <template #footer>
      <el-button type="danger" @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '保存修改' : '创建内容' }}
      </el-button>
    </template>
  </el-dialog>
</template>
