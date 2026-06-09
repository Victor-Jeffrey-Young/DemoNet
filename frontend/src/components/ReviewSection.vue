<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { getReviews, createReview, deleteReview } from '../api/review'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const props = defineProps({ itemId: { type: Number, required: true } })
const auth = useAuthStore()
const router = useRouter()

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
    ElMessage.error(e.response?.data?.error || '评论失败')
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
    ElMessage.error(e.response?.data?.error || '删除失败')
  }
}

const stars = [1, 2, 3, 4, 5]
function starLabel(r) { return r ? '★'.repeat(r) + '☆'.repeat(5 - r) : '' }

function fmtUser(id) { return `用户#${id}` }
</script>

<template>
  <div class="mt-8 border-t border-gray-800 pt-6">
    <h3 class="text-lg font-semibold text-white mb-4">评论 <span class="text-sm text-gray-500 font-normal">({{ stats.count }})</span>
      <span v-if="stats.count > 0" class="text-sm text-yellow-400 ml-2">★ {{ stats.avgRating }}</span>
    </h3>

    <!-- Write review -->
    <div class="mb-6 p-4 bg-gray-800/50 rounded-lg border border-gray-700">
      <div class="flex items-center gap-1 mb-3">
        <button v-for="s in stars" :key="s" @click="rating = s"
          :class="s <= rating ? 'text-yellow-400' : 'text-gray-600 hover:text-yellow-500'"
          class="text-lg transition-colors">★</button>
        <span v-if="rating" class="text-xs text-gray-500 ml-2">已评分</span>
      </div>
      <textarea v-model="comment" placeholder="写下你的评论..."
        class="w-full bg-gray-900 border border-gray-700 rounded-lg p-3 text-sm text-gray-200 placeholder-gray-500 resize-none focus:outline-none focus:border-indigo-500 transition-colors"
        rows="3" />
      <div class="flex justify-end mt-2">
        <button @click="submitReview" :disabled="submitting || !comment.trim()"
          class="px-4 py-1.5 text-sm rounded-lg bg-indigo-600 hover:bg-indigo-500 disabled:opacity-40 disabled:cursor-not-allowed text-white transition-colors">
          {{ submitting ? '发布中...' : '发布评论' }}
        </button>
      </div>
    </div>

    <!-- Reviews list -->
    <div v-if="loading" class="text-gray-500 text-sm py-4">加载中...</div>
    <div v-else-if="reviews.length === 0" class="text-gray-600 text-sm py-4">暂无评论，来说点什么吧</div>
    <div v-else class="space-y-4">
      <div v-for="r in reviews" :key="r.id" class="p-4 bg-gray-800/30 rounded-lg border border-gray-800">
        <div class="flex items-center justify-between mb-2">
          <div class="flex items-center gap-3">
            <span class="text-xs text-gray-300 font-medium">{{ fmtUser(r.userId) }}</span>
            <span v-if="r.rating" class="text-xs text-yellow-400">{{ starLabel(r.rating) }}</span>
          </div>
          <div class="flex items-center gap-3">
            <span class="text-[10px] text-gray-600">{{ new Date(r.createdAt).toLocaleDateString('zh-CN') }}</span>
            <button v-if="auth.user?.id === r.userId" @click="removeReview(r.id)" class="text-[10px] text-gray-600 hover:text-red-400 transition-colors">删除</button>
          </div>
        </div>
        <p class="text-sm text-gray-300 leading-relaxed">{{ r.comment }}</p>
      </div>
    </div>
  </div>
</template>
