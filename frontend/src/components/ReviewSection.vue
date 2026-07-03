<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { getReviews, createReview, deleteReview } from '../api/review'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'

const props = defineProps({ itemId: { type: Number, required: true } })
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const reviews = ref([])
const stats = ref({ count: 0, avgRating: 0 })
const loading = ref(true)
const comment = ref('')
const rating = ref(0)
const submitting = ref(false)

onMounted(() => fetchReviews())

async function fetchReviews() {
  loading.value = true
  try {
    const data = await getReviews(props.itemId)
    reviews.value = data.records || []
    stats.value = data.stats || { count: 0, avgRating: 0 }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function submitReview() {
  if (!auth.isLoggedIn) { router.push({ name: 'Login', query: { redirect: route.fullPath } }); return }
  if (!comment.value.trim()) return
  submitting.value = true
  try {
    await createReview({ itemId: props.itemId, rating: rating.value, comment: comment.value })
    comment.value = ''
    rating.value = 0
    ElMessage.success('评论发布成功')
    await fetchReviews()
  } catch (e) {
    ElMessage.error(e.response?.data?.error || e.response?.data?.message || '评论失败')
  } finally {
    submitting.value = false
  }
}

async function removeReview(id) {
  try {
    await deleteReview(id)
    ElMessage.success('已删除')
    await fetchReviews()
  } catch (e) {
    ElMessage.error(e.response?.data?.error || e.response?.data?.message || '删除失败')
  }
}

const stars = [1, 2, 3, 4, 5]

function fmtUser(id) { return `用户#${id}` }
function fmtDate(d) {
  const date = new Date(d)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前'
  return date.toLocaleDateString('zh-CN')
}

// Random background color for cards (subtle tint)
const cardTints = [
  'from-gray-800 to-gray-800/80',
  'from-gray-800 via-indigo-900/30 to-gray-800',
  'from-gray-800 via-emerald-900/20 to-gray-800',
  'from-gray-800 via-amber-900/20 to-gray-800',
  'from-gray-800 via-rose-900/20 to-gray-800',
  'from-gray-800 via-cyan-900/20 to-gray-800',
]
function tintForIndex(i) { return cardTints[i % cardTints.length] }
</script>

<template>
  <div class="mt-12 border-t border-gray-800 pt-8">
    <!-- Header -->
    <div class="flex items-baseline gap-3 mb-6">
      <h3 class="text-xl font-bold text-white">玩家评论</h3>
      <span v-if="stats.count" class="text-sm text-gray-500">({{ stats.count }})</span>
      <span v-if="stats.avgRating" class="text-sm text-yellow-400 ml-auto">★ {{ stats.avgRating }}</span>
    </div>

    <!-- Write review -->
    <div class="mb-8 p-5 bg-gray-800/40 rounded-2xl border border-gray-700/50 backdrop-blur-sm">
      <div class="flex items-center gap-1 mb-3">
        <button v-for="s in stars" :key="s" @click="rating = s"
          :class="s <= rating ? 'text-yellow-400 scale-110' : 'text-gray-600 hover:text-yellow-500'"
          class="text-xl transition-all duration-150">★</button>
        <span v-if="rating" class="text-xs text-gray-400 ml-3 bg-gray-700/50 px-2 py-0.5 rounded-full">已评分 {{ rating }}/5</span>
      </div>
      <textarea v-model="comment" placeholder="写下你的游戏体验..."
        class="w-full bg-gray-900/80 border border-gray-700 rounded-xl p-4 text-sm text-gray-200 placeholder-gray-500 resize-none focus:outline-none focus:border-indigo-500 transition-colors"
        rows="3" />
      <div class="flex justify-end mt-3">
        <button @click="submitReview" :disabled="submitting || !comment.trim()"
          class="px-5 py-2 text-sm rounded-xl font-medium transition-all"
          :class="comment.trim()
            ? 'bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white shadow-lg shadow-indigo-500/20'
            : 'bg-gray-700 text-gray-500 cursor-not-allowed'">
          {{ submitting ? '发布中...' : '发布评论' }}
        </button>
      </div>
    </div>

    <!-- Reviews waterfall -->
    <div v-if="loading" class="text-gray-500 text-sm py-8 text-center">加载中...</div>
    <div v-else-if="reviews.length === 0" class="text-gray-600 text-sm py-12 text-center">
      <div class="text-4xl mb-3 opacity-30">💬</div>
      <p>暂无评论，来写下第一条体验吧</p>
    </div>
    <div v-else class="review-masonry">
      <div v-for="(r, i) in reviews" :key="r.id"
        class="break-inside-avoid mb-4 p-5 rounded-2xl border border-gray-700/40 bg-gradient-to-br transition-all duration-300 hover:border-gray-600 hover:shadow-lg hover:shadow-black/20"
        :class="tintForIndex(i)">
        <!-- Card header -->
        <div class="flex items-center gap-3 mb-3">
          <div class="w-8 h-8 rounded-full bg-gray-700 flex items-center justify-center text-xs font-bold text-gray-300 shrink-0">
            {{ fmtUser(r.userId).slice(-1) }}
          </div>
          <div class="flex-1 min-w-0">
            <div class="text-xs font-medium text-gray-300 truncate">{{ fmtUser(r.userId) }}</div>
            <div class="text-[10px] text-gray-500">{{ fmtDate(r.createdAt) }}</div>
          </div>
          <span v-if="r.rating" class="text-xs text-yellow-400 font-mono shrink-0">★ {{ r.rating }}</span>
        </div>
        <!-- Card body -->
        <p class="text-sm text-gray-300 leading-relaxed whitespace-pre-line">{{ r.comment }}</p>
        <!-- Card footer -->
        <div v-if="auth.user?.id === r.userId" class="mt-3 pt-2 border-t border-gray-700/30 flex justify-end">
          <button @click="removeReview(r.id)" class="text-[10px] text-gray-500 hover:text-red-400 transition-colors">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.review-masonry {
  column-count: 1;
  column-gap: 1rem;
}
@media (min-width: 640px) {
  .review-masonry { column-count: 2; }
}
@media (min-width: 1024px) {
  .review-masonry { column-count: 3; }
}
.break-inside-avoid {
  break-inside: avoid;
}
</style>
