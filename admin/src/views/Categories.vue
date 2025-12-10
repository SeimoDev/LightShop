<script setup>
import { ref, onMounted } from 'vue'
import { categoryApi } from '@/api'

const loading = ref(true)
const categories = ref([])
const showModal = ref(false)
const editingCategory = ref(null)

const form = ref({
  name: '',
  icon: '',
  sortOrder: 0,
  status: 1
})

const emojiList = ['📱', '💻', '🏠', '👕', '💄', '🍎', '📚', '⚽', '🎮', '🎧', '📷', '🚗', '🔧', '💡', '🎁', '🛒']

onMounted(async () => {
  await loadCategories()
})

async function loadCategories() {
  loading.value = true
  try {
    const res = await categoryApi.getList()
    categories.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openAddModal() {
  editingCategory.value = null
  form.value = { name: '', icon: '', sortOrder: 0, status: 1 }
  showModal.value = true
}

function openEditModal(category) {
  editingCategory.value = category
  form.value = {
    name: category.name,
    icon: category.icon || '',
    sortOrder: category.sortOrder || 0,
    status: category.status
  }
  showModal.value = true
}

function selectEmoji(emoji) {
  form.value.icon = emoji
}

async function submit() {
  if (!form.value.name.trim()) {
    alert('请输入分类名称')
    return
  }

  try {
    if (editingCategory.value) {
      await categoryApi.update(editingCategory.value.id, form.value)
      alert('更新成功')
    } else {
      await categoryApi.create(form.value)
      alert('创建成功')
    }
    showModal.value = false
    loadCategories()
  } catch (e) {
    console.error(e)
    alert('操作失败')
  }
}

async function deleteCategory(id) {
  if (!confirm('确定要删除这个分类吗？')) return
  
  try {
    await categoryApi.delete(id)
    loadCategories()
  } catch (e) {
    console.error(e)
    alert('删除失败')
  }
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-8">
      <h1 class="text-2xl font-bold text-white">分类管理</h1>
      <button @click="openAddModal" class="btn-primary">添加分类</button>
    </div>

    <!-- Categories Table -->
    <div class="card overflow-x-auto">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>图标</th>
            <th>名称</th>
            <th>排序</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="text-center py-8 text-slate-500">加载中...</td>
          </tr>
          <tr v-else-if="categories.length === 0">
            <td colspan="6" class="text-center py-8 text-slate-500">暂无分类</td>
          </tr>
          <tr v-else v-for="category in categories" :key="category.id">
            <td class="text-slate-400">{{ category.id }}</td>
            <td class="text-2xl">{{ category.icon || '📦' }}</td>
            <td class="text-white font-medium">{{ category.name }}</td>
            <td class="text-slate-400">{{ category.sortOrder }}</td>
            <td>
              <span :class="['badge', category.status === 1 ? 'badge-success' : 'badge-danger']">
                {{ category.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>
              <div class="flex items-center space-x-2">
                <button @click="openEditModal(category)" class="text-indigo-400 hover:text-indigo-300">
                  编辑
                </button>
                <button @click="deleteCategory(category.id)" class="text-rose-400 hover:text-rose-300">
                  删除
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-slate-800 rounded-2xl p-6 w-full max-w-md mx-4">
        <h2 class="text-xl font-bold text-white mb-6">
          {{ editingCategory ? '编辑分类' : '添加分类' }}
        </h2>

        <form @submit.prevent="submit" class="space-y-4">
          <div>
            <label class="block text-slate-300 mb-2">分类名称</label>
            <input v-model="form.name" type="text" class="input" placeholder="请输入分类名称" />
          </div>

          <div>
            <label class="block text-slate-300 mb-2">图标</label>
            <div class="flex items-center space-x-3 mb-2">
              <div class="w-12 h-12 rounded-lg bg-slate-700 flex items-center justify-center text-2xl">
                {{ form.icon || '📦' }}
              </div>
              <input v-model="form.icon" type="text" class="input flex-1" placeholder="输入 emoji 或选择下方" />
            </div>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="emoji in emojiList"
                :key="emoji"
                type="button"
                @click="selectEmoji(emoji)"
                :class="['w-10 h-10 rounded-lg text-xl flex items-center justify-center transition-colors', 
                  form.icon === emoji ? 'bg-indigo-500' : 'bg-slate-700 hover:bg-slate-600']"
              >
                {{ emoji }}
              </button>
            </div>
          </div>

          <div>
            <label class="block text-slate-300 mb-2">排序</label>
            <input v-model.number="form.sortOrder" type="number" class="input" min="0" />
            <p class="text-slate-500 text-sm mt-1">数字越小越靠前</p>
          </div>

          <div>
            <label class="block text-slate-300 mb-2">状态</label>
            <div class="flex items-center space-x-6">
              <label class="flex items-center space-x-2 cursor-pointer">
                <input type="radio" v-model="form.status" :value="1" class="text-indigo-500" />
                <span class="text-white">启用</span>
              </label>
              <label class="flex items-center space-x-2 cursor-pointer">
                <input type="radio" v-model="form.status" :value="0" class="text-indigo-500" />
                <span class="text-white">禁用</span>
              </label>
            </div>
          </div>

          <div class="flex items-center justify-end space-x-4 pt-4">
            <button type="button" @click="showModal = false" class="btn-secondary">
              取消
            </button>
            <button type="submit" class="btn-primary">
              {{ editingCategory ? '保存修改' : '创建分类' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

