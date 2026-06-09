<script setup>
import { ref, watch, computed } from 'vue'
import { createItem, updateItem, uploadImage, associateItemTags, getAdminItem } from '../../api/admin'
import { ElMessage } from 'element-plus'
import { TYPE_LIST, getMeta } from '../../constants/types'
import GameEditorFields from './GameEditorFields.vue'
import MovieEditorFields from './MovieEditorFields.vue'
import AnimeEditorFields from './AnimeEditorFields.vue'
import BoardgameEditorFields from './BoardgameEditorFields.vue'
import ModelEditorFields from './ModelEditorFields.vue'
import BookEditorFields from './BookEditorFields.vue'
import MusicEditorFields from './MusicEditorFields.vue'
import DigitalEditorFields from './DigitalEditorFields.vue'
import CoffeeEditorFields from './CoffeeEditorFields.vue'
import OfflineEditorFields from './OfflineEditorFields.vue'
import GenericEditorFields from './GenericEditorFields.vue'

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

function stringifyInfoJson(obj) {
  return JSON.stringify(obj)
}

const infoObj = computed({
  get: () => parseInfoJson(form.value.infoJson),
  set: (val) => { form.value.infoJson = stringifyInfoJson(val) },
})

// Dynamic fields based on type
const isGameType = computed(() => form.value.type === 'game')
const isMovieType = computed(() => form.value.type === 'movie')
const isOtherType = computed(() => !isGameType.value && !isMovieType.value)

const editorComponent = computed(() => {
  if (isGameType.value) return GameEditorFields
  if (isMovieType.value) return MovieEditorFields
  if (form.value.type === 'anime') return AnimeEditorFields
  if (form.value.type === 'boardgame') return BoardgameEditorFields
  if (form.value.type === 'model') return ModelEditorFields
  if (form.value.type === 'book') return BookEditorFields
  if (form.value.type === 'music') return MusicEditorFields
  if (form.value.type === 'digital') return DigitalEditorFields
  if (form.value.type === 'coffee') return CoffeeEditorFields
  if (form.value.type === 'offline') return OfflineEditorFields
  return GenericEditorFields
})

