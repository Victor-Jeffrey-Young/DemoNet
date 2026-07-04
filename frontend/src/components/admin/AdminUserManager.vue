<script setup>
import { ref, onMounted, computed } from 'vue'
import { getAdminUsers, toggleUserBan, resetUserPassword } from '../../api/admin'
import { useAuthStore } from '../../stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@iconify/vue'

const auth = useAuthStore()
const users = ref([])
const loading = ref(false)
const searchQuery = ref('')
const currentUserId = auth.user?.id

onMounted(loadUsers)
async function loadUsers() { 
  loading.value = true
  try { users.value = await getAdminUsers() || [] } 
  catch {} 
  finally { loading.value = false }
}

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  const q = searchQuery.value.toLowerCase()
  return users.value.filter(u => u.username?.toLowerCase().includes(q) || u.email?.toLowerCase().includes(q))
})

const stats = computed(() => ({
  total: users.value.length,
  admins: users.value.filter(u => u.role === 'ADMIN').length,
  banned: users.value.filter(u => u.enabled === 0).length
}))

const canBan = (user) => user.id !== currentUserId && user.role !== 'ADMIN'

async function handleBan(user) {
  const action = user.enabled === 1 ? '封禁' : '解封'
  try {
    await ElMessageBox.confirm(`确定${action}用户「${user.username}」？`, action, { type: 'warning' })
    await toggleUserBan(user.id, user.enabled === 1 ? 0 : 1)
    ElMessage.success(`已${action}`)
    await loadUsers()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.error || e.response?.data?.message || '操作失败') }
}
async function handleResetPassword(user) {
  try {
    await ElMessageBox.confirm(`确定重置「${user.username}」的密码？`, '重置密码', { type: 'warning' })
    const res = await resetUserPassword(user.id)
    ElMessageBox.alert(res.message, '密码重置成功', { type: 'success' })
  } catch (e) { if (e !== 'cancel') ElMessage.error('重置失败') }
}
defineExpose({ refresh: loadUsers })
</script>

<template>
  <div class="space-y-6">
    <!-- Header Stats -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
      <div class="bg-gray-800 rounded-lg p-5 border border-gray-700 flex items-center justify-between shadow-sm">
        <div>
          <div class="text-sm text-gray-400 mb-1">总用户数</div>
          <div class="text-3xl font-bold text-gray-100">{{ stats.total }}</div>
        </div>
        <div class="p-3 bg-blue-500/10 rounded-full text-blue-400"><Icon icon="lucide:users" class="w-8 h-8" /></div>
      </div>
      <div class="bg-gray-800 rounded-lg p-5 border border-gray-700 flex items-center justify-between shadow-sm">
        <div>
          <div class="text-sm text-gray-400 mb-1">管理员权限</div>
          <div class="text-3xl font-bold text-emerald-400">{{ stats.admins }}</div>
        </div>
        <div class="p-3 bg-emerald-500/10 rounded-full text-emerald-400"><Icon icon="lucide:shield-check" class="w-8 h-8" /></div>
      </div>
      <div class="bg-gray-800 rounded-lg p-5 border border-gray-700 flex items-center justify-between shadow-sm">
        <div>
          <div class="text-sm text-gray-400 mb-1">已封禁 (黑名单)</div>
          <div class="text-3xl font-bold text-red-400">{{ stats.banned }}</div>
        </div>
        <div class="p-3 bg-red-500/10 rounded-full text-red-400"><Icon icon="lucide:ban" class="w-8 h-8" /></div>
      </div>
    </div>

    <!-- Toolbar -->
    <div class="flex flex-col md:flex-row md:items-center justify-between bg-gray-800 p-4 rounded-lg border border-gray-700 gap-4">
      <el-input v-model="searchQuery" placeholder="搜索用户名或邮箱..." class="w-full md:w-80" clearable />
      <el-button type="primary" @click="loadUsers" :loading="loading" plain class="w-full md:w-auto">刷新数据</el-button>
    </div>

    <!-- Table -->
    <el-table :data="filteredUsers" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column label="用户" min-width="200">
        <template #default="{ row }">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-full bg-indigo-500 flex items-center justify-center text-white font-bold text-sm uppercase shadow-sm shrink-0">
              {{ row.username.substring(0, 2) }}
            </div>
            <div class="flex flex-col min-w-0">
              <span class="font-medium text-gray-200 truncate">{{ row.username }}</span>
              <span class="text-xs text-gray-500 truncate">{{ row.email }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="角色" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" effect="dark" class="w-16 text-center">
            {{ row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册日期" width="110" align="center">
        <template #default="{ row }">
          <span class="text-gray-400 text-xs">{{ row.created_at?.substring(0,10) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="账号状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'danger'" effect="plain">
            {{ row.enabled === 1 ? '正常' : '封禁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="{ row }">
          <el-button size="small" :type="row.enabled === 1 ? 'warning' : 'success'" plain @click="handleBan(row)" :disabled="!canBan(row)">
            {{ row.enabled === 1 ? '封禁' : '解封' }}
          </el-button>
          <el-button size="small" type="primary" plain @click="handleResetPassword(row)">重置</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
