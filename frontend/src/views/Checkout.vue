<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { addressApi, orderApi, productApi } from '@/api'
import { useCartStore } from '@/stores/cart'
import { useToastStore } from '@/stores/toast'
import Loading from '@/components/Loading.vue'

const router = useRouter()
const cartStore = useCartStore()
const toast = useToastStore()

const loading = ref(true)
const submitting = ref(false)
const addresses = ref([])
const selectedAddressId = ref(null)
const remark = ref('')
const orderItems = ref([])
const fromCart = ref(true)

const selectedAddress = computed(() => 
  addresses.value.find(a => a.id === selectedAddressId.value)
)

const totalAmount = computed(() => 
  orderItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
)

onMounted(async () => {
  // Load checkout data from session
  const checkoutData = sessionStorage.getItem('checkout')
  if (!checkoutData) {
    toast.warning('请先选择商品')
    router.push('/cart')
    return
  }

  const data = JSON.parse(checkoutData)
  fromCart.value = data.fromCart

  try {
    // Load addresses
    const addressRes = await addressApi.getList()
    addresses.value = addressRes.data || []
    
    // Set default address
    const defaultAddr = addresses.value.find(a => a.isDefault)
    if (defaultAddr) {
      selectedAddressId.value = defaultAddr.id
    } else if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].id
    }

    // Load order items
    if (fromCart.value) {
      // From cart - use selected items
      await cartStore.fetchCart()
      orderItems.value = cartStore.selectedItems.map(item => ({
        productId: item.productId,
        productName: item.productName,
        productImage: item.productImage,
        price: item.productPrice,
        quantity: item.quantity
      }))
    } else {
      // Direct buy
      const items = data.items || []
      for (const item of items) {
        const res = await productApi.getDetail(item.productId)
        const product = res.data.product
        let productImage = product.images
        if (productImage && typeof productImage === 'string' && productImage.startsWith('[')) {
          productImage = JSON.parse(productImage)[0]
        }
        orderItems.value.push({
          productId: product.id,
          productName: product.name,
          productImage,
          price: product.price,
          quantity: item.quantity
        })
      }
    }

    if (orderItems.value.length === 0) {
      toast.warning('请选择商品')
      router.push('/cart')
    }
  } catch (error) {
    console.error('Failed to load checkout data:', error)
    toast.error('加载结算信息失败')
  } finally {
    loading.value = false
  }
})

async function submitOrder() {
  if (!selectedAddressId.value) {
    toast.warning('请选择收货地址')
    return
  }

  submitting.value = true
  try {
    const orderData = {
      addressId: selectedAddressId.value,
      remark: remark.value,
      fromCart: fromCart.value,
      items: orderItems.value.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      }))
    }

    const res = await orderApi.create(orderData)
    sessionStorage.removeItem('checkout')
    
    toast.success('订单创建成功')
    router.push(`/order/${res.data.orderNo}`)
  } catch (error) {
    // Error handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-2xl font-bold text-white mb-8">确认订单</h1>

    <Loading v-if="loading" text="加载中..." />

    <div v-else class="space-y-6">
      <!-- Address Selection -->
      <div class="glass-card">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-bold text-white">收货地址</h2>
          <RouterLink to="/addresses" class="text-indigo-400 hover:text-indigo-300 text-sm">
            管理地址
          </RouterLink>
        </div>

        <div v-if="addresses.length === 0" class="text-center py-6">
          <p class="text-white/60 mb-4">暂无收货地址</p>
          <RouterLink to="/addresses" class="glass-button-primary">
            添加地址
          </RouterLink>
        </div>

        <div v-else class="space-y-3">
          <label
            v-for="address in addresses"
            :key="address.id"
            class="block cursor-pointer"
          >
            <div
              :class="[
                'p-4 rounded-xl border transition-all',
                selectedAddressId === address.id
                  ? 'border-indigo-500 bg-indigo-500/10'
                  : 'border-white/10 hover:border-white/30'
              ]"
            >
              <div class="flex items-start space-x-3">
                <input
                  type="radio"
                  :value="address.id"
                  v-model="selectedAddressId"
                  class="mt-1 w-4 h-4 text-indigo-500 bg-white/10 border-white/20 focus:ring-indigo-500 focus:ring-offset-0"
                />
                <div class="flex-1">
                  <div class="flex items-center space-x-2 mb-1">
                    <span class="text-white font-medium">{{ address.receiverName }}</span>
                    <span class="text-white/60">{{ address.phone }}</span>
                    <span v-if="address.isDefault" class="px-2 py-0.5 bg-indigo-500/20 text-indigo-400 text-xs rounded">默认</span>
                  </div>
                  <p class="text-white/70 text-sm">
                    {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detailAddress }}
                  </p>
                </div>
              </div>
            </div>
          </label>
        </div>
      </div>

      <!-- Order Items -->
      <div class="glass-card">
        <h2 class="text-lg font-bold text-white mb-4">商品清单</h2>

        <div class="space-y-4">
          <div
            v-for="item in orderItems"
            :key="item.productId"
            class="flex items-center space-x-4"
          >
            <img
              :src="item.productImage || 'https://picsum.photos/80/80'"
              :alt="item.productName"
              class="w-16 h-16 rounded-xl object-cover"
            />
            <div class="flex-1 min-w-0">
              <h3 class="text-white line-clamp-1">{{ item.productName }}</h3>
              <p class="text-white/50 text-sm">¥{{ item.price.toFixed(2) }} × {{ item.quantity }}</p>
            </div>
            <p class="text-white font-medium">¥{{ (item.price * item.quantity).toFixed(2) }}</p>
          </div>
        </div>
      </div>

      <!-- Remark -->
      <div class="glass-card">
        <h2 class="text-lg font-bold text-white mb-4">订单备注</h2>
        <textarea
          v-model="remark"
          placeholder="选填，如有特殊要求请备注"
          rows="3"
          class="glass-input resize-none"
        ></textarea>
      </div>

      <!-- Order Summary -->
      <div class="glass-card">
        <div class="space-y-3 text-sm">
          <div class="flex justify-between text-white/70">
            <span>商品金额</span>
            <span>¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <div class="flex justify-between text-white/70">
            <span>运费</span>
            <span class="text-emerald-400">免运费</span>
          </div>
        </div>

        <hr class="my-4 border-white/10" />

        <div class="flex justify-between items-center mb-6">
          <span class="text-white">实付金额</span>
          <span class="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-pink-400 to-purple-400">
            ¥{{ totalAmount.toFixed(2) }}
          </span>
        </div>

        <button
          @click="submitOrder"
          :disabled="submitting || !selectedAddressId"
          class="w-full glass-button-primary py-4 flex items-center justify-center space-x-2 disabled:opacity-50"
        >
          <span v-if="submitting" class="spinner w-5 h-5"></span>
          <span>{{ submitting ? '提交中...' : '提交订单' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

