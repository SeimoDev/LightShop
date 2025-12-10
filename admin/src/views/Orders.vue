<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api'

const loading = ref(true)
const orders = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const statusFilter = ref('')
const showModal = ref(false)
const currentOrder = ref(null)

const statusOptions = [
  { value: '', label: '全部' },
  { value: '0', label: '待付款' },
  { value: '1', label: '待发货' },
  { value: '2', label: '已发货' },
  { value: '4', label: '已完成' },
  { value: '5', label: '已取消' }
]

const statusText = {
  0: '待付款',
  1: '待发货',
  2: '已发货',
  3: '已收货',
  4: '已完成',
  5: '已取消'
}

const statusClass = {
  0: 'badge-warning',
  1: 'badge-info',
  2: 'badge-info',
  3: 'badge-success',
  4: 'badge-success',
  5: 'badge-danger'
}

onMounted(async () => {
  await loadOrders()
})

async function loadOrders() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (statusFilter.value !== '') params.status = statusFilter.value

    const res = await orderApi.getList(params)
    orders.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function filterByStatus() {
  page.value = 1
  loadOrders()
}

async function viewDetail(order) {
  try {
    const res = await orderApi.getDetail(order.orderNo)
    currentOrder.value = res.data
    showModal.value = true
  } catch (e) {
    console.error(e)
    alert('获取订单详情失败')
  }
}

async function shipOrder(orderNo) {
  if (!confirm('确定要发货吗？')) return
  try {
    await orderApi.ship(orderNo)
    alert('发货成功')
    showModal.value = false
    loadOrders()
  } catch (e) {
    console.error(e)
    alert('发货失败')
  }
}

