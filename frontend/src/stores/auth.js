import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, registerApi, getMe } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)

  const isLoggedIn = computed(() => !!token.value && !!localStorage.getItem('token'))
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(username, password) {
    const data = await loginApi(username, password)
    token.value = data.token
    user.value = data.user
    localStorage.setItem('token', data.token)
    return data
  }

  async function register(username, email, password) {
    const data = await registerApi(username, email, password)
    token.value = data.token
    user.value = data.user
    localStorage.setItem('token', data.token)
    return data
  }

  async function fetchUser() {
    if (!token.value) return
    try {
      user.value = await getMe()
    } catch (e) {
      logout()
    }
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  return { token, user, isLoggedIn, isAdmin, login, register, fetchUser, logout }
})
