<script setup>
defineProps({
  text: {
    type: String,
    default: '加载中...'
  },
  fullscreen: {
    type: Boolean,
    default: false
  }
})
</script>

<template>
  <div 
    :class="[
      'flex flex-col items-center justify-center',
      fullscreen ? 'fixed inset-0 z-50 bg-black/50 backdrop-blur-sm' : 'py-12'
    ]"
  >
    <div class="relative">
      <!-- Outer ring -->
      <div class="w-16 h-16 rounded-full border-4 border-white/10"></div>
      <!-- Spinning gradient ring -->
      <div class="absolute inset-0 w-16 h-16 rounded-full border-4 border-transparent border-t-indigo-500 border-r-purple-500 animate-spin"></div>
      <!-- Inner glow -->
      <div class="absolute inset-2 w-12 h-12 rounded-full bg-gradient-to-br from-indigo-500/20 to-purple-500/20 animate-pulse"></div>
    </div>
    <p class="mt-4 text-white/60 text-sm">{{ text }}</p>
  </div>
</template>

