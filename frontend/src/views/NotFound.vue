<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <div class="min-h-[70vh] flex items-center justify-center px-4">
    <div class="text-center">
      <div class="relative mb-8">
        <h1 class="text-[150px] md:text-[200px] font-bold text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 via-purple-400 to-pink-400 leading-none">
          404
        </h1>
        <div class="absolute inset-0 blur-3xl bg-gradient-to-r from-indigo-500/20 via-purple-500/20 to-pink-500/20"></div>
      </div>

      <h2 class="text-2xl md:text-3xl font-bold text-white mb-4">
        页面走丢了
      </h2>
      <p class="text-white/60 mb-8 max-w-md mx-auto">
        您访问的页面不存在，可能已被删除或者链接有误
      </p>

      <div class="flex flex-col sm:flex-row items-center justify-center space-y-4 sm:space-y-0 sm:space-x-4">
        <RouterLink to="/" class="glass-button-primary px-8">
          返回首页
        </RouterLink>
        <RouterLink to="/products" class="glass-button px-8">
          去购物
        </RouterLink>
      </div>
    </div>
  </div>
</template>

