<script setup>
import { ref, onMounted, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { orderApi } from '@/api'
import { useToastStore } from '@/stores/toast'
import Loading from '@/components/Loading.vue'
import Empty from '@/components/Empty.vue'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()

const loading = ref(true)
const orders = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const status = ref(null)

const tabs = [
  { value: null, label: '全部' },
  { value: 0, label: '待付款' },
  { value: 1, label: '待发货' },
  { value: 2, label: '待收货' },
  { value: 4, label: '已完成' }
]

const statusText = {
  0: '待付款',
  1: '待发货',
  2: '已发货',
  3: '已送达',
  4: '已完成',
  5: '已取消',
  6: '退款中',
  7: '已退款'
}

const statusClass = {
  0: 'bg-amber-500/20 text-amber-400',
  1: 'bg-blue-500/20 text-blue-400',
  2: 'bg-indigo-500/20 text-indigo-400',
  3: 'bg-purple-500/20 text-purple-400',
  4: 'bg-emerald-500/20 text-emerald-400',
  5: 'bg-white/10 text-white/50',
  6: 'bg-orange-500/20 text-orange-400',
  7: 'bg-rose-500/20 text-rose-400'
}

onMounted(() => {
  if (route.query.status !== undefined) {
    status.value = parseInt(route.query.status)
  }
  loadOrders()
})

watch(status, () => {
  page.value = 1
  router.replace({ query: status.value !== null ? { status: status.value } : {} })
  loadOrders()
})

async function loadOrders() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (status.value !== null) {
      params.status = status.value
    }
    
    const res = await orderApi.getList(params)
    orders.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('Failed to load orders:', error)
  } finally {
    loading.value = false
  }
}

async function payOrder(order) {
  try {
    await orderApi.pay(order.orderNo)
    toast.success('支付成功')
    loadOrders()
  } catch {
    // Error handled by interceptor
  }
}

async function cancelOrder(order) {
  if (!confirm('确定要取消这个订单吗？')) return
  
  try {
    await orderApi.cancel(order.orderNo)
    toast.success('订单已取消')
    loadOrders()
  } catch {
    // Error handled by interceptor
  }
}

async function confirmReceive(order) {
  try {
    await orderApi.confirm(order.orderNo)
    toast.success('确认收货成功')
    loadOrders()
  } catch {
    // Error handled by interceptor
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-2xl font-bold text-white mb-8">我的订单</h1>

    <!-- Tabs -->
    <div class="glass rounded-xl p-1 mb-6 flex overflow-x-auto">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        @click="status = tab.value"
        :class="[
          'px-4 py-2 rounded-lg text-sm font-medium transition-all whitespace-nowrap',
          status === tab.value
            ? 'bg-indigo-500/30 text-white'
            : 'text-white/60 hover:text-white hover:bg-white/10'
        ]"
      >
        {{ tab.label }}
      </button>
    </div>

    <Loading v-if="loading" text="加载订单..." />

    <Empty 
      v-else-if="orders.length === 0"
      icon="order"
      title="暂无订单"
      description="快去购物吧"
    >
      <RouterLink to="/products" class="glass-button-primary mt-4">
        去购物
      </RouterLink>
    </Empty>

    <div v-else class="space-y-4">
      <div
        v-for="order in orders"
        :key="order.id"
        class="glass-card"
      >
        <!-- Order Header -->
        <div class="flex items-center justify-between mb-4 pb-4 border-b border-white/10">
          <div class="flex items-center space-x-4">
            <span class="text-white/50 text-sm">订单号: {{ order.orderNo }}</span>
            <span class="text-white/50 text-sm">{{ order.createdAt }}</span>
          </div>
          <span :class="['px-3 py-1 rounded-lg text-xs font-medium', statusClass[order.status]]">
            {{ statusText[order.status] }}
          </span>
        </div>

        <!-- Order Items -->
        <RouterLink :to="`/order/${order.orderNo}`" class="block">
          <div class="space-y-3">
            <div
              v-for="item in order.items?.slice(0, 2)"
              :key="item.id"
              class="flex items-center space-x-4"
            >
              <img
                :src="item.productImage || 'https://picsum.photos/60/60'"
                :alt="item.productName"
                class="w-14 h-14 rounded-lg object-cover"
              />
              <div class="flex-1 min-w-0">
                <h3 class="text-white text-sm line-clamp-1">{{ item.productName }}</h3>
                <p class="text-white/50 text-xs">¥{{ item.price.toFixed(2) }} × {{ item.quantity }}</p>
              </div>
            </div>
            <p v-if="order.items?.length > 2" class="text-white/50 text-sm">
              等共 {{ order.items.length }} 件商品
            </p>
          </div>
        </RouterLink>

        <!-- Order Footer -->
        <div class="flex items-center justify-between mt-4 pt-4 border-t border-white/10">
          <div>
            <span class="text-white/60 text-sm">实付金额：</span>
            <span class="text-lg font-bold text-transparent bg-clip-text bg-gradient-to-r from-pink-400 to-purple-400">
              ¥{{ order.totalAmount.toFixed(2) }}
            </span>
          </div>

          <div class="flex items-center space-x-3">
            <RouterLink
              :to="`/order/${order.orderNo}`"
              class="glass-button text-sm"
            >
              查看详情
            </RouterLink>
            
            <button
              v-if="order.status === 0"
              @click.prevent="payOrder(order)"
              class="glass-button-primary text-sm"
            >
              立即支付
            </button>
            
            <button
              v-if="order.status === 0"
              @click.prevent="cancelOrder(order)"
              class="text-white/50 hover:text-rose-400 text-sm transition-colors"
            >
              取消订单
            </button>
            
            <button
              v-if="order.status === 2"
              @click.prevent="confirmReceive(order)"
              class="glass-button-primary text-sm"
            >
              确认收货
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

