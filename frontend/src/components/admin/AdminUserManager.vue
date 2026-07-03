<script setup>
import { ref, onMounted } from 'vue'
import { getAdminUsers, toggleUserBan, resetUserPassword } from '../../api/admin'
import { useAuthStore } from '../../stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const auth = useAuthStore()
const users = ref([])
const currentUserId = auth.user?.id

onMounted(loadUsers)
async function loadUsers() { try { users.value = await getAdminUsers() || [] } catch {} }

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
    ElMessage.success({ message: `新密码: ${res.password}`, duration: 10000 })
  } catch (e) { if (e !== 'cancel') ElMessage.error('重置失败') }
}
defineExpose({ refresh: loadUsers })
</script>

<template>
  <div style="width:85%">
    <div class="flex items-center justify-between mb-4">
      <span class="text-sm text-gray-400">共 {{ users.length }} 个用户</span>
      <el-button size="small" @click="loadUsers">刷新</el-button>
    </div>
    <el-table :data="users" style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role==='ADMIN'?'danger':'info'" size="small">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="110">
        <template #default="{ row }">{{ row.created_at?.substring(0,10) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.enabled===1?'success':'danger'" size="small">{{ row.enabled===1?'正常':'封禁' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" :type="row.enabled===1?'warning':'success'" @click="handleBan(row)" :disabled="!canBan(row)">
            {{ row.enabled===1 ? '封禁' : '解封' }}
          </el-button>
          <el-button size="small" type="primary" @click="handleResetPassword(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
