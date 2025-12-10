<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi, favoriteApi, reviewApi } from '@/api'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { useToastStore } from '@/stores/toast'
import Loading from '@/components/Loading.vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const toast = useToastStore()

const loading = ref(true)
const product = ref(null)
const reviews = ref([])
const reviewCount = ref(0)
const averageRating = ref(0)
const quantity = ref(1)
const isFavorited = ref(false)
const currentImage = ref(0)

const images = computed(() => {
  if (!product.value?.images) return []
  try {
    const imgs = typeof product.value.images === 'string' 
      ? JSON.parse(product.value.images) 
      : product.value.images
    return Array.isArray(imgs) ? imgs : [imgs]
  } catch {
    return [product.value.images]
  }
})

const inStock = computed(() => product.value?.stock > 0)
const maxQuantity = computed(() => Math.min(product.value?.stock || 0, 99))

onMounted(async () => {
  await loadProduct()
  if (userStore.isLoggedIn) {
    checkFavorite()
  }
})

async function loadProduct() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await productApi.getDetail(id)
    product.value = res.data.product
    reviewCount.value = res.data.reviewCount || 0
    averageRating.value = res.data.averageRating || 0

    // Load reviews
    const reviewsRes = await reviewApi.getList(id, { page: 1, pageSize: 10 })
    reviews.value = reviewsRes.data.list || []
  } catch (error) {
    toast.error('加载商品失败')
    router.push('/products')
  } finally {
    loading.value = false
  }
}

async function checkFavorite() {
  try {
    const res = await favoriteApi.check(route.params.id)
    isFavorited.value = res.data.isFavorited
  } catch {
    // Ignore
  }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    toast.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const res = await favoriteApi.toggle(product.value.id)
    isFavorited.value = res.data.isFavorited
    toast.success(res.message)
  } catch {
    // Error handled by interceptor
  }
}

async function addToCart() {
  if (!userStore.isLoggedIn) {
    toast.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await cartStore.addItem(product.value.id, quantity.value)
    toast.success('已添加到购物车')
  } catch {
    // Error handled by interceptor
  }
}

async function buyNow() {
  if (!userStore.isLoggedIn) {
    toast.warning('请先登录')
    router.push('/login')
    return
  }

  // Store buy info and go to checkout
  sessionStorage.setItem('checkout', JSON.stringify({
    fromCart: false,
    items: [{
      productId: product.value.id,
      quantity: quantity.value
    }]
  }))
  router.push('/checkout')
}

function updateQuantity(delta) {
  const newVal = quantity.value + delta
  if (newVal >= 1 && newVal <= maxQuantity.value) {
    quantity.value = newVal
  }
}
</script>

