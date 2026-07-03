<script setup>
import { ref, onMounted, watch } from 'vue'
import { getAdminTagsPaged, createAdminTag, deleteAdminTag } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const tags = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const newTagName = ref('')
const searchKeyword = ref('')
const tagSelection = ref(new Set())

async function loadTags() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await getAdminTagsPaged(params)
    tags.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载标签失败，请确认已登录管理员账号')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  loadTags()
}

function handlePageChange() {
  loadTags()
}

async function handleCreate() {
  const name = newTagName.value.trim()
  if (!name) return
  try {
    await createAdminTag(name)
    newTagName.value = ''
    ElMessage.success('标签创建成功')
    await loadTags()
  } catch (e) {
    const msg = e.response?.data?.error || e.response?.data?.message || e.message
    ElMessage.error('创建失败: ' + msg)
  }
}

async function handleDelete(tag) {
  try {
    await ElMessageBox.confirm(`确定删除标签「${tag.name}」？关联的内容将取消该标签。`, '确认删除', { type: 'warning' })
    await deleteAdminTag(tag.id)
    ElMessage.success('标签已删除')
    if (tags.value.length === 1 && page.value > 1) page.value--
    await loadTags()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

function handleTagSelection(rows) { tagSelection.value = new Set(rows.map(r => r.id)) }
async function batchDeleteTags() {
  try {
    await ElMessageBox.confirm(`确定删除 ${tagSelection.value.size} 个标签？`, '批量删除', { type: 'warning' })
    for (const id of tagSelection.value) await deleteAdminTag(id)
    tagSelection.value = new Set(); await loadTags()
    ElMessage.success('已删除')
  } catch (e) { if (e !== 'cancel') ElMessage.error('失败') }
}

let searchTimer = null
watch(searchKeyword, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(handleSearch, 300)
})

onMounted(loadTags)
defineExpose({ refresh: loadTags })
</script>

<template>
  <div v-loading="loading">
    <div class="flex flex-wrap items-center gap-3 mb-4">
      <el-input v-model="newTagName" placeholder="标签名称" size="small" style="width:150px" @keyup.enter="handleCreate" />
      <el-button type="primary" size="small" @click="handleCreate" :disabled="!newTagName.trim()">添加</el-button>
      <div class="flex-1" />
      <el-input v-model="searchKeyword" placeholder="搜索标签" clearable size="small" style="width:140px" />
      <span class="text-sm text-gray-300">共 {{ total }} 个</span>
    </div>

    <el-table :data="tags" style="width: 100%" @selection-change="handleTagSelection">
      <el-table-column type="selection" width="40" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="标签名称" min-width="200" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-popconfirm
            :title="`确定删除「${row.name}」？`"
            confirm-button-text="删除"
            cancel-button-text="取消"
            @confirm="handleDelete(row)"
          >
            <template #reference>
              <el-button type="danger" size="small" text>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="tagSelection.size > 0" class="flex items-center gap-3 mt-3 p-2 bg-blue-900/30 rounded-lg border border-blue-800/50">
      <span class="text-sm text-blue-300">已选 {{ tagSelection.size }} 项</span>
      <el-button size="small" type="danger" @click="batchDeleteTags">批量删除</el-button>
      <el-button size="small" @click="tagSelection = new Set()">取消</el-button>
    </div>

    <div class="flex justify-center mt-4" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>
