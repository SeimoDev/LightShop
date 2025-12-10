<script setup>
import { useToastStore } from '@/stores/toast'

const toastStore = useToastStore()

const icons = {
  success: `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>`,
  error: `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>`,
  warning: `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>`,
  info: `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>`
}

const colors = {
  success: 'from-emerald-500/20 to-emerald-600/20 border-emerald-500/30 text-emerald-200',
  error: 'from-rose-500/20 to-rose-600/20 border-rose-500/30 text-rose-200',
  warning: 'from-amber-500/20 to-amber-600/20 border-amber-500/30 text-amber-200',
  info: 'from-indigo-500/20 to-indigo-600/20 border-indigo-500/30 text-indigo-200'
}
</script>

<template>
  <div class="fixed top-20 right-4 z-50 space-y-2">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toastStore.messages"
        :key="toast.id"
        :class="[
          'flex items-center space-x-3 px-4 py-3 rounded-xl backdrop-blur-xl border min-w-[280px] max-w-sm shadow-lg bg-gradient-to-r',
          colors[toast.type]
        ]"
      >
        <span v-html="icons[toast.type]"></span>
        <span class="flex-1 text-sm">{{ toast.message }}</span>
        <button 
          @click="toastStore.remove(toast.id)"
          class="text-white/50 hover:text-white transition-colors"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-enter-active {
  animation: slideIn 0.3s ease;
}

.toast-leave-active {
  animation: slideOut 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideOut {
  from {
    opacity: 1;
    transform: translateX(0);
  }
  to {
    opacity: 0;
    transform: translateX(100%);
  }
}
</style>

