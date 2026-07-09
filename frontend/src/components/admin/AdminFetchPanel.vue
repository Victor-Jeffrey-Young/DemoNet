<script setup>
import { ref, onMounted } from 'vue'
import {
  triggerSteamFetch, triggerTMDBFetch, triggerAniListFetch,
  triggerBangumiFetch, triggerTMDBTVFetch, triggerItunesFetch, triggerIGDBFetch,
  triggerSpotifyFetch, triggerQQMusicFetch, searchQQMusicAlbums,
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
const spotifyQuery = ref('')
const spotifyTarget = ref('music')
const qqmusicQuery = ref('')
const qqmusicTarget = ref('music')
const qqmusicResults = ref([])
const qqmusicSearching = ref(false)
let qqmusicSearchTimer = null
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
  try { const r = await triggerSteamFetch(ids, steamTarget.value); ElMessage.success(r.message); steamIds.value = ''; setTimeout(loadPending, 2000) } catch (e) { ElMessage.error(e.response?.data?.error || e.response?.data?.message || '提交失败') }
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
  try { const r = await triggerSteamFetch([game.id], steamTarget.value); ElMessage.success(r.message || `${game.name} 已加入抓取队列`); setTimeout(loadPending, 2000) } catch (e) { ElMessage.error(e.response?.data?.error || e.response?.data?.message || '抓取提交失败') }
}
// TMDB Movie
async function handleTMDBFetch() {
  if (!tmdbQuery.value.trim()) { ElMessage.warning('请输入关键词'); return }
  try { const r = await triggerTMDBFetch(tmdbQuery.value.trim(), tmdbTarget.value); ElMessage.success(r.message); tmdbQuery.value = ''; setTimeout(loadPending, 2000) } catch (e) { ElMessage.error(e.response?.data?.error || e.response?.data?.message || 'TMDB 提交失败') }
}
// AniList
async function handleAniListFetch() {
  if (!aniQuery.value.trim()) { ElMessage.warning('请输入关键词'); return }
  try { const r = await triggerAniListFetch(aniQuery.value.trim(), aniTarget.value); ElMessage.success(r.message); aniQuery.value = ''; setTimeout(loadPending, 2000) } catch (e) { ElMessage.error(e.response?.data?.error || e.response?.data?.message || 'AniList 提交失败') }
}
// Bangumi
async function handleBangumiFetch() {
  if (!bangumiQuery.value.trim()) { ElMessage.warning('请输入关键词'); return }
  try { const r = await triggerBangumiFetch(bangumiQuery.value.trim(), bangumiTarget.value); ElMessage.success(r.message); bangumiQuery.value = ''; setTimeout(loadPending, 2000) } catch (e) { ElMessage.error(e.response?.data?.error || e.response?.data?.message || 'Bangumi 提交失败') }
}
// TMDB TV
async function handleTMDBTVFetch() {
  if (!tmdbTVQuery.value.trim()) { ElMessage.warning('请输入关键词'); return }
  try { const r = await triggerTMDBTVFetch(tmdbTVQuery.value.trim(), tmdbTVTarget.value); ElMessage.success(r.message); tmdbTVQuery.value = ''; setTimeout(loadPending, 2000) } catch (e) { ElMessage.error(e.response?.data?.error || e.response?.data?.message || 'TMDB TV 提交失败') }
}
async function handleItunesFetch() {
  if (!itunesQuery.value.trim()) { ElMessage.warning('请输入关键词'); return }
  try { const r = await triggerItunesFetch(itunesQuery.value.trim(), itunesTarget.value); ElMessage.success(r.message); itunesQuery.value = ''; setTimeout(loadPending, 2000) } catch (e) { ElMessage.error(e.response?.data?.error || e.response?.data?.message || 'iTunes 提交失败') }
}
async function handleSpotifyFetch() {
  if (!spotifyQuery.value.trim()) { ElMessage.warning('请输入关键词'); return }
  try {
    const r = await triggerSpotifyFetch(spotifyQuery.value.trim(), spotifyTarget.value);
    if (r.url) {
      ElMessage.success(r.message);
    } else {
      ElMessage.info(r.message);
    }
    spotifyQuery.value = '';
  } catch (e) {
    ElMessage.error(e.response?.data?.error || e.response?.data?.message || 'Spotify 提交失败');
  }
}
async function searchQQMusic() {
  if (!qqmusicQuery.value.trim()) { qqmusicResults.value = []; return }
  qqmusicSearching.value = true
  try { qqmusicResults.value = await searchQQMusicAlbums(qqmusicQuery.value.trim()) } catch { qqmusicResults.value = [] }
  qqmusicSearching.value = false
}
function debounceSearchQQMusic() {
  clearTimeout(qqmusicSearchTimer)
  qqmusicSearchTimer = setTimeout(searchQQMusic, 400)
}
async function fetchQQMusicResult(album) {
  try {
    const r = await triggerQQMusicFetch(album.artist || qqmusicQuery.value, qqmusicTarget.value, album.albumMID)
    ElMessage.success(r.message || `${album.name} 已加入抓取队列`)
    setTimeout(loadPending, 2000)
  } catch (e) {
    ElMessage.error(e.response?.data?.error || e.response?.data?.message || '抓取提交失败')
  }
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
  } catch (e) { ElMessage.error(e.response?.data?.error || e.response?.data?.message || 'IGDB 提交失败') }
}

