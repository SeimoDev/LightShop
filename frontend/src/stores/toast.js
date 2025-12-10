import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useToastStore = defineStore('toast', () => {
  const messages = ref([])
  let id = 0

  function add(message, type = 'info', duration = 3000) {
    const toast = {
      id: ++id,
      message,
      type,
      duration
    }
    messages.value.push(toast)

    if (duration > 0) {
      setTimeout(() => {
        remove(toast.id)
      }, duration)
    }

    return toast.id
  }

  function remove(id) {
    const index = messages.value.findIndex(m => m.id === id)
    if (index > -1) {
      messages.value.splice(index, 1)
    }
  }

  function success(message, duration) {
    return add(message, 'success', duration)
  }

  function error(message, duration) {
    return add(message, 'error', duration)
  }

  function warning(message, duration) {
    return add(message, 'warning', duration)
  }

  function info(message, duration) {
    return add(message, 'info', duration)
  }

  return {
    messages,
    add,
    remove,
    success,
    error,
    warning,
    info
  }
})

