<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { dashboardApi } from '@/api'

const loading = ref(true)
const stats = ref({
  totalUsers: 0,
  totalProducts: 0,
  totalOrders: 0,
  totalSales: 0,
  orderStats: {},
  recentOrders: [],
  hotProducts: []
})

const statusText = {
  0: '待付款',
  1: '待发货',
  2: '已发货',
  4: '已完成',
  5: '已取消'
}

onMounted(async () => {
  try {
    const res = await dashboardApi.getStats()
    stats.value = res.data
  } catch (error) {
    console.error('Failed to load dashboard:', error)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold text-white mb-8">仪表盘</h1>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="stat-card">
        <div class="stat-icon bg-gradient-to-br from-blue-400 to-blue-600">
          <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
          </svg>
        </div>
        <div>
          <p class="text-slate-400 text-sm">用户总数</p>
          <p class="text-2xl font-bold text-white">{{ stats.totalUsers }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon bg-gradient-to-br from-emerald-400 to-emerald-600">
          <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
          </svg>
        </div>
        <div>
          <p class="text-slate-400 text-sm">商品总数</p>
          <p class="text-2xl font-bold text-white">{{ stats.totalProducts }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon bg-gradient-to-br from-purple-400 to-purple-600">
          <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        </div>
        <div>
          <p class="text-slate-400 text-sm">订单总数</p>
          <p class="text-2xl font-bold text-white">{{ stats.totalOrders }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon bg-gradient-to-br from-amber-400 to-amber-600">
          <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div>
          <p class="text-slate-400 text-sm">销售总额</p>
          <p class="text-2xl font-bold text-white">¥{{ stats.totalSales?.toFixed(2) }}</p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Order Stats -->
      <div class="card">
        <h2 class="text-lg font-bold text-white mb-4">订单状态</h2>
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <span class="text-slate-400">待付款</span>
            <span class="badge badge-warning">{{ stats.orderStats?.pending || 0 }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-slate-400">待发货</span>
            <span class="badge badge-info">{{ stats.orderStats?.paid || 0 }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-slate-400">已发货</span>
            <span class="badge badge-info">{{ stats.orderStats?.shipped || 0 }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-slate-400">已完成</span>
            <span class="badge badge-success">{{ stats.orderStats?.completed || 0 }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-slate-400">已取消</span>
            <span class="badge badge-danger">{{ stats.orderStats?.cancelled || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- Recent Orders -->
      <div class="card">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-bold text-white">最近订单</h2>
          <RouterLink to="/orders" class="text-indigo-400 hover:text-indigo-300 text-sm">
            查看全部
          </RouterLink>
        </div>
        
        <div v-if="stats.recentOrders?.length === 0" class="text-center py-8 text-slate-500">
          暂无订单
        </div>

        <div v-else class="space-y-3">
          <div
            v-for="order in stats.recentOrders"
            :key="order.id"
            class="flex items-center justify-between py-3 border-b border-slate-700/50 last:border-0"
          >
            <div>
              <p class="text-white font-medium">{{ order.orderNo }}</p>
              <p class="text-slate-500 text-sm">{{ order.username }}</p>
            </div>
            <div class="text-right">
              <p class="text-white">¥{{ order.totalAmount?.toFixed(2) }}</p>
              <p class="text-slate-500 text-xs">{{ statusText[order.status] }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Hot Products -->
    <div class="card mt-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-bold text-white">热销商品</h2>
        <RouterLink to="/products" class="text-indigo-400 hover:text-indigo-300 text-sm">
          查看全部
        </RouterLink>
      </div>

      <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
        <div
          v-for="product in stats.hotProducts"
          :key="product.id"
          class="bg-slate-800/50 rounded-xl p-4"
        >
          <div class="aspect-square rounded-lg overflow-hidden mb-3">
            <img
              :src="product.images ? (JSON.parse(product.images)[0] || 'https://picsum.photos/200') : 'https://picsum.photos/200'"
              :alt="product.name"
              class="w-full h-full object-cover"
            />
          </div>
          <h3 class="text-white text-sm font-medium line-clamp-1">{{ product.name }}</h3>
          <div class="flex items-center justify-between mt-2">
            <span class="text-indigo-400 font-medium">¥{{ product.price }}</span>
            <span class="text-slate-500 text-xs">销量 {{ product.sales }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

