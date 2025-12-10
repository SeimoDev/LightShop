import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartApi } from '@/api'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loading = ref(false)

  const totalQuantity = computed(() => 
    items.value.reduce((sum, item) => sum + item.quantity, 0)
  )

  const selectedItems = computed(() => 
    items.value.filter(item => item.selected)
  )

  const totalAmount = computed(() => 
    selectedItems.value.reduce((sum, item) => sum + item.productPrice * item.quantity, 0)
  )

  const allSelected = computed(() => 
    items.value.length > 0 && items.value.every(item => item.selected)
  )

  async function fetchCart() {
    loading.value = true
    try {
      const res = await cartApi.getCart()
      items.value = res.data.items || []
    } catch (error) {
      console.error('Failed to fetch cart:', error)
    } finally {
      loading.value = false
    }
  }

  async function addItem(productId, quantity = 1) {
    await cartApi.addItem({ productId, quantity })
    await fetchCart()
  }

  async function updateQuantity(itemId, quantity) {
    await cartApi.updateItem(itemId, { quantity })
    const item = items.value.find(i => i.id === itemId)
    if (item) {
      item.quantity = quantity
    }
  }

  async function toggleSelected(itemId, selected) {
    await cartApi.updateItem(itemId, { selected })
    const item = items.value.find(i => i.id === itemId)
    if (item) {
      item.selected = selected
    }
  }

  async function toggleSelectAll(selected) {
    await cartApi.selectAll(selected)
    items.value.forEach(item => {
      item.selected = selected
    })
  }

  async function removeItem(itemId) {
    await cartApi.deleteItem(itemId)
    items.value = items.value.filter(i => i.id !== itemId)
  }

  async function removeSelected() {
    await cartApi.deleteSelected()
    items.value = items.value.filter(i => !i.selected)
  }

  async function clearCart() {
    await cartApi.clearCart()
    items.value = []
  }

  return {
    items,
    loading,
    totalQuantity,
    selectedItems,
    totalAmount,
    allSelected,
    fetchCart,
    addItem,
    updateQuantity,
    toggleSelected,
    toggleSelectAll,
    removeItem,
    removeSelected,
    clearCart
  }
})

