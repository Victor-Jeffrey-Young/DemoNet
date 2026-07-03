<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const email = ref('')
const password = ref('')
const inviteCode = ref('')
const loading = ref(false)
const turnstileToken = ref('')
const turnstileSiteKey = ref('')
const inviteOnly = ref(false)

onMounted(async () => {
  try {
    const cfg = await request.get('/auth/config')
    turnstileSiteKey.value = cfg.turnstileSiteKey || ''
    inviteOnly.value = cfg.inviteOnly || false
    if (turnstileSiteKey.value) loadTurnstile()
  } catch (e) { /* config not critical */ }
})

function loadTurnstile() {
  const script = document.createElement('script')
  script.src = 'https://challenges.cloudflare.com/turnstile/v0/api.js'
  script.async = true
  script.defer = true
  script.onload = () => {
    if (window.turnstile) {
      window.turnstile.render('#turnstile-widget', {
        sitekey: turnstileSiteKey.value,
        callback: (token) => { turnstileToken.value = token },
        'expired-callback': () => { turnstileToken.value = '' },
      })
    }
  }
  document.head.appendChild(script)
}

async function handleRegister() {
  if (inviteOnly.value && !inviteCode.value.trim()) {
    ElMessage.warning('请输入邀请码')
    return
  }
  loading.value = true
  try {
    await auth.register(username.value, email.value, password.value,
      turnstileToken.value || undefined, inviteCode.value || undefined)
    router.push('/')
  } catch (e) {
    ElMessage.error(e.response?.data?.error || e.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-[70vh] flex items-center justify-center px-4">
    <div class="w-full max-w-sm bg-gray-900 rounded-2xl border border-gray-800 p-8">
      <h1 class="text-2xl font-bold text-center mb-6">注册</h1>
      <form @submit.prevent="handleRegister" class="space-y-4">
        <div>
          <label class="block text-sm text-gray-400 mb-1">用户名</label>
          <input v-model="username" type="text" required
            class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-blue-500" />
        </div>
        <div>
          <label class="block text-sm text-gray-400 mb-1">邮箱</label>
          <input v-model="email" type="email" required
            class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-blue-500" />
        </div>
        <div>
          <label class="block text-sm text-gray-400 mb-1">密码</label>
          <input v-model="password" type="password" required minlength="6"
            class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-blue-500" />
        </div>
        <div v-if="inviteOnly">
          <label class="block text-sm text-gray-400 mb-1">邀请码</label>
          <input v-model="inviteCode" type="text" required
            class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-blue-500" />
        </div>
        <div v-if="turnstileSiteKey" id="turnstile-widget" class="flex justify-center"></div>
        <button type="submit" :disabled="loading"
          class="w-full bg-blue-600 hover:bg-blue-500 text-white rounded-lg py-2.5 text-sm font-medium transition disabled:opacity-50">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      <p class="text-center text-gray-500 text-sm mt-6">
        已有账号？<router-link to="/login" class="text-blue-400 hover:underline">登录</router-link>
      </p>
    </div>
  </div>
</template>