async function cancelOrder(orderNo) {
  if (!confirm('确定要取消此订单吗？')) return
  try {
    await orderApi.cancel(orderNo)
    alert('取消成功')
    showModal.value = false
    loadOrders()
  } catch (e) {
    console.error(e)
    alert('取消失败')
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-8">
      <h1 class="text-2xl font-bold text-white">订单管理</h1>
    </div>

    <!-- Filters -->
    <div class="card mb-6">
      <div class="flex flex-wrap gap-4">
        <select v-model="statusFilter" @change="filterByStatus" class="select w-48">
          <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>
    </div>

    <!-- Orders Table -->
    <div class="card overflow-x-auto">
      <table class="table">
        <thead>
          <tr>
            <th>订单号</th>
            <th>用户</th>
            <th>金额</th>
            <th>状态</th>
            <th>下单时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="text-center py-8 text-slate-500">加载中...</td>
          </tr>
          <tr v-else-if="orders.length === 0">
            <td colspan="6" class="text-center py-8 text-slate-500">暂无订单</td>
          </tr>
          <tr v-else v-for="order in orders" :key="order.id">
            <td class="text-white font-mono">{{ order.orderNo }}</td>
            <td class="text-slate-300">{{ order.username || `用户${order.userId}` }}</td>
            <td class="text-indigo-400 font-medium">¥{{ order.totalAmount?.toFixed(2) }}</td>
            <td>
              <span :class="['badge', statusClass[order.status]]">
                {{ statusText[order.status] }}
              </span>
            </td>
            <td class="text-slate-400 text-sm">{{ formatDate(order.createdAt) }}</td>
            <td>
              <button @click="viewDetail(order)" class="text-indigo-400 hover:text-indigo-300">
                详情
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div v-if="total > pageSize" class="flex items-center justify-between mt-6 pt-6 border-t border-slate-700">
        <p class="text-slate-500 text-sm">共 {{ total }} 条</p>
        <div class="flex space-x-2">
          <button
            @click="page--; loadOrders()"
            :disabled="page === 1"
            class="btn-secondary px-3 py-1 text-sm disabled:opacity-50"
          >
            上一页
          </button>
          <button
            @click="page++; loadOrders()"
            :disabled="page >= Math.ceil(total / pageSize)"
            class="btn-secondary px-3 py-1 text-sm disabled:opacity-50"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- Order Detail Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-slate-800 rounded-2xl p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-white">订单详情</h2>
          <button @click="showModal = false" class="text-slate-400 hover:text-white">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div v-if="currentOrder" class="space-y-6">
          <!-- Order Info -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <p class="text-slate-500 text-sm">订单号</p>
              <p class="text-white font-mono">{{ currentOrder.orderNo }}</p>
            </div>
            <div>
              <p class="text-slate-500 text-sm">状态</p>
              <span :class="['badge', statusClass[currentOrder.status]]">
                {{ statusText[currentOrder.status] }}
              </span>
            </div>
            <div>
              <p class="text-slate-500 text-sm">下单时间</p>
              <p class="text-white">{{ formatDate(currentOrder.createdAt) }}</p>
            </div>
            <div>
              <p class="text-slate-500 text-sm">付款时间</p>
              <p class="text-white">{{ formatDate(currentOrder.paidAt) }}</p>
            </div>
          </div>

          <!-- Address -->
          <div v-if="currentOrder.addressSnapshot" class="bg-slate-700/50 rounded-xl p-4">
            <p class="text-slate-400 text-sm mb-2">收货地址</p>
            <p class="text-white">
              {{ JSON.parse(currentOrder.addressSnapshot).receiverName }}
              {{ JSON.parse(currentOrder.addressSnapshot).phone }}
            </p>
            <p class="text-slate-300">
              {{ JSON.parse(currentOrder.addressSnapshot).province }}
              {{ JSON.parse(currentOrder.addressSnapshot).city }}
              {{ JSON.parse(currentOrder.addressSnapshot).district }}
              {{ JSON.parse(currentOrder.addressSnapshot).detailAddress }}
            </p>
          </div>

          <!-- Order Items -->
          <div>
            <p class="text-slate-400 text-sm mb-3">商品信息</p>
            <div class="space-y-3">
              <div
                v-for="item in currentOrder.items"
                :key="item.id"
                class="flex items-center space-x-4 bg-slate-700/50 rounded-xl p-3"
              >
                <img 
                  :src="item.productImage || 'https://picsum.photos/60'" 
                  class="w-16 h-16 rounded-lg object-cover"
                />
                <div class="flex-1">
                  <p class="text-white font-medium">{{ item.productName }}</p>
                  <p class="text-slate-400 text-sm">¥{{ item.price }} × {{ item.quantity }}</p>
                </div>
                <p class="text-indigo-400 font-medium">¥{{ (item.price * item.quantity).toFixed(2) }}</p>
              </div>
            </div>
          </div>

          <!-- Total -->
          <div class="flex justify-end">
            <div class="text-right">
              <p class="text-slate-400 text-sm">订单总额</p>
              <p class="text-2xl font-bold text-indigo-400">¥{{ currentOrder.totalAmount?.toFixed(2) }}</p>
            </div>
          </div>

          <!-- Remark -->
          <div v-if="currentOrder.remark">
            <p class="text-slate-400 text-sm mb-1">买家备注</p>
            <p class="text-white bg-slate-700/50 rounded-lg p-3">{{ currentOrder.remark }}</p>
          </div>

          <!-- Actions -->
          <div class="flex items-center justify-end space-x-4 pt-4 border-t border-slate-700">
            <button @click="showModal = false" class="btn-secondary">关闭</button>
            <button
              v-if="currentOrder.status === 1"
              @click="shipOrder(currentOrder.orderNo)"
              class="btn-primary"
            >
              发货
            </button>
            <button
              v-if="currentOrder.status === 0"
              @click="cancelOrder(currentOrder.orderNo)"
              class="bg-rose-500 hover:bg-rose-600 text-white px-4 py-2 rounded-lg transition-colors"
            >
              取消订单
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

