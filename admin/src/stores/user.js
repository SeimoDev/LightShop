import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const user = ref(JSON.parse(localStorage.getItem('admin_user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 1)

  async function login(credentials) {
    const res = await authApi.login(credentials)
    
    if (res.data.user.role !== 1) {
      throw new Error('您没有管理员权限')
    }
    
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('admin_token', res.data.token)
    localStorage.setItem('admin_user', JSON.stringify(res.data.user))
    
    return res
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
    router.push('/login')
  }

  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    login,
    logout
  }
})