async function loadPending() {
  loading.value = true
  try { const r = await getPendingItems({ page: pendingPage.value, size: 10 }); pendingList.value = r.records || []; pendingTotal.value = r.total || 0 }
  catch {} finally { loading.value = false }
}
async function handleApprove(item) {
  try { await ElMessageBox.confirm(`确定通过「${item.title}」？`, '确认上线', { type: 'success' }); await approveItem(item.id); ElMessage.success(`已上线: ${item.title}`); await loadPending() }
  catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.error || e.response?.data?.message || '操作失败') }
}
async function handleReject(item) {
  try { await ElMessageBox.confirm(`确定拒绝「${item.title}」？`, '确认拒绝', { type: 'warning' }); await rejectItem(item.id); ElMessage.success(`已拒绝: ${item.title}`); await loadPending() }
  catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.error || e.response?.data?.message || '拒绝失败') }
}
const selectedIds = ref([])
async function handleBatchReject() {
  const ids = selectedIds.value
  if (!ids.length) { ElMessage.warning('请先选择条目'); return }
  try {
    await ElMessageBox.confirm(`确定批量拒绝 ${ids.length} 条？`, '批量拒绝', { type: 'warning' })
    await rejectBatch(ids); ElMessage.success(`已拒绝 ${ids.length} 条`); selectedIds.value = []; await loadPending()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.error || e.response?.data?.message || '批量拒绝失败') }
}
onMounted(loadPending)
defineExpose({ refresh: loadPending })
</script>

