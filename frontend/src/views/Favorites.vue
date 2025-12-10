<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { favoriteApi } from '@/api'
import { useCartStore } from '@/stores/cart'
import { useToastStore } from '@/stores/toast'
import Loading from '@/components/Loading.vue'
import Empty from '@/components/Empty.vue'

const cartStore = useCartStore()
const toast = useToastStore()

const loading = ref(true)
const favorites = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(12)

onMounted(() => {
  loadFavorites()
})

async function loadFavorites() {
  loading.value = true
  try {
    const res = await favoriteApi.getList({ page: page.value, pageSize: pageSize.value })
    favorites.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('Failed to load favorites:', error)
  } finally {
    loading.value = false
  }
}

async function removeFavorite(item) {
  try {
    await favoriteApi.remove(item.productId)
    favorites.value = favorites.value.filter(f => f.id !== item.id)
    total.value--
    toast.success('已取消收藏')
  } catch {
    // Error handled by interceptor
  }
}

async function addToCart(item) {
  try {
    await cartStore.addItem(item.productId, 1)
    toast.success('已添加到购物车')
  } catch {
    // Error handled by interceptor
  }
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-2xl font-bold text-white mb-8">我的收藏</h1>

    <Loading v-if="loading" text="加载收藏..." />

    <Empty 
      v-else-if="favorites.length === 0"
      icon="heart"
      title="暂无收藏"
      description="快去发现喜欢的商品吧"
    >
      <RouterLink to="/products" class="glass-button-primary mt-4">
        去逛逛
      </RouterLink>
    </Empty>

    <div v-else class="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
      <div
        v-for="item in favorites"
        :key="item.id"
        class="glass-card group"
      >
        <RouterLink :to="`/product/${item.productId}`" class="block">
          <div class="relative aspect-square overflow-hidden rounded-xl mb-4">
            <img 
              :src="item.productImage || 'https://picsum.photos/400/400'" 
              :alt="item.productName"
              class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
              loading="lazy"
            />
          </div>

          <h3 class="text-white font-medium line-clamp-2 group-hover:text-indigo-300 transition-colors mb-2">
            {{ item.productName }}
          </h3>
          
          <p class="text-xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-pink-400 to-purple-400">
            ¥{{ item.productPrice?.toFixed(2) }}
          </p>
        </RouterLink>

        <div class="flex items-center space-x-2 mt-4">
          <button
            @click="addToCart(item)"
            class="flex-1 glass-button text-sm flex items-center justify-center space-x-1"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            <span>加入购物车</span>
          </button>
          <button
            @click="removeFavorite(item)"
            class="p-2 text-rose-400 hover:bg-rose-500/20 rounded-lg transition-colors"
          >
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
              <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

