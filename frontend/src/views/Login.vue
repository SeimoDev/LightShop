<script setup>
import { ref } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useToastStore } from '@/stores/toast'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const toast = useToastStore()

const loading = ref(false)
const form = ref({
  username: '',
  password: ''
})

async function handleSubmit() {
  if (!form.value.username || !form.value.password) {
    toast.warning('请填写用户名和密码')
    return
  }

  loading.value = true
  try {
    await userStore.login(form.value)
    toast.success('登录成功')
    
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-[80vh] flex items-center justify-center px-4 py-12">
    <div class="w-full max-w-md">
      <div class="glass-card">
        <div class="text-center mb-8">
          <div class="w-16 h-16 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center">
            <span class="text-white text-2xl font-bold">L</span>
          </div>
          <h1 class="text-2xl font-bold text-white">欢迎回来</h1>
          <p class="text-white/60 mt-2">登录您的 LightShop 账号</p>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-6">
          <div>
            <label class="block text-white/70 text-sm mb-2">用户名</label>
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              class="glass-input"
              autocomplete="username"
            />
          </div>

          <div>
            <label class="block text-white/70 text-sm mb-2">密码</label>
            <input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              class="glass-input"
              autocomplete="current-password"
            />
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full glass-button-primary py-4 flex items-center justify-center space-x-2"
          >
            <span v-if="loading" class="spinner w-5 h-5"></span>
            <span>{{ loading ? '登录中...' : '登录' }}</span>
          </button>
        </form>

        <div class="mt-6 text-center">
          <p class="text-white/60">
            还没有账号？
            <RouterLink to="/register" class="text-indigo-400 hover:text-indigo-300">
              立即注册
            </RouterLink>
          </p>
        </div>

        <!-- Demo Account Info -->
        <div class="mt-6 p-4 rounded-xl bg-white/5 border border-white/10">
          <p class="text-white/50 text-sm text-center">
            演示账号：admin / admin123
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

