<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminItems, getAdminSceneItems, getAdminScenes, createAdminScene, updateAdminScene, saveAdminSceneItems, deleteAdminScene } from '../../api/admin'

const scenes = ref([])
const candidates = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const selectedIds = ref([])
const form = ref(emptyForm())

function emptyForm() {
  return { slug: '', title: '', description: '', coverUrl: '', constraintsJson: '', visible: 0, displayOrder: 0, items: [] }
}

async function loadScenes() {
  loading.value = true
  try { scenes.value = await getAdminScenes() || [] } catch (error) { console.error(error) }
  finally { loading.value = false }
}

async function loadCandidates() {
  const result = await getAdminItems({ status: 1, size: 100, page: 1 })
  candidates.value = result.records || []
}

async function openCreate() {
  editingId.value = null
  form.value = emptyForm()
  selectedIds.value = []
  try { await loadCandidates(); dialogVisible.value = true } catch (error) { console.error(error) }
}

async function openEdit(scene) {
  editingId.value = scene.id
  form.value = { ...scene, constraintsJson: scene.constraintsJson || '', items: [] }
  try {
    const [items] = await Promise.all([getAdminSceneItems(scene.id), loadCandidates()])
    form.value.items = (items || []).map((item) => ({ itemId: item.id, reason: item.reason, editorNote: item.editorNote || '', title: item.title, type: item.type, status: item.status }))
    selectedIds.value = form.value.items.map((item) => item.itemId)
    dialogVisible.value = true
  } catch (error) { console.error(error) }
}

function syncItems(ids) {
  const previous = new Map(form.value.items.map((item) => [item.itemId, item]))
  form.value.items = ids.map((id) => previous.get(id) || candidateToItem(id)).filter(Boolean)
}

function candidateToItem(id) {
  const item = candidates.value.find((candidate) => candidate.id === id)
  return item && { itemId: item.id, title: item.title, type: item.type, status: item.status, reason: '', editorNote: '' }
}

function moveItem(index, delta) {
  const target = index + delta
  if (target < 0 || target >= form.value.items.length) return
  const items = [...form.value.items]
  ;[items[index], items[target]] = [items[target], items[index]]
  form.value.items = items
}

async function save() {
  if (!form.value.slug || !form.value.title || !form.value.description) {
    ElMessage.warning('请填写 slug、标题和简介')
    return
  }
  saving.value = true
  try {
    const { items, ...scene } = form.value
    const saved = editingId.value ? await updateAdminScene(editingId.value, scene) : await createAdminScene(scene)
    await saveAdminSceneItems(saved.id, items.map(({ itemId, reason, editorNote }) => ({ itemId, reason, editorNote })))
    ElMessage.success('场景已保存，发布前请完成内容核验')
    dialogVisible.value = false
    await loadScenes()
  } catch (error) { console.error(error) }
  finally { saving.value = false }
}

async function remove(scene) {
  try {
    await ElMessageBox.confirm(`确定删除场景「${scene.title}」？`, '删除场景', { type: 'warning' })
    await deleteAdminScene(scene.id)
    ElMessage.success('场景已删除')
    await loadScenes()
  } catch (error) { if (error !== 'cancel') console.error(error) }
}

onMounted(loadScenes)
defineExpose({ refresh: loadScenes })
</script>

<template>
  <section>
    <div class="flex items-center justify-between mb-5"><div><h3 class="text-lg font-semibold text-white">场景策展</h3><p class="text-sm text-gray-400 mt-1">草稿可先保存；发布前须核验每条行动链接与入选理由。</p></div><el-button type="primary" @click="openCreate">新建场景</el-button></div>
    <el-table v-loading="loading" :data="scenes" empty-text="还没有场景草稿" class="scene-table">
      <el-table-column prop="displayOrder" label="排序" width="70" />
      <el-table-column prop="title" label="场景"><template #default="{ row }"><p class="text-white">{{ row.title }}</p><code class="text-xs text-gray-500">{{ row.slug }}</code></template></el-table-column>
      <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.visible ? 'success' : 'info'">{{ row.visible ? '已发布' : '草稿' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="150"><template #default="{ row }"><el-button size="small" @click="openEdit(row)">编辑</el-button><el-button size="small" type="danger" plain @click="remove(row)">删除</el-button></template></el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑场景' : '新建场景'" width="min(92vw, 820px)" destroy-on-close>
      <el-form label-position="top" class="scene-form"><div class="grid grid-cols-1 sm:grid-cols-2 gap-x-4"><el-form-item label="场景标题" required><el-input v-model="form.title" /></el-form-item><el-form-item label="Slug" required><el-input v-model="form.slug" placeholder="solo-quick-start" /></el-form-item></div><el-form-item label="简介" required><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item><div class="grid grid-cols-1 sm:grid-cols-2 gap-x-4"><el-form-item label="封面链接"><el-input v-model="form.coverUrl" /></el-form-item><el-form-item label="首页排序"><el-input-number v-model="form.displayOrder" :min="0" class="w-full" /></el-form-item></div><el-form-item label="场景约束 JSON"><el-input v-model="form.constraintsJson" type="textarea" :rows="4" placeholder='{"people":{"min":1,"max":1}}' /></el-form-item><el-form-item label="前台可见"><el-switch v-model="form.visible" :active-value="1" :inactive-value="0" active-text="已核验并发布" inactive-text="草稿" /></el-form-item></el-form>
      <section class="border-t border-gray-700 pt-4"><h4 class="text-sm font-semibold text-white">入选条目与理由</h4><p class="mt-1 text-xs text-gray-500">条目按下列顺序展示；草稿可为空，但已发布场景必须完成核验。</p><el-select v-model="selectedIds" multiple filterable class="mt-3 w-full" placeholder="选择已发布内容" @change="syncItems"><el-option v-for="item in candidates" :key="item.id" :value="item.id" :label="`${item.title} · ${item.type}`" /></el-select><div v-if="form.items.length" class="mt-3 space-y-3"><div v-for="(item, index) in form.items" :key="item.itemId" class="rounded-lg border border-gray-700 p-3"><div class="flex gap-2 items-center"><span class="text-xs text-gray-500 w-5">{{ index + 1 }}</span><strong class="flex-1 text-sm text-gray-100">{{ item.title }}</strong><el-button size="small" :disabled="index === 0" @click="moveItem(index, -1)">↑</el-button><el-button size="small" :disabled="index === form.items.length - 1" @click="moveItem(index, 1)">↓</el-button></div><el-input v-model="item.reason" class="mt-2" placeholder="为什么它适合这个场景？" /><el-input v-model="item.editorNote" class="mt-2" type="textarea" :rows="2" placeholder="仅后台可见的编辑备注" /></div></div></section>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></template>
    </el-dialog>
  </section>
</template>

<style scoped>
.scene-table :deep(.el-table__inner-wrapper), .scene-table :deep(.el-table__body-wrapper) { background: rgb(17 24 39); }
.scene-table :deep(th.el-table__cell), .scene-table :deep(td.el-table__cell) { background: transparent; border-color: rgb(55 65 81); }
</style>
