<script setup>
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useToastStore } from '@/stores/toast'

const router = useRouter()
const userStore = useUserStore()
const toast = useToastStore()

const loading = ref(false)
const form = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

async function handleSubmit() {
  if (!form.value.username || !form.value.password) {
    toast.warning('请填写用户名和密码')
    return
  }

  if (form.value.password !== form.value.confirmPassword) {
    toast.warning('两次密码输入不一致')
    return
  }

  if (form.value.password.length < 6) {
    toast.warning('密码长度不能少于6位')
    return
  }

  loading.value = true
  try {
    await userStore.register({
      username: form.value.username,
      email: form.value.email,
      password: form.value.password
    })
    toast.success('注册成功')
    router.push('/')
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
          <h1 class="text-2xl font-bold text-white">创建账号</h1>
          <p class="text-white/60 mt-2">加入 LightShop，开启购物之旅</p>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-5">
          <div>
            <label class="block text-white/70 text-sm mb-2">用户名 *</label>
            <input
              v-model="form.username"
              type="text"
              placeholder="3-20位字母、数字或下划线"
              class="glass-input"
              autocomplete="username"
            />
          </div>

          <div>
            <label class="block text-white/70 text-sm mb-2">邮箱</label>
            <input
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱（选填）"
              class="glass-input"
              autocomplete="email"
            />
          </div>

          <div>
            <label class="block text-white/70 text-sm mb-2">密码 *</label>
            <input
              v-model="form.password"
              type="password"
              placeholder="至少6位字符"
              class="glass-input"
              autocomplete="new-password"
            />
          </div>

          <div>
            <label class="block text-white/70 text-sm mb-2">确认密码 *</label>
            <input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              class="glass-input"
              autocomplete="new-password"
            />
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full glass-button-primary py-4 flex items-center justify-center space-x-2"
          >
            <span v-if="loading" class="spinner w-5 h-5"></span>
            <span>{{ loading ? '注册中...' : '注册' }}</span>
          </button>
        </form>

        <div class="mt-6 text-center">
          <p class="text-white/60">
            已有账号？
            <RouterLink to="/login" class="text-indigo-400 hover:text-indigo-300">
              立即登录
            </RouterLink>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

