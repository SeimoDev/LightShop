<script setup>
import { ref, computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const showUserMenu = ref(false)
const showMobileMenu = ref(false)

const cartCount = computed(() => cartStore.totalQuantity)

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'Products', query: { keyword: searchKeyword.value } })
    searchKeyword.value = ''
  }
}

function handleLogout() {
  userStore.logout()
  showUserMenu.value = false
}
</script>

<template>
  <nav class="fixed top-0 left-0 right-0 z-50 glass border-b border-white/10">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <RouterLink to="/" class="flex items-center space-x-2">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center">
            <span class="text-white font-bold text-lg">L</span>
          </div>
          <span class="text-xl font-bold gradient-text hidden sm:block">LightShop</span>
        </RouterLink>

        <!-- Search Bar -->
        <div class="hidden md:flex flex-1 max-w-xl mx-8">
          <form @submit.prevent="handleSearch" class="w-full relative">
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索商品..."
              class="glass-input pr-12"
            />
            <button 
              type="submit"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-white/50 hover:text-white transition-colors"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </button>
          </form>
        </div>

        <!-- Desktop Navigation -->
        <div class="hidden md:flex items-center space-x-4">
          <RouterLink to="/products" class="text-white/70 hover:text-white transition-colors px-3 py-2">
            全部商品
          </RouterLink>

          <RouterLink 
            to="/cart" 
            class="relative text-white/70 hover:text-white transition-colors px-3 py-2"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            <span 
              v-if="cartCount > 0"
              class="absolute -top-1 -right-1 w-5 h-5 bg-gradient-to-r from-pink-500 to-rose-500 rounded-full text-xs flex items-center justify-center text-white font-medium"
            >
              {{ cartCount > 99 ? '99+' : cartCount }}
            </span>
          </RouterLink>

          <!-- User Menu -->
          <div v-if="userStore.isLoggedIn" class="relative">
            <button 
              @click="showUserMenu = !showUserMenu"
              class="flex items-center space-x-2 glass-button"
            >
              <div class="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-400 to-purple-500 flex items-center justify-center">
                <span class="text-white text-sm font-medium">
                  {{ userStore.user?.username?.charAt(0).toUpperCase() }}
                </span>
              </div>
              <span class="text-white/90">{{ userStore.user?.username }}</span>
              <svg class="w-4 h-4 text-white/50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </button>

            <!-- Dropdown Menu -->
            <transition name="fade">
              <div 
                v-if="showUserMenu"
                class="absolute right-0 mt-2 w-48 glass rounded-xl py-2 shadow-xl"
                @mouseleave="showUserMenu = false"
              >
                <RouterLink 
                  to="/user" 
                  class="block px-4 py-2 text-white/70 hover:text-white hover:bg-white/10 transition-colors"
                  @click="showUserMenu = false"
                >
                  个人中心
                </RouterLink>
                <RouterLink 
                  to="/orders" 
                  class="block px-4 py-2 text-white/70 hover:text-white hover:bg-white/10 transition-colors"
                  @click="showUserMenu = false"
                >
                  我的订单
                </RouterLink>
                <RouterLink 
                  to="/favorites" 
                  class="block px-4 py-2 text-white/70 hover:text-white hover:bg-white/10 transition-colors"
                  @click="showUserMenu = false"
                >
                  我的收藏
                </RouterLink>
                <RouterLink 
                  to="/addresses" 
                  class="block px-4 py-2 text-white/70 hover:text-white hover:bg-white/10 transition-colors"
                  @click="showUserMenu = false"
                >
                  收货地址
                </RouterLink>
                <hr class="my-2 border-white/10" />
                <button 
                  @click="handleLogout"
                  class="w-full text-left px-4 py-2 text-rose-400 hover:bg-white/10 transition-colors"
                >
                  退出登录
                </button>
              </div>
            </transition>
          </div>

          <div v-else class="flex items-center space-x-3">
            <RouterLink to="/login" class="glass-button">
              登录
            </RouterLink>
            <RouterLink to="/register" class="glass-button-primary">
              注册
            </RouterLink>
          </div>
        </div>

        <!-- Mobile Menu Button -->
        <button 
          @click="showMobileMenu = !showMobileMenu"
          class="md:hidden text-white/70 hover:text-white p-2"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path v-if="!showMobileMenu" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>

    <!-- Mobile Menu -->
    <transition name="slide">
      <div v-if="showMobileMenu" class="md:hidden glass border-t border-white/10">
        <div class="px-4 py-4 space-y-3">
          <!-- Mobile Search -->
          <form @submit.prevent="handleSearch" class="relative">
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索商品..."
              class="glass-input pr-12"
            />
            <button type="submit" class="absolute right-3 top-1/2 -translate-y-1/2 text-white/50">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </button>
          </form>

          <RouterLink to="/products" class="block px-4 py-2 text-white/70 hover:text-white" @click="showMobileMenu = false">
            全部商品
          </RouterLink>
          <RouterLink to="/cart" class="block px-4 py-2 text-white/70 hover:text-white" @click="showMobileMenu = false">
            购物车 <span v-if="cartCount > 0" class="text-pink-400">({{ cartCount }})</span>
          </RouterLink>

          <template v-if="userStore.isLoggedIn">
            <RouterLink to="/user" class="block px-4 py-2 text-white/70 hover:text-white" @click="showMobileMenu = false">
              个人中心
            </RouterLink>
            <RouterLink to="/orders" class="block px-4 py-2 text-white/70 hover:text-white" @click="showMobileMenu = false">
              我的订单
            </RouterLink>
            <button @click="handleLogout" class="block w-full text-left px-4 py-2 text-rose-400">
              退出登录
            </button>
          </template>
          <template v-else>
            <RouterLink to="/login" class="block px-4 py-2 text-white/70 hover:text-white" @click="showMobileMenu = false">
              登录
            </RouterLink>
            <RouterLink to="/register" class="block px-4 py-2 text-indigo-400 hover:text-indigo-300" @click="showMobileMenu = false">
              注册
            </RouterLink>
          </template>
        </div>
      </div>
    </transition>
  </nav>

  <!-- Spacer -->
  <div class="h-16"></div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>

