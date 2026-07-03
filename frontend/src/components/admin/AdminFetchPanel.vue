<script setup>
import { ref, onMounted } from 'vue'
import {
  triggerSteamFetch, triggerTMDBFetch, triggerAniListFetch,
  triggerBangumiFetch, triggerTMDBTVFetch, triggerItunesFetch, triggerIGDBFetch,
  getPendingItems, approveItem, rejectItem, rejectBatch, searchSteamGames,
} from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMeta, TYPE_LIST } from '../../constants/types'
import TypeIcon from '../../components/TypeIcon.vue'

const steamIds = ref('')
const steamTarget = ref('game')
const steamQuery = ref('')
const steamResults = ref([])
const steamSearching = ref(false)
const tmdbQuery = ref('')
const tmdbTarget = ref('movie')
const aniQuery = ref('')
const aniTarget = ref('anime')
const bangumiQuery = ref('')
const bangumiTarget = ref('anime')
const tmdbTVQuery = ref('')
const tmdbTVTarget = ref('anime')
const itunesQuery = ref('')
const itunesTarget = ref('music')
const igdbQuery = ref('')
const igdbEndpoint = ref('search')
const igdbLimit = ref(10)
const igdbTarget = ref('game')
const pendingList = ref([])
const pendingTotal = ref(0)
const pendingPage = ref(1)
const loading = ref(false)

// Steam
async function handleSteamFetch() {
  const ids = steamIds.value.split(',').map(s => s.trim()).filter(Boolean).map(Number)
  if (!ids.length) { ElMessage.warning('请输入 AppID'); return }
  try { const r = await triggerSteamFetch(ids, steamTarget.value); ElMessage.success(r.message); steamIds.value = ''; setTimeout(loadPending, 2000) } catch { ElMessage.error('提交失败') }
}
// Steam search
let steamSearchTimer = null
async function searchSteam() {
  if (!steamQuery.value.trim()) { steamResults.value = []; return }
  steamSearching.value = true
  try { steamResults.value = await searchSteamGames(steamQuery.value.trim()) } catch { steamResults.value = [] }
  steamSearching.value = false
}
function debounceSearchSteam() {
  clearTimeout(steamSearchTimer)
  steamSearchTimer = setTimeout(searchSteam, 400)
}
async function fetchSteamResult(game) {
  try { const r = await triggerSteamFetch([game.id], steamTarget.value); ElMessage.success(r.message || `${game.name} 已加入抓取队列`); setTimeout(loadPending, 2000) } catch { ElMessage.error('提交失败') }
}
// TMDB Movie
async function handleTMDBFetch() {
  if (!tmdbQuery.value.trim()) { ElMessage.warning('请输入关键词'); return }
  try { const r = await triggerTMDBFetch(tmdbQuery.value.trim(), tmdbTarget.value); ElMessage.success(r.message); tmdbQuery.value = ''; setTimeout(loadPending, 2000) } catch { ElMessage.error('提交失败') }
}
// AniList
async function handleAniListFetch() {
  if (!aniQuery.value.trim()) { ElMessage.warning('请输入关键词'); return }
  try { const r = await triggerAniListFetch(aniQuery.value.trim(), aniTarget.value); ElMessage.success(r.message); aniQuery.value = ''; setTimeout(loadPending, 2000) } catch { ElMessage.error('提交失败') }
}
// Bangumi
async function handleBangumiFetch() {
  if (!bangumiQuery.value.trim()) { ElMessage.warning('请输入关键词'); return }
  try { const r = await triggerBangumiFetch(bangumiQuery.value.trim(), bangumiTarget.value); ElMessage.success(r.message); bangumiQuery.value = ''; setTimeout(loadPending, 2000) } catch { ElMessage.error('提交失败') }
}
// TMDB TV
async function handleTMDBTVFetch() {
  if (!tmdbTVQuery.value.trim()) { ElMessage.warning('请输入关键词'); return }
  try { const r = await triggerTMDBTVFetch(tmdbTVQuery.value.trim(), tmdbTVTarget.value); ElMessage.success(r.message); tmdbTVQuery.value = ''; setTimeout(loadPending, 2000) } catch { ElMessage.error('提交失败') }
}
async function handleItunesFetch() {
  if (!itunesQuery.value.trim()) { ElMessage.warning('请输入关键词'); return }
  try { const r = await triggerItunesFetch(itunesQuery.value.trim(), itunesTarget.value); ElMessage.success(r.message); itunesQuery.value = ''; setTimeout(loadPending, 2000) } catch { ElMessage.error('提交失败') }
}
// IGDB
async function handleIGDBFetch(endpoint) {
  try {
    const payload = { endpoint, limit: igdbLimit.value, targetType: igdbTarget.value }
    if (endpoint === 'search') {
      if (!igdbQuery.value.trim()) { ElMessage.warning('请输入游戏名'); return }
      payload.query = igdbQuery.value.trim()
    }
    const r = await triggerIGDBFetch(endpoint, payload)
    ElMessage.success(r.message)
    igdbQuery.value = ''
    setTimeout(loadPending, 2000)
  } catch { ElMessage.error('提交失败') }
}

