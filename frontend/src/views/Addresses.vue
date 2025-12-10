<script setup>
import { ref, onMounted } from 'vue'
import { addressApi } from '@/api'
import { useToastStore } from '@/stores/toast'
import Loading from '@/components/Loading.vue'
import Empty from '@/components/Empty.vue'

const toast = useToastStore()

const loading = ref(true)
const addresses = ref([])
const showForm = ref(false)
const editing = ref(null)
const saving = ref(false)

const form = ref({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: false
})

onMounted(() => {
  loadAddresses()
})

async function loadAddresses() {
  loading.value = true
  try {
    const res = await addressApi.getList()
    addresses.value = res.data || []
  } catch (error) {
    console.error('Failed to load addresses:', error)
  } finally {
    loading.value = false
  }
}

function openForm(address = null) {
  if (address) {
    editing.value = address.id
    form.value = { ...address }
  } else {
    editing.value = null
    form.value = {
      receiverName: '',
      phone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      isDefault: false
    }
  }
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editing.value = null
}

async function saveAddress() {
  if (!form.value.receiverName || !form.value.phone || !form.value.detailAddress) {
    toast.warning('请填写完整信息')
    return
  }

  saving.value = true
  try {
    if (editing.value) {
      await addressApi.update(editing.value, form.value)
      toast.success('修改成功')
    } else {
      await addressApi.create(form.value)
      toast.success('添加成功')
    }
    closeForm()
    loadAddresses()
  } catch {
    // Error handled by interceptor
  } finally {
    saving.value = false
  }
}

async function deleteAddress(id) {
  if (!confirm('确定要删除这个地址吗？')) return
  
  try {
    await addressApi.delete(id)
    toast.success('删除成功')
    addresses.value = addresses.value.filter(a => a.id !== id)
  } catch {
    // Error handled by interceptor
  }
}

async function setDefault(id) {
  try {
    await addressApi.setDefault(id)
    addresses.value.forEach(a => {
      a.isDefault = a.id === id
    })
    toast.success('设置成功')
  } catch {
    // Error handled by interceptor
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex items-center justify-between mb-8">
      <h1 class="text-2xl font-bold text-white">收货地址</h1>
      <button @click="openForm()" class="glass-button-primary">
        添加地址
      </button>
    </div>

    <Loading v-if="loading" text="加载地址..." />

    <Empty 
      v-else-if="addresses.length === 0"
      icon="box"
      title="暂无收货地址"
      description="添加收货地址以便购物"
    >
      <button @click="openForm()" class="glass-button-primary mt-4">
        添加地址
      </button>
    </Empty>

    <div v-else class="space-y-4">
      <div
        v-for="address in addresses"
        :key="address.id"
        class="glass-card"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center space-x-3 mb-2">
              <span class="text-white font-medium">{{ address.receiverName }}</span>
              <span class="text-white/60">{{ address.phone }}</span>
              <span 
                v-if="address.isDefault" 
                class="px-2 py-0.5 bg-indigo-500/20 text-indigo-400 text-xs rounded"
              >
                默认
              </span>
            </div>
            <p class="text-white/70">
              {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detailAddress }}
            </p>
          </div>

          <div class="flex items-center space-x-3">
            <button
              v-if="!address.isDefault"
              @click="setDefault(address.id)"
              class="text-white/50 hover:text-indigo-400 text-sm transition-colors"
            >
              设为默认
            </button>
            <button
              @click="openForm(address)"
              class="text-white/50 hover:text-white text-sm transition-colors"
            >
              编辑
            </button>
            <button
              @click="deleteAddress(address.id)"
              class="text-white/50 hover:text-rose-400 text-sm transition-colors"
            >
              删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Address Form Modal -->
    <Teleport to="body">
      <div 
        v-if="showForm"
        class="fixed inset-0 z-50 flex items-center justify-center p-4"
      >
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="closeForm"></div>
        
        <div class="relative glass-card w-full max-w-lg max-h-[90vh] overflow-y-auto">
          <h2 class="text-xl font-bold text-white mb-6">
            {{ editing ? '编辑地址' : '添加地址' }}
          </h2>

          <form @submit.prevent="saveAddress" class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-white/70 text-sm mb-2">收货人 *</label>
                <input
                  v-model="form.receiverName"
                  type="text"
                  placeholder="请输入收货人姓名"
                  class="glass-input"
                />
              </div>
              <div>
                <label class="block text-white/70 text-sm mb-2">手机号 *</label>
                <input
                  v-model="form.phone"
                  type="tel"
                  placeholder="请输入手机号"
                  class="glass-input"
                />
              </div>
            </div>

            <div class="grid grid-cols-3 gap-4">
              <div>
                <label class="block text-white/70 text-sm mb-2">省份</label>
                <input
                  v-model="form.province"
                  type="text"
                  placeholder="省"
                  class="glass-input"
                />
              </div>
              <div>
                <label class="block text-white/70 text-sm mb-2">城市</label>
                <input
                  v-model="form.city"
                  type="text"
                  placeholder="市"
                  class="glass-input"
                />
              </div>
              <div>
                <label class="block text-white/70 text-sm mb-2">区县</label>
                <input
                  v-model="form.district"
                  type="text"
                  placeholder="区"
                  class="glass-input"
                />
              </div>
            </div>

            <div>
              <label class="block text-white/70 text-sm mb-2">详细地址 *</label>
              <textarea
                v-model="form.detailAddress"
                rows="2"
                placeholder="请输入详细地址"
                class="glass-input resize-none"
              ></textarea>
            </div>

            <label class="flex items-center space-x-3 cursor-pointer">
              <input
                type="checkbox"
                v-model="form.isDefault"
                class="w-5 h-5 rounded border-white/20 bg-white/10 text-indigo-500 focus:ring-indigo-500 focus:ring-offset-0"
              />
              <span class="text-white/70">设为默认地址</span>
            </label>

            <div class="flex items-center space-x-4 pt-4">
              <button
                type="submit"
                :disabled="saving"
                class="flex-1 glass-button-primary py-3"
              >
                {{ saving ? '保存中...' : '保存' }}
              </button>
              <button
                type="button"
                @click="closeForm"
                class="flex-1 glass-button py-3"
              >
                取消
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

