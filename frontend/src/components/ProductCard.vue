<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const imageUrl = computed(() => {
  const images = props.product.images
  if (!images) return 'https://picsum.photos/400/400?random=' + props.product.id
  
  if (typeof images === 'string') {
    if (images.startsWith('[')) {
      try {
        const arr = JSON.parse(images)
        return arr[0] || 'https://picsum.photos/400/400?random=' + props.product.id
      } catch {
        return images
      }
    }
    return images
  }
  
  if (Array.isArray(images)) {
    return images[0] || 'https://picsum.photos/400/400?random=' + props.product.id
  }
  
  return 'https://picsum.photos/400/400?random=' + props.product.id
})

const hasDiscount = computed(() => {
  return props.product.originalPrice && props.product.originalPrice > props.product.price
})

const discountPercent = computed(() => {
  if (!hasDiscount.value) return 0
  return Math.round((1 - props.product.price / props.product.originalPrice) * 100)
})
</script>

<template>
  <RouterLink 
    :to="`/product/${product.id}`" 
    class="glass-card group overflow-hidden block"
  >
    <!-- Image -->
    <div class="relative aspect-square overflow-hidden rounded-xl mb-4">
      <img 
        :src="imageUrl" 
        :alt="product.name"
        class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
        loading="lazy"
      />
      
      <!-- Discount Badge -->
      <div 
        v-if="hasDiscount"
        class="absolute top-2 left-2 bg-gradient-to-r from-rose-500 to-pink-500 text-white text-xs font-bold px-2 py-1 rounded-lg"
      >
        -{{ discountPercent }}%
      </div>

      <!-- Stock Warning -->
      <div 
        v-if="product.stock < 10 && product.stock > 0"
        class="absolute top-2 right-2 bg-amber-500/80 backdrop-blur text-white text-xs px-2 py-1 rounded-lg"
      >
        仅剩 {{ product.stock }} 件
      </div>

      <!-- Sold Out -->
      <div 
        v-if="product.stock === 0"
        class="absolute inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center"
      >
        <span class="text-white font-bold text-lg">已售罄</span>
      </div>
    </div>

    <!-- Info -->
    <div class="space-y-2">
      <h3 class="text-white font-medium line-clamp-2 group-hover:text-indigo-300 transition-colors">
        {{ product.name }}
      </h3>
      
      <div class="flex items-center space-x-2">
        <span class="text-xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-pink-400 to-purple-400">
          ¥{{ product.price.toFixed(2) }}
        </span>
        <span 
          v-if="hasDiscount" 
          class="text-sm text-white/40 line-through"
        >
          ¥{{ product.originalPrice.toFixed(2) }}
        </span>
      </div>

      <div class="flex items-center justify-between text-xs text-white/50">
        <span>已售 {{ product.sales || 0 }}</span>
        <span v-if="product.categoryName">{{ product.categoryName }}</span>
      </div>
    </div>
  </RouterLink>
</template>

