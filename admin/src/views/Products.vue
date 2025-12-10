<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { productApi, categoryApi } from '@/api'

const loading = ref(true)
const products = ref([])
const categories = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const categoryId = ref('')

const statusText = { 0: '已下架', 1: '上架中' }
const statusClass = { 0: 'badge-danger', 1: 'badge-success' }

onMounted(async () => {
  await loadCategories()
  await loadProducts()
})

async function loadCategories() {
  try {
    const res = await categoryApi.getList()
    categories.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    if (categoryId.value) params.categoryId = categoryId.value

    const res = await productApi.getList(params)
    products.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  loadProducts()
}

async function toggleStatus(product) {
  try {
    await productApi.update(product.id, { status: product.status === 1 ? 0 : 1 })
    product.status = product.status === 1 ? 0 : 1
  } catch (e) {
    console.error(e)
  }
}

async function deleteProduct(id) {
  if (!confirm('确定要删除这个商品吗？')) return
  try {
    await productApi.delete(id)
    loadProducts()
  } catch (e) {
    console.error(e)
  }
}

function getImage(images) {
  if (!images) return 'https://picsum.photos/100'
  try {
    const arr = JSON.parse(images)
    return arr[0] || 'https://picsum.photos/100'
  } catch {
    return images
  }
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-8">
      <h1 class="text-2xl font-bold text-white">商品管理</h1>
      <RouterLink to="/products/add" class="btn-primary">
        添加商品
      </RouterLink>
    </div>

    <!-- Filters -->
    <div class="card mb-6">
      <div class="flex flex-wrap gap-4">
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索商品名称..."
          class="input w-64"
          @keyup.enter="search"
        />
        <select v-model="categoryId" class="select w-48" @change="search">
          <option value="">全部分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">
            {{ cat.name }}
          </option>
        </select>
        <button @click="search" class="btn-secondary">搜索</button>
      </div>
    </div>

    <!-- Table -->
    <div class="card overflow-x-auto">
      <table class="table">
        <thead>
          <tr>
            <th>商品</th>
            <th>价格</th>
            <th>库存</th>
            <th>销量</th>
            <th>分类</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="text-center py-8 text-slate-500">加载中...</td>
          </tr>
          <tr v-else-if="products.length === 0">
            <td colspan="7" class="text-center py-8 text-slate-500">暂无商品</td>
          </tr>
          <tr v-else v-for="product in products" :key="product.id">
            <td>
              <div class="flex items-center space-x-3">
                <img :src="getImage(product.images)" class="w-12 h-12 rounded-lg object-cover" />
                <div>
                  <p class="text-white font-medium line-clamp-1 max-w-xs">{{ product.name }}</p>
                  <p class="text-slate-500 text-xs">ID: {{ product.id }}</p>
                </div>
              </div>
            </td>
            <td>
              <span class="text-indigo-400">¥{{ product.price }}</span>
              <span v-if="product.originalPrice > product.price" class="text-slate-500 text-xs line-through ml-1">
                ¥{{ product.originalPrice }}
              </span>
            </td>
            <td>
              <span :class="product.stock < 10 ? 'text-amber-400' : 'text-white'">
                {{ product.stock }}
              </span>
            </td>
            <td class="text-white">{{ product.sales }}</td>
            <td class="text-slate-400">{{ product.categoryName || '-' }}</td>
            <td>
              <span :class="['badge', statusClass[product.status]]">
                {{ statusText[product.status] }}
              </span>
            </td>
            <td>
              <div class="flex items-center space-x-2">
                <RouterLink :to="`/products/${product.id}/edit`" class="text-indigo-400 hover:text-indigo-300">
                  编辑
                </RouterLink>
                <button @click="toggleStatus(product)" class="text-slate-400 hover:text-white">
                  {{ product.status === 1 ? '下架' : '上架' }}
                </button>
                <button @click="deleteProduct(product.id)" class="text-rose-400 hover:text-rose-300">
                  删除
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
            @click="page--; loadProducts()"
            :disabled="page === 1"
            class="btn-secondary px-3 py-1 text-sm disabled:opacity-50"
          >
            上一页
          </button>
          <button
            @click="page++; loadProducts()"
            :disabled="page >= Math.ceil(total / pageSize)"
            class="btn-secondary px-3 py-1 text-sm disabled:opacity-50"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