<template>
  <div>
    <!-- Fetch Panels -->
    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5 mb-6">
      
      <!-- Steam -->
      <div class="bg-gray-800 rounded-lg p-4 border border-gray-700 hover:border-blue-500/50 transition-colors md:col-span-2 xl:col-span-2">
        <div class="flex items-center justify-between mb-3">
          <h4 class="text-sm font-semibold text-blue-400 flex items-center gap-2"><TypeIcon type="game" size="16" /> Steam</h4>
          <el-select v-model="steamTarget" style="width:100px" size="small" :teleported="false">
            <el-option v-for="t in ['game']" :key="t" :label="getMeta(t).label" :value="t" />
          </el-select>
        </div>
        <div class="flex flex-col md:flex-row gap-3">
          <div class="flex-1 relative">
            <el-input v-model="steamQuery" placeholder="搜索游戏名称 (回车自动搜索)..." @input="debounceSearchSteam" clearable />
            <div v-if="steamSearching || steamResults.length" class="absolute z-20 w-full mt-1 bg-gray-800 border border-gray-600 rounded-lg shadow-2xl overflow-hidden">
              <div v-if="steamSearching" class="p-3 text-sm text-gray-400 text-center">搜索中...</div>
              <div v-else class="max-h-[250px] overflow-y-auto">
                <div v-for="g in steamResults" :key="g.id" @click="fetchSteamResult(g)"
                  class="flex items-center gap-3 px-3 py-2 cursor-pointer hover:bg-gray-700 border-b border-gray-700 last:border-0">
                  <img v-if="g.tinyImage" :src="g.tinyImage" class="w-10 h-10 object-cover rounded shadow-sm" />
                  <span class="flex-1 text-sm text-gray-200 truncate">{{ g.name }}</span>
                  <el-button size="small" type="primary" plain>抓取</el-button>
                </div>
              </div>
            </div>
          </div>
          <div class="flex-1">
            <el-input v-model="steamIds" placeholder="或输入 AppID (用逗号分隔)" clearable @keyup.enter="handleSteamFetch">
              <template #append>
                <el-button @click="handleSteamFetch" type="primary">按 ID 抓取</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>

      <!-- IGDB -->
      <div class="bg-gray-800 rounded-lg p-4 border border-emerald-700/50 hover:border-emerald-500 transition-colors md:col-span-2 xl:col-span-1">
        <div class="flex items-center justify-between mb-3">
          <h4 class="text-sm font-semibold text-emerald-400 flex items-center gap-2"><TypeIcon type="game" size="16" /> IGDB 数据库</h4>
          <el-select v-model="igdbTarget" style="width:100px" size="small" :teleported="false">
            <el-option v-for="t in ['game','anime']" :key="t" :label="getMeta(t).label" :value="t" />
          </el-select>
        </div>
        <div class="flex flex-col gap-3">
          <div class="flex gap-2">
            <el-select v-model="igdbEndpoint" style="width:130px" :teleported="false">
              <el-option label="🔍 精确搜索" value="search" />
              <el-option label="🔥 热门榜单" value="popular" />
              <el-option label="🆕 最新发布" value="recent" />
            </el-select>
            <el-select v-model="igdbLimit" style="width:100px" :teleported="false" class="flex-1">
              <el-option v-for="n in [5,10,20,50]" :key="n" :label="'上限 '+n+'条'" :value="n" />
            </el-select>
          </div>
          <div class="flex gap-2">
            <el-input v-if="igdbEndpoint === 'search'" v-model="igdbQuery" placeholder="游戏英文名 (如 Hades)" class="flex-1" @keyup.enter="handleIGDBFetch('search')" clearable />
            <el-button type="primary" @click="handleIGDBFetch(igdbEndpoint)" :class="{'flex-1': igdbEndpoint !== 'search'}">
              {{ igdbEndpoint === 'search' ? '搜索并拉取' : igdbEndpoint === 'popular' ? '批量拉取热门' : '批量拉取新品' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- TMDB Movie -->
      <div class="bg-gray-800 rounded-lg p-4 border border-gray-700 hover:border-gray-500 transition-colors">
        <div class="flex items-center justify-between mb-3">
          <h4 class="text-sm font-semibold text-gray-200 flex items-center gap-2"><TypeIcon type="movie" size="16" /> TMDB 电影</h4>
          <el-select v-model="tmdbTarget" style="width:100px" size="small" :teleported="false">
            <el-option v-for="t in ['movie','anime']" :key="t" :label="getMeta(t).label" :value="t" />
          </el-select>
        </div>
        <el-input v-model="tmdbQuery" placeholder="电影名称或关键词..." @keyup.enter="handleTMDBFetch" clearable>
          <template #append><el-button @click="handleTMDBFetch">拉取</el-button></template>
        </el-input>
      </div>

      <!-- TMDB TV -->
      <div class="bg-gray-800 rounded-lg p-4 border border-gray-700 hover:border-indigo-500/50 transition-colors">
        <div class="flex items-center justify-between mb-3">
          <h4 class="text-sm font-semibold text-indigo-300 flex items-center gap-2">📺 TMDB 剧集</h4>
          <el-select v-model="tmdbTVTarget" style="width:100px" size="small" :teleported="false">
            <el-option v-for="t in ['anime','movie']" :key="t" :label="getMeta(t).label" :value="t" />
          </el-select>
        </div>
        <el-input v-model="tmdbTVQuery" placeholder="剧集或动漫关键词..." @keyup.enter="handleTMDBTVFetch" clearable>
          <template #append><el-button @click="handleTMDBTVFetch">拉取</el-button></template>
        </el-input>
      </div>

      <!-- AniList -->
      <div class="bg-gray-800 rounded-lg p-4 border border-gray-700 hover:border-blue-400/50 transition-colors">
        <div class="flex items-center justify-between mb-3">
          <h4 class="text-sm font-semibold text-blue-300 flex items-center gap-2"><TypeIcon type="anime" size="16" /> AniList</h4>
          <el-select v-model="aniTarget" style="width:100px" size="small" :teleported="false">
            <el-option v-for="t in ['anime','movie']" :key="t" :label="getMeta(t).label" :value="t" />
          </el-select>
        </div>
        <el-input v-model="aniQuery" placeholder="动漫英文或罗马音..." @keyup.enter="handleAniListFetch" clearable>
          <template #append><el-button @click="handleAniListFetch">拉取</el-button></template>
        </el-input>
      </div>

      <!-- Bangumi -->
      <div class="bg-gray-800 rounded-lg p-4 border border-gray-700 hover:border-pink-500/50 transition-colors">
        <div class="flex items-center justify-between mb-3">
          <h4 class="text-sm font-semibold text-pink-300 flex items-center gap-2">🍥 Bangumi</h4>
          <el-select v-model="bangumiTarget" style="width:100px" size="small" :teleported="false">
            <el-option v-for="t in ['anime','movie']" :key="t" :label="getMeta(t).label" :value="t" />
          </el-select>
        </div>
        <el-input v-model="bangumiQuery" placeholder="动漫中文名..." @keyup.enter="handleBangumiFetch" clearable>
          <template #append><el-button @click="handleBangumiFetch">拉取</el-button></template>
        </el-input>
      </div>

      <!-- iTunes -->
      <div class="bg-gray-800 rounded-lg p-4 border border-gray-700 hover:border-purple-500/50 transition-colors">
        <div class="flex items-center justify-between mb-3">
          <h4 class="text-sm font-semibold text-purple-300 flex items-center gap-2"><TypeIcon type="music" size="16" /> iTunes</h4>
          <el-select v-model="itunesTarget" style="width:100px" size="small" :teleported="false">
            <el-option v-for="t in ['music','anime']" :key="t" :label="getMeta(t).label" :value="t" />
          </el-select>
        </div>
        <el-input v-model="itunesQuery" placeholder="专辑/艺人/歌曲..." @keyup.enter="handleItunesFetch" clearable>
          <template #append><el-button @click="handleItunesFetch">拉取</el-button></template>
        </el-input>
      </div>

      <!-- Spotify -->
      <div class="bg-gray-800 rounded-lg p-4 border border-green-800/50 hover:border-green-600 transition-colors">
        <div class="flex items-center justify-between mb-3">
          <h4 class="text-sm font-semibold text-green-400 flex items-center gap-2">🎵 Spotify</h4>
          <el-select v-model="spotifyTarget" style="width:100px" size="small" :teleported="false">
            <el-option v-for="t in ['music']" :key="t" :label="getMeta(t).label" :value="t" />
          </el-select>
        </div>
        <el-input v-model="spotifyQuery" placeholder="专辑/艺人名称（自动匹配补充到现有条目）..." @keyup.enter="handleSpotifyFetch" clearable>
          <template #append><el-button @click="handleSpotifyFetch" type="success">补充链接</el-button></template>
        </el-input>
      </div>

      <!-- QQ Music -->
      <div class="bg-gray-800 rounded-lg p-4 border border-red-800/50 hover:border-red-600 transition-colors md:col-span-2 xl:col-span-2">
        <div class="flex items-center justify-between mb-3">
          <h4 class="text-sm font-semibold text-red-400 flex items-center gap-2">🎶 QQ音乐</h4>
          <el-select v-model="qqmusicTarget" style="width:100px" size="small" :teleported="false">
            <el-option v-for="t in ['music']" :key="t" :label="getMeta(t).label" :value="t" />
          </el-select>
        </div>
        <div class="flex-1 relative">
          <el-input v-model="qqmusicQuery" placeholder="搜索艺人名称（如：周杰伦）..." @input="debounceSearchQQMusic" clearable />
          <div v-if="qqmusicSearching || qqmusicResults.length" class="absolute z-20 w-full mt-1 bg-gray-800 border border-gray-600 rounded-lg shadow-2xl overflow-hidden">
            <div v-if="qqmusicSearching" class="p-3 text-sm text-gray-400 text-center">搜索中...</div>
            <div v-else class="max-h-[300px] overflow-y-auto">
              <div v-for="a in qqmusicResults" :key="a.id" @click="fetchQQMusicResult(a)"
                class="flex items-center gap-3 px-3 py-2 cursor-pointer hover:bg-gray-700 border-b border-gray-700 last:border-0">
                <img v-if="a.cover" :src="a.cover" class="w-10 h-10 object-cover rounded shadow-sm" />
                <div class="flex-1 min-w-0">
                  <div class="text-sm text-gray-200 truncate font-medium">{{ a.name }}</div>
                  <div class="text-xs text-gray-400 truncate">{{ a.artist }}</div>
                </div>
                <span class="text-xs text-gray-500 shrink-0">{{ a.songCount || '?' }} 首</span>
                <el-button size="small" type="danger" plain>抓取</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- Pending Queue -->
    <div class="flex items-center gap-3 mb-3">
      <h4 class="text-sm font-semibold text-gray-200">待审核队列 ({{ pendingTotal }})</h4>
      <el-button  text @click="loadPending" :loading="loading">刷新</el-button>
      <el-button v-if="selectedIds.length" type="danger"  @click="handleBatchReject">批量拒绝 ({{ selectedIds.length }})</el-button>
    </div>
    <el-table :data="pendingList" v-loading="loading" stripe border style="width: 100%" @selection-change="selectedIds = $event.map(r => r.id)">
      <el-table-column type="selection" width="45" align="center" />
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column label="封面" width="120" align="center">
        <template #default="{ row }">
          <img v-if="row.wideCoverUrl || row.coverUrl" :src="row.wideCoverUrl || row.coverUrl" class="w-20 h-12 object-cover rounded shadow-sm mx-auto" />
          <span v-else class="text-gray-500 text-xs">暂无封面</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="条目名称" min-width="250">
        <template #default="{ row }">
          <div class="font-medium text-gray-200 truncate">{{ row.title }}</div>
          <div v-if="row.originalTitle && row.originalTitle !== row.title" class="text-xs text-gray-500 truncate">{{ row.originalTitle }}</div>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="130" align="center">
        <template #default="{ row }">
          <el-tag effect="dark" :type="row.type === 'game' ? 'success' : row.type === 'movie' ? 'primary' : 'warning'" class="flex items-center justify-center gap-1 mx-auto w-max">
            <TypeIcon :type="row.type" size="14" /> {{ getMeta(row.type).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="source" label="抓取源" width="100" align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" plain @click="handleApprove(row)">通过</el-button>
          <el-button size="small" type="danger" plain @click="handleReject(row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="flex justify-center mt-4" v-if="pendingTotal > 10">
      <el-pagination v-model:current-page="pendingPage" :page-size="10" :total="pendingTotal" layout="prev, pager, next" @current-change="loadPending" />
    </div>
  </div>
</template>
