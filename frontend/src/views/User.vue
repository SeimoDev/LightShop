<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useToastStore } from '@/stores/toast'

const userStore = useUserStore()
const toast = useToastStore()

const editing = ref(false)
const saving = ref(false)
const changingPassword = ref(false)

const form = ref({
  email: '',
  phone: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(() => {
  form.value.email = userStore.user?.email || ''
  form.value.phone = userStore.user?.phone || ''
})

async function saveProfile() {
  saving.value = true
  try {
    await userStore.updateProfile(form.value)
    toast.success('保存成功')
    editing.value = false
  } catch {
    // Error handled by interceptor
  } finally {
    saving.value = false
  }
}

async function changePassword() {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    toast.warning('两次密码输入不一致')
    return
  }

  if (passwordForm.value.newPassword.length < 6) {
    toast.warning('新密码长度不能少于6位')
    return
  }

  saving.value = true
  try {
    await userStore.updateProfile({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    toast.success('密码修改成功')
    changingPassword.value = false
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch {
    // Error handled by interceptor
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-2xl font-bold text-white mb-8">个人中心</h1>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
      <!-- Quick Links -->
      <div class="md:col-span-1 space-y-4">
        <div class="glass-card text-center">
          <div class="w-20 h-20 mx-auto mb-4 rounded-full bg-gradient-to-br from-indigo-400 to-purple-500 flex items-center justify-center">
            <span class="text-white text-3xl font-bold">
              {{ userStore.user?.username?.charAt(0).toUpperCase() }}
            </span>
          </div>
          <h2 class="text-xl font-bold text-white">{{ userStore.user?.username }}</h2>
          <p class="text-white/50 text-sm">{{ userStore.user?.email || '未设置邮箱' }}</p>
          
          <div class="mt-4 p-4 rounded-xl bg-gradient-to-r from-indigo-500/20 to-purple-500/20">
            <p class="text-white/50 text-sm">账户余额</p>
            <p class="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-pink-400 to-purple-400">
              ¥{{ userStore.user?.balance?.toFixed(2) || '0.00' }}
            </p>
          </div>
        </div>

        <div class="glass-card space-y-2">
          <RouterLink to="/orders" class="block px-4 py-3 rounded-xl text-white/70 hover:text-white hover:bg-white/10 transition-colors">
            <span class="flex items-center space-x-3">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <span>我的订单</span>
            </span>
          </RouterLink>
          <RouterLink to="/favorites" class="block px-4 py-3 rounded-xl text-white/70 hover:text-white hover:bg-white/10 transition-colors">
            <span class="flex items-center space-x-3">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
              </svg>
              <span>我的收藏</span>
            </span>
          </RouterLink>
          <RouterLink to="/addresses" class="block px-4 py-3 rounded-xl text-white/70 hover:text-white hover:bg-white/10 transition-colors">
            <span class="flex items-center space-x-3">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span>收货地址</span>
            </span>
          </RouterLink>
        </div>
      </div>

      <!-- Profile Form -->
      <div class="md:col-span-2 space-y-6">
        <!-- Basic Info -->
        <div class="glass-card">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-lg font-bold text-white">基本信息</h2>
            <button
              v-if="!editing"
              @click="editing = true"
              class="text-indigo-400 hover:text-indigo-300 text-sm"
            >
              编辑
            </button>
          </div>

          <div class="space-y-4">
            <div>
              <label class="block text-white/70 text-sm mb-2">用户名</label>
              <input
                :value="userStore.user?.username"
                type="text"
                disabled
                class="glass-input opacity-50 cursor-not-allowed"
              />
            </div>

            <div>
              <label class="block text-white/70 text-sm mb-2">邮箱</label>
              <input
                v-model="form.email"
                type="email"
                :disabled="!editing"
                placeholder="请输入邮箱"
                class="glass-input"
                :class="{ 'opacity-50': !editing }"
              />
            </div>

            <div>
              <label class="block text-white/70 text-sm mb-2">手机号</label>
              <input
                v-model="form.phone"
                type="tel"
                :disabled="!editing"
                placeholder="请输入手机号"
                class="glass-input"
                :class="{ 'opacity-50': !editing }"
              />
            </div>

            <div v-if="editing" class="flex items-center space-x-4 pt-4">
              <button
                @click="saveProfile"
                :disabled="saving"
                class="glass-button-primary"
              >
                {{ saving ? '保存中...' : '保存' }}
              </button>
              <button
                @click="editing = false"
                class="glass-button"
              >
                取消
              </button>
            </div>
          </div>
        </div>

        <!-- Password -->
        <div class="glass-card">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-lg font-bold text-white">修改密码</h2>
            <button
              v-if="!changingPassword"
              @click="changingPassword = true"
              class="text-indigo-400 hover:text-indigo-300 text-sm"
            >
              修改
            </button>
          </div>

          <div v-if="changingPassword" class="space-y-4">
            <div>
              <label class="block text-white/70 text-sm mb-2">原密码</label>
              <input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                class="glass-input"
              />
            </div>

            <div>
              <label class="block text-white/70 text-sm mb-2">新密码</label>
              <input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="至少6位字符"
                class="glass-input"
              />
            </div>

            <div>
              <label class="block text-white/70 text-sm mb-2">确认新密码</label>
              <input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                class="glass-input"
              />
            </div>

            <div class="flex items-center space-x-4 pt-4">
              <button
                @click="changePassword"
                :disabled="saving"
                class="glass-button-primary"
              >
                {{ saving ? '保存中...' : '确认修改' }}
              </button>
              <button
                @click="changingPassword = false"
                class="glass-button"
              >
                取消
              </button>
            </div>
          </div>

          <p v-else class="text-white/50">
            定期修改密码有助于保护账户安全
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

