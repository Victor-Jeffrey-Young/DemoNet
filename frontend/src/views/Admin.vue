<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AdminDashboard from '../components/admin/AdminDashboard.vue'
import AdminItemList from '../components/admin/AdminItemList.vue'
import AdminTagManager from '../components/admin/AdminTagManager.vue'
import AdminCarouselManager from '../components/admin/AdminCarouselManager.vue'
import AdminFetchPanel from '../components/admin/AdminFetchPanel.vue'
import AdminCategoryManager from '../components/admin/AdminCategoryManager.vue'
import AdminUserManager from '../components/admin/AdminUserManager.vue'
import { getAppSettings, updateAppSetting, getInviteCodes, generateInviteCodes } from '../api/admin'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const route = useRoute()
const activeTab = ref('dashboard')

// ── Settings ──
const settings = ref({})
const settingsLoading = ref(false)
async function loadSettings() {
  settingsLoading.value = true
  try { settings.value = await getAppSettings() || {} } catch (e) { console.error(e) }
  finally { settingsLoading.value = false }
}
async function saveSetting(key) {
  try {
    await updateAppSetting(key, settings.value[key] || '')
    ElMessage.success('已保存')
  } catch (e) { ElMessage.error('保存失败') }
}

// ── Invite codes ──
const inviteCodes = ref([])
async function loadInviteCodes() {
  try { inviteCodes.value = await getInviteCodes() || [] } catch {}
}
async function handleGenerateInviteCodes() {
  try {
    const res = await generateInviteCodes(5)
    ElMessage.success(res.message)
    await loadInviteCodes()
  } catch (e) { ElMessage.error('生成失败') }
}

const dashboardRef = ref(null)
const itemListRef = ref(null)
const tagManagerRef = ref(null)
const carouselRef = ref(null)
const fetchPanelRef = ref(null)
const userManagerRef = ref(null)

function handleTabChange(tab) {
  switch (tab) {
    case 'dashboard': dashboardRef.value?.refresh(); break
    case 'items': itemListRef.value?.refresh(); break
    case 'tags': tagManagerRef.value?.refresh(); break
    case 'carousel': carouselRef.value?.refresh(); break
    case 'fetch': fetchPanelRef.value?.refresh(); break
    case 'users': userManagerRef.value?.refresh(); break
    case 'settings': loadSettings(); loadInviteCodes(); break
  }
}

onMounted(() => {
  if (route.query.edit) {
    sessionStorage.setItem('adminEditId', route.query.edit)
    activeTab.value = 'items'
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-950 text-white">
    <main class="max-w-[90%] mx-auto px-6 py-8">
      <!-- Header -->
      <div class="flex items-center justify-between mb-6 pb-4 border-b border-gray-800">
        <div>
          <h1 class="text-2xl font-bold text-white">管理后台</h1>
          <p class="text-xs text-gray-400 mt-1">内容 · 标签 · 轮播 · 抓取</p>
        </div>
        <div class="flex items-center gap-3 text-sm text-gray-300">
          <span>{{ auth.user?.username }}</span>
          <el-tag type="warning" >ADMIN</el-tag>
        </div>
      </div>

      <!-- Tabs -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="admin-tabs">
        <el-tab-pane label="仪表盘" name="dashboard">
          <AdminDashboard ref="dashboardRef" />
        </el-tab-pane>
        <el-tab-pane label="内容管理" name="items">
          <AdminItemList ref="itemListRef" />
        </el-tab-pane>
        <el-tab-pane label="标签管理" name="tags">
          <AdminTagManager ref="tagManagerRef" />
        </el-tab-pane>
        <el-tab-pane label="轮播管理" name="carousel">
          <AdminCarouselManager ref="carouselRef" />
        </el-tab-pane>
        <el-tab-pane label="数据抓取" name="fetch">
          <AdminFetchPanel ref="fetchPanelRef" />
        </el-tab-pane>
        <el-tab-pane label="频道管理" name="categories">
          <AdminCategoryManager />
        </el-tab-pane>
        <el-tab-pane label="用户管理" name="users">
          <AdminUserManager ref="userManagerRef" />
        </el-tab-pane>
        <el-tab-pane label="API 设置" name="settings">
          <div v-loading="settingsLoading" class="max-w-2xl">
            <p class="text-sm text-gray-400 mb-4">配置第三方 API 密钥。保存后即时生效，无需重启。</p>
            <div v-for="(val, key) in settings" :key="key" class="flex items-center gap-3 mb-3" v-show="key !== 'INVITE_ONLY'">
              <label class="text-xs text-gray-400 font-mono w-48 shrink-0">{{ key }}</label>
              <el-input
                :model-value="val"
                @input="v => settings[key] = v"
                :placeholder="'输入 ' + key"
                
                type="password"
                show-password
                class="flex-1"
              />
              <el-button  type="primary" @click="saveSetting(key)">保存</el-button>
            </div>

            <h5 class="text-sm font-semibold text-gray-300 mt-8 mb-3 pt-4 border-t border-gray-700">邀请码管理</h5>
            <div class="flex items-center gap-4 mb-4">
              <span class="text-xs text-gray-400">启用邀请注册</span>
              <el-switch
                :model-value="settings.INVITE_ONLY === 'true'"
                @update:model-value="v => { settings.INVITE_ONLY = v ? 'true' : 'false'; saveSetting('INVITE_ONLY') }"
                
              />
              <span class="text-xs text-gray-500">{{ settings.INVITE_ONLY === 'true' ? '已开启（注册需邀请码）' : '已关闭（注册无需邀请码）' }}</span>
            </div>
            <div class="flex items-center gap-3 mb-3">
              <span class="text-xs text-gray-400">共 {{ inviteCodes.length }} 个邀请码</span>
              <el-button  type="primary" @click="handleGenerateInviteCodes">生成 5 个</el-button>
              <el-button  @click="loadInviteCodes">刷新</el-button>
            </div>
            <div v-if="inviteCodes.length" class="space-y-2 max-h-60 overflow-y-auto">
              <div v-for="c in inviteCodes" :key="c.id" class="flex items-center gap-3 text-xs">
                <code class="font-mono text-blue-300 bg-gray-800 px-2 py-1 rounded w-32">{{ c.code }}</code>
                <span v-if="c.used_by" class="text-gray-500">已使用 · {{ c.used_by_name || c.used_by }}</span>
                <span v-else class="text-green-400">未使用</span>
                <span class="text-gray-600 ml-auto">{{ c.created_at?.substring(0,10) }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </main>
  </div>
</template>

<style>
/* ===== Safari subpixel rendering fix — prevent alternating-row blur ===== */
.admin-tabs {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
.admin-tabs .el-table__body tr,
.admin-tabs .el-table__body td {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Fix Element Plus tab content clipping — visible overflow on all tab containers */
.admin-tabs .el-tabs__header,
.admin-tabs .el-tabs__nav-wrap,
.admin-tabs .el-tabs__nav-scroll,
.admin-tabs .el-tabs__nav,
.admin-tabs .el-tabs__content,
.admin-tabs .el-tab-pane {
  overflow: visible !important;
  overflow-x: visible !important;
}
.admin-tabs .el-tabs__nav { padding-left: 2px !important; }

</style>
