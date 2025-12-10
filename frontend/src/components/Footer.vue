<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <footer class="glass border-t border-white/10 mt-auto">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
        <!-- Brand -->
        <div class="md:col-span-1">
          <RouterLink to="/" class="flex items-center space-x-2 mb-4">
            <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center">
              <span class="text-white font-bold text-lg">L</span>
            </div>
            <span class="text-xl font-bold gradient-text">LightShop</span>
          </RouterLink>
          <p class="text-white/60 text-sm">
            优质商品，尽在LightShop。我们致力于为您提供最好的购物体验。
          </p>
        </div>

        <!-- Quick Links -->
        <div>
          <h3 class="text-white font-semibold mb-4">快速链接</h3>
          <ul class="space-y-2">
            <li>
              <RouterLink to="/products" class="text-white/60 hover:text-white transition-colors text-sm">
                全部商品
              </RouterLink>
            </li>
            <li>
              <RouterLink to="/products?sort=newest" class="text-white/60 hover:text-white transition-colors text-sm">
                新品上架
              </RouterLink>
            </li>
            <li>
              <RouterLink to="/products?sort=sales" class="text-white/60 hover:text-white transition-colors text-sm">
                热销商品
              </RouterLink>
            </li>
          </ul>
        </div>

        <!-- Customer Service -->
        <div>
          <h3 class="text-white font-semibold mb-4">客户服务</h3>
          <ul class="space-y-2">
            <li>
              <RouterLink to="/orders" class="text-white/60 hover:text-white transition-colors text-sm">
                订单查询
              </RouterLink>
            </li>
            <li>
              <a href="#" class="text-white/60 hover:text-white transition-colors text-sm">
                售后服务
              </a>
            </li>
            <li>
              <a href="#" class="text-white/60 hover:text-white transition-colors text-sm">
                配送说明
              </a>
            </li>
          </ul>
        </div>

        <!-- Contact -->
        <div>
          <h3 class="text-white font-semibold mb-4">联系我们</h3>
          <ul class="space-y-2 text-sm text-white/60">
            <li class="flex items-center space-x-2">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
              </svg>
              <span>400-123-4567</span>
            </li>
            <li class="flex items-center space-x-2">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
              <span>support@lightshop.com</span>
            </li>
          </ul>
        </div>
      </div>

      <!-- Bottom Bar -->
      <div class="mt-8 pt-8 border-t border-white/10 flex flex-col md:flex-row justify-between items-center">
        <p class="text-white/40 text-sm">
          © 2024 LightShop. All rights reserved.
        </p>
        <div class="flex items-center space-x-6 mt-4 md:mt-0">
          <a href="#" class="text-white/40 hover:text-white transition-colors text-sm">隐私政策</a>
          <a href="#" class="text-white/40 hover:text-white transition-colors text-sm">服务条款</a>
        </div>
      </div>
    </div>
  </footer>
</template>