async function loadPending() {
  loading.value = true
  try { const r = await getPendingItems({ page: pendingPage.value, size: 10 }); pendingList.value = r.records || []; pendingTotal.value = r.total || 0 }
  catch {} finally { loading.value = false }
}
async function handleApprove(item) {
  try { await ElMessageBox.confirm(`确定通过「${item.title}」？`, '确认上线', { type: 'success' }); await approveItem(item.id); ElMessage.success(`已上线: ${item.title}`); await loadPending() }
  catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}
async function handleReject(item) {
  try { await ElMessageBox.confirm(`确定拒绝「${item.title}」？`, '确认拒绝', { type: 'warning' }); await rejectItem(item.id); ElMessage.success(`已拒绝: ${item.title}`); await loadPending() }
  catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}
const selectedIds = ref([])
async function handleBatchReject() {
  const ids = selectedIds.value
  if (!ids.length) { ElMessage.warning('请先选择条目'); return }
  try {
    await ElMessageBox.confirm(`确定批量拒绝 ${ids.length} 条？`, '批量拒绝', { type: 'warning' })
    await rejectBatch(ids); ElMessage.success(`已拒绝 ${ids.length} 条`); selectedIds.value = []; await loadPending()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}
onMounted(loadPending)
defineExpose({ refresh: loadPending })
</script>

<template>
  <div>
    <!-- Fetch Panels -->
    <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-3 mb-6">
      <!-- Steam -->
      <div class="bg-gray-800 rounded-lg p-3 border border-gray-700">
        <h4 class="text-xs font-semibold text-gray-200 mb-2"><TypeIcon type="game" size="14" /> Steam</h4>
        <el-select v-model="steamTarget" size="small" style="width:100%" :teleported="false" popper-class="admin-select-drop" class="mb-1">
          <el-option v-for="t in ['game']" :key="t" :label="getMeta(t).label" :value="t" />
        </el-select>
        <el-input v-model="steamQuery" placeholder="搜索游戏名称..." size="small" class="mb-1" @input="debounceSearchSteam" clearable />
        <div v-if="steamSearching" class="text-xs text-gray-500 mb-1">搜索中...</div>
        <div v-if="steamResults.length" class="max-h-[120px] overflow-y-auto mb-1 space-y-0.5">
          <div v-for="g in steamResults" :key="g.id" @click="fetchSteamResult(g)"
            class="flex items-center gap-2 px-2 py-1 rounded cursor-pointer hover:bg-gray-700 text-xs text-gray-300">
            <img v-if="g.tinyImage" :src="g.tinyImage" class="w-6 h-6 object-cover rounded" />
            <span class="flex-1 truncate">{{ g.name }}</span>
            <span class="text-blue-400 shrink-0">抓取</span>
          </div>
        </div>
        <el-input v-model="steamIds" type="textarea" :rows="1" placeholder="或直接输入 AppID, 逗号分隔" size="small" class="mb-1" />
        <el-button type="primary" size="small" @click="handleSteamFetch" style="width:100%">提交</el-button>
      </div>
      <!-- TMDB Movie -->
      <div class="bg-gray-800 rounded-lg p-3 border border-gray-700">
        <h4 class="text-xs font-semibold text-gray-200 mb-2"><TypeIcon type="movie" size="14" /> TMDB 电影</h4>
        <el-select v-model="tmdbTarget" size="small" style="width:100%" :teleported="false" popper-class="admin-select-drop" class="mb-1">
          <el-option v-for="t in ['movie','anime']" :key="t" :label="getMeta(t).label" :value="t" />
        </el-select>
        <el-input v-model="tmdbQuery" placeholder="电影关键词" size="small" class="mb-1" @keyup.enter="handleTMDBFetch" />
        <el-button type="primary" size="small" @click="handleTMDBFetch" style="width:100%">提交</el-button>
      </div>
      <!-- TMDB TV -->
      <div class="bg-gray-800 rounded-lg p-3 border border-gray-700">
        <h4 class="text-xs font-semibold text-gray-200 mb-2">📺 TMDB 剧集</h4>
        <el-select v-model="tmdbTVTarget" size="small" style="width:100%" :teleported="false" popper-class="admin-select-drop" class="mb-1">
          <el-option v-for="t in ['anime','movie']" :key="t" :label="getMeta(t).label" :value="t" />
        </el-select>
        <el-input v-model="tmdbTVQuery" placeholder="剧集/动漫关键词" size="small" class="mb-1" @keyup.enter="handleTMDBTVFetch" />
        <el-button type="primary" size="small" @click="handleTMDBTVFetch" style="width:100%">提交</el-button>
      </div>
      <!-- AniList -->
      <div class="bg-gray-800 rounded-lg p-3 border border-gray-700">
        <h4 class="text-xs font-semibold text-gray-200 mb-2"><TypeIcon type="anime" size="14" /> AniList</h4>
        <el-select v-model="aniTarget" size="small" style="width:100%" :teleported="false" popper-class="admin-select-drop" class="mb-1">
          <el-option v-for="t in ['anime','movie']" :key="t" :label="getMeta(t).label" :value="t" />
        </el-select>
        <el-input v-model="aniQuery" placeholder="动漫关键词(英/日)" size="small" class="mb-1" @keyup.enter="handleAniListFetch" />
        <el-button type="primary" size="small" @click="handleAniListFetch" style="width:100%">提交</el-button>
      </div>
      <!-- Bangumi -->
      <div class="bg-gray-800 rounded-lg p-3 border border-gray-700">
        <h4 class="text-xs font-semibold text-gray-200 mb-2">🍥 Bangumi</h4>
        <el-select v-model="bangumiTarget" size="small" style="width:100%" :teleported="false" popper-class="admin-select-drop" class="mb-1">
          <el-option v-for="t in ['anime','movie']" :key="t" :label="getMeta(t).label" :value="t" />
        </el-select>
        <el-input v-model="bangumiQuery" placeholder="动漫关键词(中文)" size="small" class="mb-1" @keyup.enter="handleBangumiFetch" />
        <el-button type="primary" size="small" @click="handleBangumiFetch" style="width:100%">提交</el-button>
      </div>
      <!-- iTunes -->
      <div class="bg-gray-800 rounded-lg p-3 border border-gray-700">
        <h4 class="text-xs font-semibold text-gray-200 mb-2"><TypeIcon type="music" size="14" /> iTunes</h4>
        <el-select v-model="itunesTarget" size="small" style="width:100%" :teleported="false" popper-class="admin-select-drop" class="mb-1">
          <el-option v-for="t in ['music','anime']" :key="t" :label="getMeta(t).label" :value="t" />
        </el-select>
        <el-input v-model="itunesQuery" placeholder="专辑/艺人关键词" size="small" class="mb-1" @keyup.enter="handleItunesFetch" />
        <el-button type="primary" size="small" @click="handleItunesFetch" style="width:100%">提交</el-button>
      </div>
      <!-- IGDB -->
      <div class="bg-gray-800 rounded-lg p-3 border border-emerald-700 md:col-span-2 lg:col-span-3">
        <h4 class="text-xs font-semibold text-emerald-400 mb-2"><TypeIcon type="game" size="14" /> IGDB 游戏数据库</h4>
        <div class="flex gap-2 mb-1">
          <el-select v-model="igdbEndpoint" size="small" style="width:120px" :teleported="false" popper-class="admin-select-drop">
            <el-option label="🔍 搜索" value="search" />
            <el-option label="🔥 热门" value="popular" />
            <el-option label="🆕 新品" value="recent" />
          </el-select>
          <el-select v-model="igdbLimit" size="small" style="width:90px" :teleported="false" popper-class="admin-select-drop">
            <el-option v-for="n in [5,10,20,50]" :key="n" :label="'上限 '+n" :value="n" />
          </el-select>
          <el-select v-model="igdbTarget" size="small" style="width:100px" :teleported="false" popper-class="admin-select-drop">
            <el-option v-for="t in ['game','anime']" :key="t" :label="getMeta(t).label" :value="t" />
          </el-select>
          <el-button type="primary" size="small" @click="handleIGDBFetch(igdbEndpoint)" style="width:100%">
            {{ igdbEndpoint === 'search' ? '搜索' : igdbEndpoint === 'popular' ? '拉取热门' : '拉取新品' }}
          </el-button>
        </div>
        <el-input v-if="igdbEndpoint === 'search'" v-model="igdbQuery" placeholder="游戏名（英文更准，如 Hades / GTA V）" size="small" @keyup.enter="handleIGDBFetch('search')" />
      </div>
    </div>

    <!-- Pending Queue -->
    <div class="flex items-center gap-3 mb-3">
      <h4 class="text-sm font-semibold text-gray-200">待审核队列 ({{ pendingTotal }})</h4>
      <el-button size="small" text @click="loadPending" :loading="loading">刷新</el-button>
      <el-button v-if="selectedIds.length" type="danger" size="small" @click="handleBatchReject">批量拒绝 ({{ selectedIds.length }})</el-button>
    </div>
    <el-table :data="pendingList" v-loading="loading" style="width: 100%" @selection-change="selectedIds = $event.map(r => r.id)">
      <el-table-column type="selection" width="40" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <img v-if="row.wideCoverUrl || row.coverUrl" :src="row.wideCoverUrl || row.coverUrl" class="w-12 h-8 object-cover rounded" />
          <span v-else class="text-gray-400 text-xs">无封面</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="140" />
      <el-table-column label="品类" width="80">
        <template #default="{ row }"><el-tag size="small"><TypeIcon :type="row.type" size="14" /> {{ getMeta(row.type).label }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="source" label="来源" width="75" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
          <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="flex justify-center mt-4" v-if="pendingTotal > 10">
      <el-pagination v-model:current-page="pendingPage" :page-size="10" :total="pendingTotal" layout="prev, pager, next" @current-change="loadPending" />
    </div>
  </div>
</template>
