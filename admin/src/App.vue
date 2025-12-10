<script setup>
import { computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import Sidebar from '@/components/Sidebar.vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const showLayout = computed(() => {
  return route.name !== 'Login' && userStore.isLoggedIn
})
</script>

<template>
  <div class="min-h-screen">
    <div v-if="showLayout" class="flex">
      <Sidebar />
      <main class="flex-1 ml-64 p-8">
        <RouterView v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </RouterView>
      </main>
    </div>

    <RouterView v-else />
  </div>
</template>

