<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi, categoryApi } from '@/api'
import ProductCard from '@/components/ProductCard.vue'
import Loading from '@/components/Loading.vue'
import Empty from '@/components/Empty.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const products = ref([])
const categories = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(12)

const filters = ref({
  categoryId: null,
  keyword: '',
  sort: 'newest'
})

const sortOptions = [
  { value: 'newest', label: '最新上架' },
  { value: 'price_asc', label: '价格从低到高' },
  { value: 'price_desc', label: '价格从高到低' },
  { value: 'sales', label: '销量最高' }
]

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

onMounted(async () => {
  // Parse URL params
  if (route.query.categoryId) {
    filters.value.categoryId = parseInt(route.query.categoryId)
  }
  if (route.query.keyword) {
    filters.value.keyword = route.query.keyword
  }
  if (route.query.sort) {
    filters.value.sort = route.query.sort
  }

  // Load categories
  try {
    const res = await categoryApi.getList()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  }

  await loadProducts()
})

async function loadProducts() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value,
      sort: filters.value.sort
    }
    
    if (filters.value.categoryId) {
      params.categoryId = filters.value.categoryId
    }
    if (filters.value.keyword) {
      params.keyword = filters.value.keyword
    }

    const res = await productApi.getList(params)
    products.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('Failed to load products:', error)
  } finally {
    loading.value = false
  }
}

function updateFilters() {
  page.value = 1
  
  // Update URL
  const query = {}
  if (filters.value.categoryId) query.categoryId = filters.value.categoryId
  if (filters.value.keyword) query.keyword = filters.value.keyword
  if (filters.value.sort !== 'newest') query.sort = filters.value.sort

  router.replace({ query })
  loadProducts()
}

function selectCategory(categoryId) {
  filters.value.categoryId = categoryId === filters.value.categoryId ? null : categoryId
  updateFilters()
}

function goToPage(p) {
  if (p < 1 || p > totalPages.value) return
  page.value = p
  loadProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(() => route.query, () => {
  if (route.query.keyword !== filters.value.keyword) {
    filters.value.keyword = route.query.keyword || ''
    updateFilters()
  }
})
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-white mb-2">
        {{ filters.keyword ? `搜索: "${filters.keyword}"` : '全部商品' }}
      </h1>
      <p class="text-white/60">共找到 {{ total }} 件商品</p>
    </div>

    <div class="flex flex-col lg:flex-row gap-8">
      <!-- Sidebar Filters -->
      <aside class="w-full lg:w-64 shrink-0">
        <!-- Categories -->
        <div class="glass-card mb-6">
          <h3 class="text-white font-semibold mb-4">商品分类</h3>
          <div class="space-y-2">
            <button
              @click="selectCategory(null)"
              :class="[
                'w-full text-left px-3 py-2 rounded-lg transition-all',
                !filters.categoryId 
                  ? 'bg-indigo-500/30 text-white' 
                  : 'text-white/70 hover:bg-white/10 hover:text-white'
              ]"
            >
              全部分类
            </button>
            <button
              v-for="category in categories"
              :key="category.id"
              @click="selectCategory(category.id)"
              :class="[
                'w-full text-left px-3 py-2 rounded-lg transition-all flex items-center space-x-2',
                filters.categoryId === category.id 
                  ? 'bg-indigo-500/30 text-white' 
                  : 'text-white/70 hover:bg-white/10 hover:text-white'
              ]"
            >
              <span>{{ category.icon }}</span>
              <span>{{ category.name }}</span>
            </button>
          </div>
        </div>
      </aside>

      <!-- Main Content -->
      <main class="flex-1">
        <!-- Sort Options -->
        <div class="glass-card mb-6 flex flex-wrap items-center gap-4">
          <span class="text-white/60 text-sm">排序方式:</span>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="option in sortOptions"
              :key="option.value"
              @click="filters.sort = option.value; updateFilters()"
              :class="[
                'px-4 py-2 rounded-lg text-sm transition-all',
                filters.sort === option.value
                  ? 'bg-indigo-500/30 text-white'
                  : 'text-white/70 hover:bg-white/10 hover:text-white'
              ]"
            >
              {{ option.label }}
            </button>
          </div>
        </div>

        <!-- Loading -->
        <Loading v-if="loading" text="加载商品中..." />

        <!-- Empty State -->
        <Empty 
          v-else-if="products.length === 0"
          icon="search"
          title="未找到相关商品"
          description="试试其他搜索词或浏览其他分类"
        >
          <RouterLink to="/products" class="glass-button-primary mt-4">
            查看全部商品
          </RouterLink>
        </Empty>

        <!-- Products Grid -->
        <div v-else class="grid grid-cols-2 md:grid-cols-3 gap-4 md:gap-6">
          <ProductCard
            v-for="product in products"
            :key="product.id"
            :product="product"
          />
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="mt-8 flex justify-center">
          <div class="glass inline-flex rounded-xl overflow-hidden">
            <button
              @click="goToPage(page - 1)"
              :disabled="page === 1"
              class="px-4 py-2 text-white/70 hover:bg-white/10 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            
            <template v-for="p in totalPages" :key="p">
              <button
                v-if="p === 1 || p === totalPages || (p >= page - 2 && p <= page + 2)"
                @click="goToPage(p)"
                :class="[
                  'px-4 py-2 transition-colors',
                  p === page
                    ? 'bg-indigo-500 text-white'
                    : 'text-white/70 hover:bg-white/10'
                ]"
              >
                {{ p }}
              </button>
              <span 
                v-else-if="p === page - 3 || p === page + 3"
                class="px-2 py-2 text-white/50"
              >
                ...
              </span>
            </template>

            <button
              @click="goToPage(page + 1)"
              :disabled="page === totalPages"
              class="px-4 py-2 text-white/70 hover:bg-white/10 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