function initInfoFields() {
  const info = parseInfoJson(form.value.infoJson)
  if (isGameType.value) {
    form.value.infoJson = stringifyInfoJson({
      developer: info.developer || '',
      genre: info.genre || '',
      platform: info.platform || '',
      demo_available: info.demo_available || false,
      demo_url: info.demo_url || '',
      videos: info.videos || { youtube: '', bilibili: '' },
    })
  } else if (isMovieType.value) {
    form.value.infoJson = stringifyInfoJson({
      director: info.director || '',
      year: info.year || '',
      duration: info.duration || '',
      genre: info.genre || '',
      videos: info.videos || { youtube: '', bilibili: '' },
    })
  } else if (form.value.type === 'book') {
    form.value.infoJson = stringifyInfoJson({
      author: info.author || '',
      year: info.year || '',
      pages: info.pages || '',
      category: info.category || '',
      reader_url: info.reader_url || '',
      videos: info.videos || { bilibili: '', youtube: '' },
    })
  } else if (form.value.type === 'music') {
    form.value.infoJson = stringifyInfoJson({
      artist: info.artist || '',
      year: info.year || '',
      genre: info.genre || '',
      tracks: info.tracks || '',
      preview_url: info.preview_url || '',
      videos: info.videos || { bilibili: '', youtube: '' },
    })
  } else if (form.value.type === 'digital') {
    form.value.infoJson = stringifyInfoJson({
      brand: info.brand || '',
      category: info.category || '',
      year: info.year || '',
      features: info.features || '',
      videos: info.videos || { bilibili: '', youtube: '' },
    })
  } else if (form.value.type === 'coffee') {
    form.value.infoJson = stringifyInfoJson({
      origin: info.origin || '',
      roast: info.roast || '',
      process: info.process || '',
      variety: info.variety || '',
      flavor: info.flavor || '',
      videos: info.videos || { bilibili: '', youtube: '' },
    })
  } else if (form.value.type === 'offline') {
    form.value.infoJson = stringifyInfoJson({
      event_type: info.event_type || '',
      venue: info.venue || '',
      date: info.date || '',
      time: info.time || '',
      price: info.price || '',
      capacity: info.capacity || '',
      difficulty: info.difficulty || '',
      highlights: info.highlights || '',
      videos: info.videos || { bilibili: '', youtube: '' },
    })
  } else if (form.value.type === 'anime') {
    form.value.infoJson = stringifyInfoJson({
      studio: info.studio || '',
      year: info.year || '',
      genre: info.genre || '',
      origin: info.origin || '',
      episodes: info.episodes || '',
      videos: info.videos || { bilibili: '', youtube: '' },
    })
  } else if (form.value.type === 'boardgame') {
    form.value.infoJson = stringifyInfoJson({
      players: info.players || '',
      playtime: info.playtime || '',
      weight: info.weight || '',
      rule_text: info.rule_text || '',
      rule_images: info.rule_images || '',
      videos: info.videos || { bilibili: '', youtube: '' },
    })
  } else if (form.value.type === 'model') {
    form.value.infoJson = stringifyInfoJson({
      grade: info.grade || '',
      scale: info.scale || '',
      material: info.material || '',
      series: info.series || '',
      videos: info.videos || { bilibili: '', youtube: '' },
    })
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

function updateInfoField(key, value) {
  const info = parseInfoJson(form.value.infoJson)
  info[key] = value
  form.value.infoJson = stringifyInfoJson(info)
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
      updateInfoField('reader_url', res.url)
    } else {
      form.value[field] = res.url
    }
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '上传失败')
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
watch(() => form.value.type, () => {
  if (props.visible && !isEdit.value) {
    initInfoFields()
  }
})
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑内容' : '新增内容'"
    width="720px"
    @close="handleClose"
    destroy-on-close
  >
    <div class="max-h-[70vh] overflow-y-auto pr-2">
      <!-- Basic Fields -->
      <el-form label-position="top" size="default">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="标题 *" class="col-span-2">
            <el-input v-model="form.title" placeholder="输入标题" @blur="autoSlug" />
          </el-form-item>
          <el-form-item label="Slug *">
            <el-input v-model="form.slug" placeholder="url-friendly-slug" />
          </el-form-item>
          <el-form-item label="品类">
            <el-select v-model="form.type" :disabled="isEdit" :teleported="false" popper-class="admin-select-drop" style="width: 100%">
              <el-option v-for="t in TYPE_LIST" :key="t" :label="getMeta(t).emoji + ' ' + getMeta(t).label" :value="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="描述" class="col-span-2">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="内容描述" />
          </el-form-item>

          <!-- Image fields -->
          <el-form-item label="封面图 URL" class="col-span-2">
            <div class="flex items-center gap-2 w-full">
              <el-input v-model="form.coverUrl" placeholder="https://... 或上传图片" class="flex-1" />
              <label class="el-button el-button--default cursor-pointer">
                上传
                <input type="file" accept="image/*,.webp,.avif,.heic,.heif" class="hidden" @change="handleUpload($event, 'coverUrl')" />
              </label>
            </div>
            <img v-if="form.coverUrl" :src="form.coverUrl" class="w-16 h-22 object-cover rounded mt-2" />
          </el-form-item>

          <el-form-item label="宽封面 URL" class="col-span-2">
            <div class="flex items-center gap-2 w-full">
              <el-input v-model="form.wideCoverUrl" placeholder="https://..." class="flex-1" />
              <label class="el-button el-button--default cursor-pointer">
                上传
                <input type="file" accept="image/*,.webp,.avif,.heic,.heif" class="hidden" @change="handleUpload($event, 'wideCoverUrl')" />
              </label>
            </div>
          </el-form-item>

          <el-form-item label="竖版海报 URL" class="col-span-2">
            <div class="flex items-center gap-2 w-full">
              <el-input v-model="form.posterUrl" placeholder="https://..." class="flex-1" />
              <label class="el-button el-button--default cursor-pointer">
                上传
                <input type="file" accept="image/*,.webp,.avif,.heic,.heif" class="hidden" @change="handleUpload($event, 'posterUrl')" />
              </label>
            </div>
          </el-form-item>

          <el-form-item label="媒体 URL">
            <el-input v-model="form.mediaUrl" placeholder="视频/媒体链接" />
          </el-form-item>
          <el-form-item label="外部链接">
            <el-input v-model="form.externalLink" placeholder="https://..." />
          </el-form-item>
          <el-form-item label="来源">
            <el-input v-model="form.source" placeholder="manual, steam, tmdb..." />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上线" inactive-text="下架" />
          </el-form-item>

          <!-- Tags -->
          <el-form-item label="标签" class="col-span-2">
            <el-select
              v-model="selectedTagIds"
              multiple
              filterable
              placeholder="选择标签"
              :teleported="false"
              popper-class="admin-select-drop"
              style="width: 100%"
            >
              <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id" />
            </el-select>
          </el-form-item>
        </div>

          <el-form-item v-if="form.type === 'book'" label="上传书稿 (PDF/EPUB)" class="col-span-2">
            <label class="el-button el-button--default cursor-pointer">
              选择文件
              <input type="file" accept=".pdf,.epub" class="hidden" @change="handleUpload($event, 'readerUrl')" />
            </label>
            <span class="text-xs text-gray-500 ml-2">{{ infoObj.reader_url || '未上传' }}</span>
          </el-form-item>

          <!-- Dynamic per-category editor -->
        <component :is="editorComponent" v-model="infoObj" :type="form.type" :item-id="currentId" />
      </el-form>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '保存修改' : '创建内容' }}
      </el-button>
    </template>
  </el-dialog>
</template>
