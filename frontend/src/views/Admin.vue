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
import AdminSceneManager from '../components/admin/AdminSceneManager.vue'
import { getAppSettings, updateAppSetting, getInviteCodes, generateInviteCodes } from '../api/admin'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'

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
  } catch (e) { console.error(e) }
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
  } catch (e) { console.error(e) }
}

const dashboardRef = ref(null)
const itemListRef = ref(null)
const tagManagerRef = ref(null)
const carouselRef = ref(null)
const fetchPanelRef = ref(null)
const userManagerRef = ref(null)
const sceneManagerRef = ref(null)

function handleTabChange(tab) {
  switch (tab) {
    case 'dashboard': dashboardRef.value?.refresh(); break
    case 'items': itemListRef.value?.refresh(); break
    case 'tags': tagManagerRef.value?.refresh(); break
    case 'carousel': carouselRef.value?.refresh(); break
    case 'fetch': fetchPanelRef.value?.refresh(); break
    case 'users': userManagerRef.value?.refresh(); break
    case 'scenes': sceneManagerRef.value?.refresh(); break
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
        <el-tab-pane label="场景策展" name="scenes">
          <AdminSceneManager ref="sceneManagerRef" />
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
          <div v-loading="settingsLoading" class="grid grid-cols-1 lg:grid-cols-2 gap-6 max-w-6xl">
            
            <!-- 第三方 API 密钥配置 -->
            <div class="bg-gray-800 rounded-lg border border-gray-700 overflow-hidden shadow-sm">
              <div class="p-4 border-b border-gray-700 bg-gray-800/50">
                <h3 class="text-sm font-bold text-gray-200 flex items-center gap-2">
                  <Icon icon="lucide:key" class="text-indigo-400 w-4 h-4" /> 第三方 API 密钥
                </h3>
                <p class="text-xs text-gray-500 mt-1">配置数据抓取与人机验证的底层密钥，保存即刻生效，无需重启服务。</p>
              </div>
              <div class="p-4 space-y-4">
                <template v-for="(val, key) in settings" :key="key">
                  <div v-if="key !== 'INVITE_ONLY'" class="flex flex-col gap-1">
                    <label class="text-xs font-mono text-indigo-300 font-semibold uppercase tracking-wider">{{ key }}</label>
                    <div class="flex gap-2">
                      <el-input
                        :model-value="val"
                        @input="v => settings[key] = v"
                        :placeholder="'配置 ' + key"
                        type="password"
                        show-password
                        class="flex-1"
                      />
                      <el-button type="primary" plain @click="saveSetting(key)">保存</el-button>
                    </div>
                  </div>
                </template>
              </div>
            </div>

            <!-- 注册与邀请码管理 -->
            <div class="bg-gray-800 rounded-lg border border-gray-700 overflow-hidden shadow-sm flex flex-col">
              <div class="p-4 border-b border-gray-700 bg-gray-800/50 flex justify-between items-center">
                <div>
                  <h3 class="text-sm font-bold text-gray-200 flex items-center gap-2">
                    <Icon icon="lucide:ticket" class="text-emerald-400 w-4 h-4" /> 邀请码机制
                  </h3>
                  <p class="text-xs text-gray-500 mt-1">限制非法注册，构建私密小圈子社区。</p>
                </div>
                <div class="flex items-center gap-2 bg-gray-900 px-3 py-1.5 rounded border border-gray-700">
                  <span class="text-xs text-gray-400 font-medium">全局开关</span>
                  <el-switch
                    :model-value="settings.INVITE_ONLY === 'true'"
                    @update:model-value="v => { settings.INVITE_ONLY = v ? 'true' : 'false'; saveSetting('INVITE_ONLY') }"
                  />
                </div>
              </div>
              
              <div class="p-4 flex-1 flex flex-col">
                <div class="flex items-center justify-between mb-4">
                  <span class="text-xs text-gray-400">当前累计生成 <strong class="text-gray-200">{{ inviteCodes.length }}</strong> 枚</span>
                  <div class="flex gap-2">
                    <el-button size="small" type="primary" plain @click="handleGenerateInviteCodes">生成 5 枚</el-button>
                    <el-button size="small" @click="loadInviteCodes">刷新数据</el-button>
                  </div>
                </div>
                
                <div v-if="inviteCodes.length" class="flex-1 max-h-[400px] overflow-y-auto space-y-2 pr-2 custom-scrollbar">
                  <div v-for="c in inviteCodes" :key="c.id" class="flex items-center justify-between bg-gray-900 p-3 rounded border border-gray-700 hover:border-gray-600 transition-colors">
                    <div class="flex items-center gap-4">
                      <code class="font-mono text-sm tracking-wider font-bold text-indigo-400">{{ c.code }}</code>
                      <div v-if="c.used_by" class="flex items-center gap-2">
                        <el-tag type="danger" size="small" effect="dark" class="border-0">已消耗</el-tag>
                        <span class="text-xs text-gray-400">使用者: <span class="text-gray-200">{{ c.used_by_name || c.used_by }}</span></span>
                      </div>
                      <div v-else>
                        <el-tag type="success" size="small" effect="plain" class="border-emerald-500/30">全新可用</el-tag>
                      </div>
                    </div>
                    <span class="text-[10px] text-gray-600 font-mono">{{ c.created_at?.substring(0,10) }}</span>
                  </div>
                </div>
                <div v-else class="flex-1 flex items-center justify-center text-gray-500 text-sm py-10">
                  暂无任何邀请码数据
                </div>
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

/* Fix Element Plus tab content clipping for dropdowns, without breaking responsive tab headers */
.admin-tabs .el-tabs__content,
.admin-tabs .el-tab-pane {
  overflow: visible !important;
}
.admin-tabs .el-tabs__nav { padding-left: 2px !important; }
</style>

