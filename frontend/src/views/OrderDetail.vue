<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { orderApi } from '@/api'
import { useToastStore } from '@/stores/toast'
import Loading from '@/components/Loading.vue'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()

const loading = ref(true)
const order = ref(null)

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
  0: 'from-amber-400 to-orange-500',
  1: 'from-blue-400 to-indigo-500',
  2: 'from-indigo-400 to-purple-500',
  3: 'from-purple-400 to-pink-500',
  4: 'from-emerald-400 to-green-500',
  5: 'from-gray-400 to-gray-500',
  6: 'from-orange-400 to-red-500',
  7: 'from-rose-400 to-pink-500'
}

onMounted(async () => {
  await loadOrder()
})

async function loadOrder() {
  loading.value = true
  try {
    const res = await orderApi.getDetail(route.params.orderNo)
    order.value = res.data
  } catch (error) {
    toast.error('订单不存在')
    router.push('/orders')
  } finally {
    loading.value = false
  }
}

async function payOrder() {
  try {
    await orderApi.pay(order.value.orderNo)
    toast.success('支付成功')
    loadOrder()
  } catch {
    // Error handled by interceptor
  }
}

async function cancelOrder() {
  if (!confirm('确定要取消这个订单吗？')) return
  
  try {
    await orderApi.cancel(order.value.orderNo)
    toast.success('订单已取消')
    loadOrder()
  } catch {
    // Error handled by interceptor
  }
}

async function confirmReceive() {
  try {
    await orderApi.confirm(order.value.orderNo)
    toast.success('确认收货成功')
    loadOrder()
  } catch {
    // Error handled by interceptor
  }
}

function getAddress() {
  if (!order.value?.addressSnapshot) return null
  try {
    return JSON.parse(order.value.addressSnapshot)
  } catch {
    return null
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <Loading v-if="loading" text="加载订单详情..." />

    <div v-else-if="order">
      <!-- Status Banner -->
      <div :class="['glass-card mb-6 bg-gradient-to-r', statusClass[order.status]]">
        <div class="flex items-center justify-between">
          <div>
            <h2 class="text-xl font-bold text-white">{{ statusText[order.status] }}</h2>
            <p class="text-white/70 text-sm mt-1">订单号：{{ order.orderNo }}</p>
          </div>
          <div class="text-right">
            <p class="text-2xl font-bold text-white">¥{{ order.totalAmount.toFixed(2) }}</p>
          </div>
        </div>
      </div>

      <!-- Address -->
      <div class="glass-card mb-6">
        <h3 class="text-lg font-bold text-white mb-4">收货信息</h3>
        <div v-if="getAddress()" class="space-y-2">
          <div class="flex items-center space-x-3">
            <span class="text-white font-medium">{{ getAddress().receiverName }}</span>
            <span class="text-white/60">{{ getAddress().phone }}</span>
          </div>
          <p class="text-white/70">
            {{ getAddress().province }}{{ getAddress().city }}{{ getAddress().district }}{{ getAddress().detailAddress }}
          </p>
        </div>
      </div>

      <!-- Order Items -->
      <div class="glass-card mb-6">
        <h3 class="text-lg font-bold text-white mb-4">商品信息</h3>
        <div class="space-y-4">
          <div
            v-for="item in order.items"
            :key="item.id"
            class="flex items-center space-x-4"
          >
            <RouterLink :to="`/product/${item.productId}`">
              <img
                :src="item.productImage || 'https://picsum.photos/80/80'"
                :alt="item.productName"
                class="w-16 h-16 rounded-xl object-cover"
              />
            </RouterLink>
            <div class="flex-1 min-w-0">
              <RouterLink :to="`/product/${item.productId}`">
                <h4 class="text-white hover:text-indigo-300 transition-colors">{{ item.productName }}</h4>
              </RouterLink>
              <p class="text-white/50 text-sm">¥{{ item.price.toFixed(2) }} × {{ item.quantity }}</p>
            </div>
            <p class="text-white font-medium">¥{{ (item.price * item.quantity).toFixed(2) }}</p>
          </div>
        </div>
      </div>

      <!-- Order Info -->
      <div class="glass-card mb-6">
        <h3 class="text-lg font-bold text-white mb-4">订单信息</h3>
        <div class="space-y-3 text-sm">
          <div class="flex justify-between">
            <span class="text-white/60">下单时间</span>
            <span class="text-white">{{ order.createdAt }}</span>
          </div>
          <div v-if="order.paidAt" class="flex justify-between">
            <span class="text-white/60">支付时间</span>
            <span class="text-white">{{ order.paidAt }}</span>
          </div>
          <div v-if="order.shippedAt" class="flex justify-between">
            <span class="text-white/60">发货时间</span>
            <span class="text-white">{{ order.shippedAt }}</span>
          </div>
          <div v-if="order.completedAt" class="flex justify-between">
            <span class="text-white/60">完成时间</span>
            <span class="text-white">{{ order.completedAt }}</span>
          </div>
          <div v-if="order.remark" class="flex justify-between">
            <span class="text-white/60">订单备注</span>
            <span class="text-white">{{ order.remark }}</span>
          </div>
        </div>
      </div>

      <!-- Price Summary -->
      <div class="glass-card mb-6">
        <div class="space-y-3 text-sm">
          <div class="flex justify-between text-white/70">
            <span>商品金额</span>
            <span>¥{{ order.totalAmount.toFixed(2) }}</span>
          </div>
          <div class="flex justify-between text-white/70">
            <span>运费</span>
            <span class="text-emerald-400">免运费</span>
          </div>
        </div>
        <hr class="my-4 border-white/10" />
        <div class="flex justify-between items-center">
          <span class="text-white font-medium">实付金额</span>
          <span class="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-pink-400 to-purple-400">
            ¥{{ order.totalAmount.toFixed(2) }}
          </span>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex items-center justify-end space-x-4">
        <RouterLink to="/orders" class="glass-button">
          返回订单列表
        </RouterLink>
        
        <button
          v-if="order.status === 0"
          @click="cancelOrder"
          class="text-white/50 hover:text-rose-400 px-4 py-2 transition-colors"
        >
          取消订单
        </button>
        
        <button
          v-if="order.status === 0"
          @click="payOrder"
          class="glass-button-primary"
        >
          立即支付
        </button>
        
        <button
          v-if="order.status === 2"
          @click="confirmReceive"
          class="glass-button-primary"
        >
          确认收货
        </button>
      </div>
    </div>
  </div>
</template>

