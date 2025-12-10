<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi, categoryApi, uploadApi } from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const categories = ref([])
const isEdit = computed(() => !!route.params.id)

const form = ref({
  name: '',
  description: '',
  price: '',
  originalPrice: '',
  stock: 0,
  categoryId: '',
  images: [],
  status: 1
})

const errors = ref({})

onMounted(async () => {
  await loadCategories()
  if (isEdit.value) {
    await loadProduct()
  }
})

async function loadCategories() {
  try {
    const res = await categoryApi.getList()
    categories.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

async function loadProduct() {
  loading.value = true
  try {
    const res = await productApi.getDetail(route.params.id)
    const product = res.data
    form.value = {
      name: product.name,
      description: product.description || '',
      price: product.price,
      originalPrice: product.originalPrice || '',
      stock: product.stock,
      categoryId: product.categoryId || '',
      images: product.images ? JSON.parse(product.images) : [],
      status: product.status
    }
  } catch (e) {
    console.error(e)
    alert('加载商品信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

function validate() {
  errors.value = {}
  if (!form.value.name.trim()) {
    errors.value.name = '请输入商品名称'
  }
  if (!form.value.price || form.value.price <= 0) {
    errors.value.price = '请输入有效价格'
  }
  if (form.value.stock < 0) {
    errors.value.stock = '库存不能为负数'
  }
  return Object.keys(errors.value).length === 0
}

async function handleUpload(e) {
  const file = e.target.files[0]
  if (!file) return

  try {
    const res = await uploadApi.upload(file)
    form.value.images.push(res.data.url)
  } catch (err) {
    console.error(err)
    alert('上传失败')
  }
}

function removeImage(index) {
  form.value.images.splice(index, 1)
}

async function submit() {
  if (!validate()) return

  saving.value = true
  try {
    const data = {
      name: form.value.name,
      description: form.value.description,
      price: parseFloat(form.value.price),
      originalPrice: form.value.originalPrice ? parseFloat(form.value.originalPrice) : parseFloat(form.value.price),
      stock: parseInt(form.value.stock),
      categoryId: form.value.categoryId ? parseInt(form.value.categoryId) : 0,
      images: form.value.images,
      status: form.value.status
    }

    if (isEdit.value) {
      await productApi.update(route.params.id, data)
      alert('更新成功')
    } else {
      await productApi.create(data)
      alert('创建成功')
    }
    router.push('/products')
  } catch (e) {
    console.error(e)
    alert('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-8">
      <h1 class="text-2xl font-bold text-white">{{ isEdit ? '编辑商品' : '添加商品' }}</h1>
      <button @click="router.back()" class="btn-secondary">返回</button>
    </div>

    <div v-if="loading" class="card text-center py-12 text-slate-400">
      加载中...
    </div>

    <form v-else @submit.prevent="submit" class="card space-y-6">
      <!-- 商品名称 -->
      <div>
        <label class="block text-slate-300 mb-2">商品名称 <span class="text-rose-400">*</span></label>
        <input v-model="form.name" type="text" class="input" placeholder="请输入商品名称" />
        <p v-if="errors.name" class="text-rose-400 text-sm mt-1">{{ errors.name }}</p>
      </div>

      <!-- 商品描述 -->
      <div>
        <label class="block text-slate-300 mb-2">商品描述</label>
        <textarea 
          v-model="form.description" 
          class="input min-h-[120px]" 
          placeholder="请输入商品描述"
        ></textarea>
      </div>

      <!-- 价格和原价 -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label class="block text-slate-300 mb-2">售价 <span class="text-rose-400">*</span></label>
          <input 
            v-model="form.price" 
            type="number" 
            step="0.01" 
            min="0" 
            class="input" 
            placeholder="0.00" 
          />
          <p v-if="errors.price" class="text-rose-400 text-sm mt-1">{{ errors.price }}</p>
        </div>
        <div>
          <label class="block text-slate-300 mb-2">原价</label>
          <input 
            v-model="form.originalPrice" 
            type="number" 
            step="0.01" 
            min="0" 
            class="input" 
            placeholder="0.00 (可选，用于显示划线价)" 
          />
        </div>
      </div>

      <!-- 库存和分类 -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label class="block text-slate-300 mb-2">库存 <span class="text-rose-400">*</span></label>
          <input v-model="form.stock" type="number" min="0" class="input" />
          <p v-if="errors.stock" class="text-rose-400 text-sm mt-1">{{ errors.stock }}</p>
        </div>
        <div>
          <label class="block text-slate-300 mb-2">分类</label>
          <select v-model="form.categoryId" class="select">
            <option value="">请选择分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </option>
          </select>
        </div>
      </div>

      <!-- 商品图片 -->
      <div>
        <label class="block text-slate-300 mb-2">商品图片</label>
        <div class="flex flex-wrap gap-4">
          <div 
            v-for="(img, index) in form.images" 
            :key="index"
            class="relative w-24 h-24 rounded-lg overflow-hidden group"
          >
            <img :src="img" class="w-full h-full object-cover" />
            <button
              type="button"
              @click="removeImage(index)"
              class="absolute inset-0 bg-black/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
          
          <label class="w-24 h-24 rounded-lg border-2 border-dashed border-slate-600 flex items-center justify-center cursor-pointer hover:border-indigo-500 transition-colors">
            <input type="file" accept="image/*" class="hidden" @change="handleUpload" />
            <svg class="w-8 h-8 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
            </svg>
          </label>
        </div>
        <p class="text-slate-500 text-sm mt-2">支持 JPG、PNG 格式，建议尺寸 800x800</p>
      </div>

      <!-- 状态 -->
      <div>
        <label class="block text-slate-300 mb-2">状态</label>
        <div class="flex items-center space-x-6">
          <label class="flex items-center space-x-2 cursor-pointer">
            <input 
              type="radio" 
              v-model="form.status" 
              :value="1" 
              class="text-indigo-500 focus:ring-indigo-500"
            />
            <span class="text-white">上架</span>
          </label>
          <label class="flex items-center space-x-2 cursor-pointer">
            <input 
              type="radio" 
              v-model="form.status" 
              :value="0" 
              class="text-indigo-500 focus:ring-indigo-500"
            />
            <span class="text-white">下架</span>
          </label>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="flex items-center justify-end space-x-4 pt-6 border-t border-slate-700">
        <button type="button" @click="router.back()" class="btn-secondary">
          取消
        </button>
        <button type="submit" :disabled="saving" class="btn-primary">
          {{ saving ? '保存中...' : (isEdit ? '保存修改' : '创建商品') }}
        </button>
      </div>
    </form>
  </div>
</template>

