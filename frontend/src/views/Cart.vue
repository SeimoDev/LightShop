<script setup>
import { onMounted, computed } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useToastStore } from '@/stores/toast'
import Loading from '@/components/Loading.vue'
import Empty from '@/components/Empty.vue'

const router = useRouter()
const cartStore = useCartStore()
const toast = useToastStore()

onMounted(() => {
  cartStore.fetchCart()
})

const canCheckout = computed(() => cartStore.selectedItems.length > 0)

async function updateQuantity(item, delta) {
  const newQuantity = item.quantity + delta
  if (newQuantity < 1) {
    await cartStore.removeItem(item.id)
    toast.success('已从购物车移除')
  } else if (newQuantity <= item.productStock) {
    await cartStore.updateQuantity(item.id, newQuantity)
  } else {
    toast.warning('超出库存数量')
  }
}

async function removeItem(item) {
  await cartStore.removeItem(item.id)
  toast.success('已从购物车移除')
}

function checkout() {
  if (!canCheckout.value) {
    toast.warning('请选择要结算的商品')
    return
  }

  sessionStorage.setItem('checkout', JSON.stringify({ fromCart: true }))
  router.push('/checkout')
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-2xl font-bold text-white mb-8">购物车</h1>

    <Loading v-if="cartStore.loading" text="加载购物车..." />

    <Empty 
      v-else-if="cartStore.items.length === 0"
      icon="cart"
      title="购物车是空的"
      description="快去挑选心仪的商品吧"
    >
      <RouterLink to="/products" class="glass-button-primary mt-4">
        去购物
      </RouterLink>
    </Empty>

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Cart Items -->
      <div class="lg:col-span-2 space-y-4">
        <!-- Select All -->
        <div class="glass-card flex items-center justify-between">
          <label class="flex items-center space-x-3 cursor-pointer">
            <input
              type="checkbox"
              :checked="cartStore.allSelected"
              @change="cartStore.toggleSelectAll($event.target.checked)"
              class="w-5 h-5 rounded border-white/20 bg-white/10 text-indigo-500 focus:ring-indigo-500 focus:ring-offset-0"
            />
            <span class="text-white">全选</span>
          </label>
          <button
            v-if="cartStore.selectedItems.length > 0"
            @click="cartStore.removeSelected"
            class="text-rose-400 hover:text-rose-300 text-sm"
          >
            删除选中
          </button>
        </div>

        <!-- Cart Item List -->
        <div
          v-for="item in cartStore.items"
          :key="item.id"
          class="glass-card"
        >
          <div class="flex items-center space-x-4">
            <!-- Checkbox -->
            <input
              type="checkbox"
              :checked="item.selected"
              @change="cartStore.toggleSelected(item.id, $event.target.checked)"
              class="w-5 h-5 rounded border-white/20 bg-white/10 text-indigo-500 focus:ring-indigo-500 focus:ring-offset-0"
            />

            <!-- Product Image -->
            <RouterLink :to="`/product/${item.productId}`" class="shrink-0">
              <img
                :src="item.productImage || 'https://picsum.photos/100/100'"
                :alt="item.productName"
                class="w-20 h-20 rounded-xl object-cover"
              />
            </RouterLink>

            <!-- Product Info -->
            <div class="flex-1 min-w-0">
              <RouterLink :to="`/product/${item.productId}`">
                <h3 class="text-white font-medium line-clamp-2 hover:text-indigo-300 transition-colors">
                  {{ item.productName }}
                </h3>
              </RouterLink>
              <p class="text-xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-pink-400 to-purple-400 mt-1">
                ¥{{ item.productPrice.toFixed(2) }}
              </p>
            </div>

            <!-- Quantity Controls -->
            <div class="flex items-center space-x-2">
              <button
                @click="updateQuantity(item, -1)"
                class="w-8 h-8 rounded-lg glass flex items-center justify-center text-white/70 hover:text-white"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
                </svg>
              </button>
              <span class="w-10 text-center text-white">{{ item.quantity }}</span>
              <button
                @click="updateQuantity(item, 1)"
                :disabled="item.quantity >= item.productStock"
                class="w-8 h-8 rounded-lg glass flex items-center justify-center text-white/70 hover:text-white disabled:opacity-30"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
              </button>
            </div>

            <!-- Subtotal -->
            <div class="text-right hidden sm:block">
              <p class="text-white/50 text-sm">小计</p>
              <p class="text-white font-bold">¥{{ (item.productPrice * item.quantity).toFixed(2) }}</p>
            </div>

            <!-- Remove Button -->
            <button
              @click="removeItem(item)"
              class="p-2 text-white/40 hover:text-rose-400 transition-colors"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- Order Summary -->
      <div class="lg:col-span-1">
        <div class="glass-card sticky top-24">
          <h2 class="text-lg font-bold text-white mb-4">订单摘要</h2>

          <div class="space-y-3 text-sm">
            <div class="flex justify-between text-white/70">
              <span>已选商品</span>
              <span>{{ cartStore.selectedItems.length }} 件</span>
            </div>
            <div class="flex justify-between text-white/70">
              <span>商品金额</span>
              <span>¥{{ cartStore.totalAmount.toFixed(2) }}</span>
            </div>
            <div class="flex justify-between text-white/70">
              <span>运费</span>
              <span class="text-emerald-400">免运费</span>
            </div>
          </div>

          <hr class="my-4 border-white/10" />

          <div class="flex justify-between items-center mb-6">
            <span class="text-white font-medium">合计</span>
            <span class="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-pink-400 to-purple-400">
              ¥{{ cartStore.totalAmount.toFixed(2) }}
            </span>
          </div>

          <button
            @click="checkout"
            :disabled="!canCheckout"
            class="w-full glass-button-primary py-4 disabled:opacity-50"
          >
            去结算 ({{ cartStore.selectedItems.length }})
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

