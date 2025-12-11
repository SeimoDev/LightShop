<script setup>
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import Toast from '@/components/Toast.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 应用启动时刷新用户信息（包括余额）
onMounted(() => {
  if (userStore.isLoggedIn) {
    userStore.fetchProfile()
  }
})
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <!-- Background Decorations -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <div class="absolute top-20 left-10 w-72 h-72 bg-purple-500/20 rounded-full blur-3xl animate-pulse-slow"></div>
      <div class="absolute top-40 right-20 w-96 h-96 bg-pink-500/20 rounded-full blur-3xl animate-pulse-slow delay-1000"></div>
      <div class="absolute bottom-20 left-1/3 w-80 h-80 bg-indigo-500/20 rounded-full blur-3xl animate-pulse-slow delay-2000"></div>
    </div>

    <Navbar />
    
    <main class="flex-1 relative z-10">
      <RouterView v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </RouterView>
    </main>

    <Footer />
    <Toast />
  </div>
</template>