<template>
  <div class="min-h-[60vh]">
    <Loading v-if="loading" text="加载商品详情..." />

    <div v-else-if="product" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Breadcrumb -->
      <nav class="mb-6 text-sm">
        <ol class="flex items-center space-x-2 text-white/60">
          <li><RouterLink to="/" class="hover:text-white">首页</RouterLink></li>
          <li>/</li>
          <li><RouterLink to="/products" class="hover:text-white">商品</RouterLink></li>
          <li>/</li>
          <li class="text-white">{{ product.name }}</li>
        </ol>
      </nav>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-12">
        <!-- Images -->
        <div class="space-y-4">
          <div class="glass rounded-2xl overflow-hidden aspect-square">
            <img 
              :src="images[currentImage] || 'https://picsum.photos/600/600'" 
              :alt="product.name"
              class="w-full h-full object-cover"
            />
          </div>
          
          <div v-if="images.length > 1" class="flex space-x-2 overflow-x-auto pb-2">
            <button
              v-for="(img, index) in images"
              :key="index"
              @click="currentImage = index"
              :class="[
                'w-20 h-20 rounded-lg overflow-hidden flex-shrink-0 border-2 transition-all',
                currentImage === index ? 'border-indigo-500' : 'border-transparent opacity-60 hover:opacity-100'
              ]"
            >
              <img :src="img" :alt="`Image ${index + 1}`" class="w-full h-full object-cover" />
            </button>
          </div>
        </div>

        <!-- Product Info -->
        <div class="space-y-6">
          <div>
            <h1 class="text-2xl md:text-3xl font-bold text-white mb-2">{{ product.name }}</h1>
            <p class="text-white/60">{{ product.categoryName }}</p>
          </div>

          <!-- Price -->
          <div class="glass-card">
            <div class="flex items-baseline space-x-4">
              <span class="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-pink-400 to-purple-400">
                ¥{{ product.price.toFixed(2) }}
              </span>
              <span v-if="product.originalPrice > product.price" class="text-lg text-white/40 line-through">
                ¥{{ product.originalPrice.toFixed(2) }}
              </span>
              <span 
                v-if="product.originalPrice > product.price" 
                class="px-2 py-1 bg-rose-500/20 text-rose-400 text-xs rounded"
              >
                省 ¥{{ (product.originalPrice - product.price).toFixed(2) }}
              </span>
            </div>
            
            <div class="mt-4 flex items-center space-x-6 text-sm text-white/60">
              <span>销量: {{ product.sales }}</span>
              <span>库存: {{ product.stock }}</span>
              <span v-if="reviewCount > 0">评价: {{ reviewCount }}</span>
            </div>
          </div>

          <!-- Quantity -->
          <div class="flex items-center space-x-4">
            <span class="text-white/70">数量</span>
            <div class="glass inline-flex items-center rounded-xl overflow-hidden">
              <button 
                @click="updateQuantity(-1)"
                :disabled="quantity <= 1"
                class="w-10 h-10 flex items-center justify-center text-white/70 hover:bg-white/10 disabled:opacity-30"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
                </svg>
              </button>
              <input 
                v-model.number="quantity"
                type="number"
                :min="1"
                :max="maxQuantity"
                class="w-16 h-10 text-center bg-transparent text-white border-x border-white/10 focus:outline-none"
              />
              <button 
                @click="updateQuantity(1)"
                :disabled="quantity >= maxQuantity"
                class="w-10 h-10 flex items-center justify-center text-white/70 hover:bg-white/10 disabled:opacity-30"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
              </button>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex items-center space-x-4">
            <button
              @click="addToCart"
              :disabled="!inStock"
              class="flex-1 glass-button flex items-center justify-center space-x-2 disabled:opacity-50"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              <span>加入购物车</span>
            </button>

            <button
              @click="buyNow"
              :disabled="!inStock"
              class="flex-1 glass-button-primary disabled:opacity-50"
            >
              {{ inStock ? '立即购买' : '已售罄' }}
            </button>

            <button
              @click="toggleFavorite"
              :class="[
                'glass-button p-3',
                isFavorited ? 'text-rose-400' : 'text-white/70 hover:text-rose-400'
              ]"
            >
              <svg class="w-6 h-6" :fill="isFavorited ? 'currentColor' : 'none'" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
              </svg>
            </button>
          </div>

          <!-- Description -->
          <div class="glass-card">
            <h3 class="text-white font-semibold mb-3">商品详情</h3>
            <p class="text-white/70 leading-relaxed whitespace-pre-wrap">{{ product.description || '暂无商品描述' }}</p>
          </div>
        </div>
      </div>

      <!-- Reviews Section -->
      <div class="glass-card">
        <h3 class="text-xl font-bold text-white mb-6">商品评价 ({{ reviewCount }})</h3>

        <div v-if="reviews.length === 0" class="text-center py-8 text-white/60">
          暂无评价
        </div>

        <div v-else class="space-y-6">
          <div v-for="review in reviews" :key="review.id" class="border-b border-white/10 pb-6 last:border-0">
            <div class="flex items-center space-x-3 mb-3">
              <div class="w-10 h-10 rounded-full bg-gradient-to-br from-indigo-400 to-purple-500 flex items-center justify-center">
                <span class="text-white text-sm">{{ review.username?.charAt(0).toUpperCase() }}</span>
              </div>
              <div>
                <p class="text-white font-medium">{{ review.username }}</p>
                <div class="flex items-center space-x-1">
                  <template v-for="i in 5" :key="i">
                    <svg 
                      class="w-4 h-4" 
                      :class="i <= review.rating ? 'text-amber-400' : 'text-white/20'"
                      fill="currentColor" 
                      viewBox="0 0 20 20"
                    >
                      <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                    </svg>
                  </template>
                </div>
              </div>
            </div>
            <p class="text-white/70">{{ review.content }}</p>
            <p class="text-white/40 text-sm mt-2">{{ review.createdAt }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

