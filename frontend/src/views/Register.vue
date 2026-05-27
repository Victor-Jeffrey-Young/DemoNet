<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleRegister() {
  error.value = ''
  loading.value = true
  try {
    await auth.register(username.value, email.value, password.value)
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-[70vh] flex items-center justify-center px-4">
    <div class="w-full max-w-sm bg-gray-900 rounded-2xl border border-gray-800 p-8">
      <h1 class="text-2xl font-bold text-center mb-6">注册</h1>
      <div v-if="error" class="mb-4 p-3 bg-red-900/30 border border-red-800 rounded-lg text-red-400 text-sm">
        {{ error }}
      </div>
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
          <input v-model="password" type="password" required
            class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-blue-500" />
        </div>
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
