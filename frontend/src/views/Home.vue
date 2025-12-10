<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { productApi, categoryApi, bannerApi } from '@/api'
import ProductCard from '@/components/ProductCard.vue'
import Loading from '@/components/Loading.vue'

const loading = ref(true)
const banners = ref([])
const categories = ref([])
const hotProducts = ref([])
const newProducts = ref([])
const currentBanner = ref(0)

let bannerInterval = null

onMounted(async () => {
  try {
    const [bannersRes, categoriesRes, hotRes, newRes] = await Promise.all([
      bannerApi.getList(),
      categoryApi.getList(),
      productApi.getHot(8),
      productApi.getNew(8)
    ])
    
    banners.value = bannersRes.data || []
    categories.value = categoriesRes.data || []
    hotProducts.value = hotRes.data || []
    newProducts.value = newRes.data || []

    // Auto slide banners
    if (banners.value.length > 1) {
      bannerInterval = setInterval(() => {
        currentBanner.value = (currentBanner.value + 1) % banners.value.length
      }, 5000)
    }
  } catch (error) {
    console.error('Failed to load home data:', error)
  } finally {
    loading.value = false
  }
})

function goToBanner(index) {
  currentBanner.value = index
}
</script>

<template>
  <div v-if="loading" class="min-h-[60vh]">
    <Loading text="加载中..." />
  </div>

  <div v-else class="pb-12">
    <!-- Hero Banner -->
    <section class="relative overflow-hidden">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="relative aspect-[21/9] md:aspect-[3/1] rounded-2xl overflow-hidden glass">
          <!-- Banner Images -->
          <TransitionGroup name="fade">
            <div
              v-for="(banner, index) in banners"
              v-show="currentBanner === index"
              :key="banner.id"
              class="absolute inset-0"
            >
              <img 
                :src="banner.image" 
                :alt="banner.title"
                class="w-full h-full object-cover"
              />
              <div class="absolute inset-0 bg-gradient-to-r from-black/60 via-transparent to-transparent"></div>
              <div class="absolute inset-0 flex items-center px-8 md:px-16">
                <div class="max-w-lg">
                  <h2 class="text-2xl md:text-4xl font-bold text-white mb-4">{{ banner.title }}</h2>
                  <RouterLink 
                    v-if="banner.link"
                    :to="banner.link"
                    class="glass-button-primary inline-flex items-center space-x-2"
                  >
                    <span>立即查看</span>
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                  </RouterLink>
                </div>
              </div>
            </div>
          </TransitionGroup>

          <!-- Fallback if no banners -->
          <div v-if="banners.length === 0" class="absolute inset-0 flex items-center justify-center bg-gradient-to-br from-indigo-600 to-purple-700">
            <div class="text-center">
              <h2 class="text-3xl md:text-5xl font-bold text-white mb-4">欢迎来到 LightShop</h2>
              <p class="text-white/70 mb-6">发现优质好物，享受品质生活</p>
              <RouterLink to="/products" class="glass-button-primary">
                开始购物
              </RouterLink>
            </div>
          </div>

          <!-- Banner Indicators -->
          <div v-if="banners.length > 1" class="absolute bottom-4 left-1/2 -translate-x-1/2 flex space-x-2">
            <button
              v-for="(_, index) in banners"
              :key="index"
              @click="goToBanner(index)"
              :class="[
                'w-2 h-2 rounded-full transition-all duration-300',
                currentBanner === index ? 'bg-white w-6' : 'bg-white/50 hover:bg-white/80'
              ]"
            ></button>
          </div>
        </div>
      </div>
    </section>

    <!-- Categories -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-bold text-white">商品分类</h2>
        <RouterLink to="/products" class="text-indigo-400 hover:text-indigo-300 text-sm flex items-center space-x-1">
          <span>查看全部</span>
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </RouterLink>
      </div>

      <div class="grid grid-cols-4 md:grid-cols-8 gap-4">
        <RouterLink
          v-for="category in categories.slice(0, 8)"
          :key="category.id"
          :to="`/products?categoryId=${category.id}`"
          class="glass-card text-center group hover:scale-105"
        >
          <div class="text-3xl mb-2">{{ category.icon || '📦' }}</div>
          <span class="text-sm text-white/80 group-hover:text-white transition-colors">
            {{ category.name }}
          </span>
        </RouterLink>
      </div>
    </section>

    <!-- Hot Products -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center space-x-3">
          <span class="text-2xl">🔥</span>
          <h2 class="text-2xl font-bold text-white">热销商品</h2>
        </div>
        <RouterLink to="/products?sort=sales" class="text-indigo-400 hover:text-indigo-300 text-sm flex items-center space-x-1">
          <span>查看更多</span>
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </RouterLink>
      </div>

      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
        <ProductCard
          v-for="product in hotProducts"
          :key="product.id"
          :product="product"
        />
      </div>
    </section>

    <!-- New Products -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center space-x-3">
          <span class="text-2xl">✨</span>
          <h2 class="text-2xl font-bold text-white">新品上架</h2>
        </div>
        <RouterLink to="/products?sort=newest" class="text-indigo-400 hover:text-indigo-300 text-sm flex items-center space-x-1">
          <span>查看更多</span>
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </RouterLink>
      </div>

      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
        <ProductCard
          v-for="product in newProducts"
          :key="product.id"
          :product="product"
        />
      </div>
    </section>

    <!-- Features -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
        <div class="glass-card text-center">
          <div class="w-12 h-12 mx-auto mb-4 rounded-full bg-gradient-to-br from-emerald-400 to-green-500 flex items-center justify-center">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h3 class="text-white font-medium mb-1">正品保障</h3>
          <p class="text-white/50 text-sm">100% 正品</p>
        </div>

        <div class="glass-card text-center">
          <div class="w-12 h-12 mx-auto mb-4 rounded-full bg-gradient-to-br from-blue-400 to-indigo-500 flex items-center justify-center">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <h3 class="text-white font-medium mb-1">极速发货</h3>
          <p class="text-white/50 text-sm">24小时内发货</p>
        </div>

        <div class="glass-card text-center">
          <div class="w-12 h-12 mx-auto mb-4 rounded-full bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
          </div>
          <h3 class="text-white font-medium mb-1">安全支付</h3>
          <p class="text-white/50 text-sm">支付安全有保障</p>
        </div>

        <div class="glass-card text-center">
          <div class="w-12 h-12 mx-auto mb-4 rounded-full bg-gradient-to-br from-pink-400 to-rose-500 flex items-center justify-center">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
          </div>
          <h3 class="text-white font-medium mb-1">售后无忧</h3>
          <p class="text-white/50 text-sm">7天无理由退换</p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

