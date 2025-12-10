<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api'

const loading = ref(true)
const users = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const showModal = ref(false)
const currentUser = ref(null)

const roleText = {
  0: '普通用户',
  1: '管理员'
}

const statusText = {
  0: '已禁用',
  1: '正常'
}

const statusClass = {
  0: 'badge-danger',
  1: 'badge-success'
}

onMounted(async () => {
  await loadUsers()
})

async function loadUsers() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (keyword.value) params.keyword = keyword.value

    const res = await userApi.getList(params)
    users.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  loadUsers()
}

async function viewUser(user) {
  try {
    const res = await userApi.getDetail(user.id)
    currentUser.value = res.data
    showModal.value = true
  } catch (e) {
    console.error(e)
    alert('获取用户详情失败')
  }
}

async function toggleStatus(user) {
  const action = user.status === 1 ? '禁用' : '启用'
  if (!confirm(`确定要${action}此用户吗？`)) return

  try {
    await userApi.update(user.id, { status: user.status === 1 ? 0 : 1 })
    user.status = user.status === 1 ? 0 : 1
    alert(`${action}成功`)
  } catch (e) {
    console.error(e)
    alert(`${action}失败`)
  }
}

async function updateBalance(userId) {
  const amount = prompt('请输入要增加的余额（负数为扣减）：')
  if (amount === null) return

  const num = parseFloat(amount)
  if (isNaN(num)) {
    alert('请输入有效数字')
    return
  }

  try {
    const user = users.value.find(u => u.id === userId)
    await userApi.update(userId, { balance: user.balance + num })
    user.balance += num
    if (currentUser.value && currentUser.value.id === userId) {
      currentUser.value.balance += num
    }
    alert('余额更新成功')
  } catch (e) {
    console.error(e)
    alert('余额更新失败')
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-8">
      <h1 class="text-2xl font-bold text-white">用户管理</h1>
    </div>

    <!-- Filters -->
    <div class="card mb-6">
      <div class="flex flex-wrap gap-4">
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索用户名、邮箱..."
          class="input w-64"
          @keyup.enter="search"
        />
        <button @click="search" class="btn-secondary">搜索</button>
      </div>
    </div>

    <!-- Users Table -->
    <div class="card overflow-x-auto">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户</th>
            <th>邮箱</th>
            <th>手机</th>
            <th>余额</th>
            <th>角色</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="9" class="text-center py-8 text-slate-500">加载中...</td>
          </tr>
          <tr v-else-if="users.length === 0">
            <td colspan="9" class="text-center py-8 text-slate-500">暂无用户</td>
          </tr>
          <tr v-else v-for="user in users" :key="user.id">
            <td class="text-slate-400">{{ user.id }}</td>
            <td>
              <div class="flex items-center space-x-3">
                <img 
                  :src="user.avatar || `https://api.dicebear.com/7.x/initials/svg?seed=${user.username}`"
                  class="w-8 h-8 rounded-full"
                />
                <span class="text-white font-medium">{{ user.username }}</span>
              </div>
            </td>
            <td class="text-slate-300">{{ user.email || '-' }}</td>
            <td class="text-slate-300">{{ user.phone || '-' }}</td>
            <td class="text-indigo-400">¥{{ user.balance?.toFixed(2) || '0.00' }}</td>
            <td>
              <span :class="['badge', user.role === 1 ? 'badge-warning' : 'badge-info']">
                {{ roleText[user.role] }}
              </span>
            </td>
            <td>
              <span :class="['badge', statusClass[user.status]]">
                {{ statusText[user.status] }}
              </span>
            </td>
            <td class="text-slate-400 text-sm">{{ formatDate(user.createdAt) }}</td>
            <td>
              <div class="flex items-center space-x-2">
                <button @click="viewUser(user)" class="text-indigo-400 hover:text-indigo-300">
                  详情
                </button>
                <button
                  v-if="user.role !== 1"
                  @click="toggleStatus(user)"
                  :class="user.status === 1 ? 'text-rose-400 hover:text-rose-300' : 'text-emerald-400 hover:text-emerald-300'"
                >
                  {{ user.status === 1 ? '禁用' : '启用' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div v-if="total > pageSize" class="flex items-center justify-between mt-6 pt-6 border-t border-slate-700">
        <p class="text-slate-500 text-sm">共 {{ total }} 条</p>
        <div class="flex space-x-2">
          <button
            @click="page--; loadUsers()"
            :disabled="page === 1"
            class="btn-secondary px-3 py-1 text-sm disabled:opacity-50"
          >
            上一页
          </button>
          <button
            @click="page++; loadUsers()"
            :disabled="page >= Math.ceil(total / pageSize)"
            class="btn-secondary px-3 py-1 text-sm disabled:opacity-50"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- User Detail Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-slate-800 rounded-2xl p-6 w-full max-w-lg">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-white">用户详情</h2>
          <button @click="showModal = false" class="text-slate-400 hover:text-white">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div v-if="currentUser" class="space-y-6">
          <!-- User Avatar & Name -->
          <div class="flex items-center space-x-4">
            <img 
              :src="currentUser.avatar || `https://api.dicebear.com/7.x/initials/svg?seed=${currentUser.username}`"
              class="w-16 h-16 rounded-full"
            />
            <div>
              <h3 class="text-xl font-bold text-white">{{ currentUser.username }}</h3>
              <span :class="['badge', currentUser.role === 1 ? 'badge-warning' : 'badge-info']">
                {{ roleText[currentUser.role] }}
              </span>
            </div>
          </div>

          <!-- User Info -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <p class="text-slate-500 text-sm">用户 ID</p>
              <p class="text-white">{{ currentUser.id }}</p>
            </div>
            <div>
              <p class="text-slate-500 text-sm">状态</p>
              <span :class="['badge', statusClass[currentUser.status]]">
                {{ statusText[currentUser.status] }}
              </span>
            </div>
            <div>
              <p class="text-slate-500 text-sm">邮箱</p>
              <p class="text-white">{{ currentUser.email || '-' }}</p>
            </div>
            <div>
              <p class="text-slate-500 text-sm">手机</p>
              <p class="text-white">{{ currentUser.phone || '-' }}</p>
            </div>
            <div>
              <p class="text-slate-500 text-sm">注册时间</p>
              <p class="text-white">{{ formatDate(currentUser.createdAt) }}</p>
            </div>
            <div>
              <p class="text-slate-500 text-sm">余额</p>
              <div class="flex items-center space-x-2">
                <p class="text-indigo-400 font-bold">¥{{ currentUser.balance?.toFixed(2) }}</p>
                <button 
                  @click="updateBalance(currentUser.id)"
                  class="text-xs text-slate-400 hover:text-white"
                >
                  调整
                </button>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex items-center justify-end space-x-4 pt-4 border-t border-slate-700">
            <button @click="showModal = false" class="btn-secondary">关闭</button>
            <button
              v-if="currentUser.role !== 1"
              @click="toggleStatus(currentUser); showModal = false"
              :class="currentUser.status === 1 
                ? 'bg-rose-500 hover:bg-rose-600' 
                : 'bg-emerald-500 hover:bg-emerald-600'"
              class="text-white px-4 py-2 rounded-lg transition-colors"
            >
              {{ currentUser.status === 1 ? '禁用用户' : '启用用户' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

