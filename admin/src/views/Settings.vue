<script setup>
import { ref, onMounted } from 'vue'
import { settingsApi, bannerApi, uploadApi } from '@/api'

const loading = ref(true)
const saving = ref(false)
const activeTab = ref('site')

const siteForm = ref({
  siteName: '',
  logo: '',
  description: '',
  keywords: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  copyright: ''
})

const banners = ref([])
const showBannerModal = ref(false)
const editingBanner = ref(null)
const bannerForm = ref({
  title: '',
  image: '',
  link: '',
  sortOrder: 0,
  status: 1
})

onMounted(async () => {
  await loadSettings()
  await loadBanners()
})

async function loadSettings() {
  loading.value = true
  try {
    const res = await settingsApi.get()
    if (res.data) {
      siteForm.value = {
        siteName: res.data.siteName || 'LightShop',
        logo: res.data.logo || '',
        description: res.data.description || '',
        keywords: res.data.keywords || '',
        contactPhone: res.data.contactPhone || '',
        contactEmail: res.data.contactEmail || '',
        address: res.data.address || '',
        copyright: res.data.copyright || ''
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadBanners() {
  try {
    const res = await bannerApi.getList()
    banners.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

async function saveSettings() {
  saving.value = true
  try {
    await settingsApi.update(siteForm.value)
    alert('保存成功')
  } catch (e) {
    console.error(e)
    alert('保存失败')
  } finally {
    saving.value = false
  }
}

async function handleLogoUpload(e) {
  const file = e.target.files[0]
  if (!file) return

  try {
    const res = await uploadApi.upload(file)
    siteForm.value.logo = res.data.url
  } catch (err) {
    console.error(err)
    alert('上传失败')
  }
}

function openBannerModal(banner = null) {
  editingBanner.value = banner
  if (banner) {
    bannerForm.value = {
      title: banner.title,
      image: banner.image,
      link: banner.link || '',
      sortOrder: banner.sortOrder || 0,
      status: banner.status
    }
  } else {
    bannerForm.value = { title: '', image: '', link: '', sortOrder: 0, status: 1 }
  }
  showBannerModal.value = true
}

async function handleBannerImageUpload(e) {
  const file = e.target.files[0]
  if (!file) return

  try {
    const res = await uploadApi.upload(file)
    bannerForm.value.image = res.data.url
  } catch (err) {
    console.error(err)
    alert('上传失败')
  }
}

async function saveBanner() {
  if (!bannerForm.value.title.trim() || !bannerForm.value.image) {
    alert('请填写标题并上传图片')
    return
  }

  try {
    await bannerApi.create(bannerForm.value)
    alert('保存成功')
    showBannerModal.value = false
    loadBanners()
  } catch (e) {
    console.error(e)
    alert('保存失败')
  }
}

async function deleteBanner(id) {
  if (!confirm('确定要删除这个轮播图吗？')) return
  try {
    await bannerApi.delete(id)
    loadBanners()
  } catch (e) {
    console.error(e)
    alert('删除失败')
  }
}
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold text-white mb-8">系统设置</h1>

    <!-- Tabs -->
    <div class="flex space-x-4 mb-6">
      <button
        @click="activeTab = 'site'"
        :class="['px-4 py-2 rounded-lg transition-colors', 
          activeTab === 'site' ? 'bg-indigo-500 text-white' : 'bg-slate-700 text-slate-300 hover:bg-slate-600']"
      >
        网站设置
      </button>
      <button
        @click="activeTab = 'banners'"
        :class="['px-4 py-2 rounded-lg transition-colors', 
          activeTab === 'banners' ? 'bg-indigo-500 text-white' : 'bg-slate-700 text-slate-300 hover:bg-slate-600']"
      >
        轮播图管理
      </button>
    </div>

    <!-- Site Settings -->
    <div v-if="activeTab === 'site'" class="card">
      <div v-if="loading" class="text-center py-8 text-slate-400">
        加载中...
      </div>

      <form v-else @submit.prevent="saveSettings" class="space-y-6">
        <!-- Logo -->
        <div>
          <label class="block text-slate-300 mb-2">网站 Logo</label>
          <div class="flex items-center space-x-4">
            <div class="w-20 h-20 rounded-xl bg-slate-700 flex items-center justify-center overflow-hidden">
              <img v-if="siteForm.logo" :src="siteForm.logo" class="w-full h-full object-contain" />
              <span v-else class="text-3xl">🛒</span>
            </div>
            <label class="btn-secondary cursor-pointer">
              上传 Logo
              <input type="file" accept="image/*" class="hidden" @change="handleLogoUpload" />
            </label>
          </div>
        </div>

        <!-- Site Name -->
        <div>
          <label class="block text-slate-300 mb-2">网站名称</label>
          <input v-model="siteForm.siteName" type="text" class="input" />
        </div>

        <!-- Description -->
        <div>
          <label class="block text-slate-300 mb-2">网站描述</label>
          <textarea v-model="siteForm.description" class="input min-h-[100px]"></textarea>
        </div>

        <!-- Keywords -->
        <div>
          <label class="block text-slate-300 mb-2">SEO 关键词</label>
          <input v-model="siteForm.keywords" type="text" class="input" placeholder="多个关键词用逗号分隔" />
        </div>

        <!-- Contact Info -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label class="block text-slate-300 mb-2">联系电话</label>
            <input v-model="siteForm.contactPhone" type="text" class="input" />
          </div>
          <div>
            <label class="block text-slate-300 mb-2">联系邮箱</label>
            <input v-model="siteForm.contactEmail" type="email" class="input" />
          </div>
        </div>

        <!-- Address -->
        <div>
          <label class="block text-slate-300 mb-2">公司地址</label>
          <input v-model="siteForm.address" type="text" class="input" />
        </div>

        <!-- Copyright -->
        <div>
          <label class="block text-slate-300 mb-2">版权信息</label>
          <input v-model="siteForm.copyright" type="text" class="input" />
        </div>

        <!-- Submit -->
        <div class="flex justify-end pt-6 border-t border-slate-700">
          <button type="submit" :disabled="saving" class="btn-primary">
            {{ saving ? '保存中...' : '保存设置' }}
          </button>
        </div>
      </form>
    </div>

    <!-- Banners Management -->
    <div v-if="activeTab === 'banners'" class="card">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-lg font-bold text-white">轮播图列表</h2>
        <button @click="openBannerModal()" class="btn-primary">添加轮播图</button>
      </div>

      <div v-if="banners.length === 0" class="text-center py-12 text-slate-500">
        暂无轮播图，点击上方按钮添加
      </div>

      <div v-else class="grid gap-4">
        <div
          v-for="banner in banners"
          :key="banner.id"
          class="flex items-center space-x-4 bg-slate-700/50 rounded-xl p-4"
        >
          <img :src="banner.image" class="w-32 h-20 rounded-lg object-cover" />
          <div class="flex-1">
            <h3 class="text-white font-medium">{{ banner.title }}</h3>
            <p class="text-slate-400 text-sm">{{ banner.link || '无链接' }}</p>
          </div>
          <span :class="['badge', banner.status === 1 ? 'badge-success' : 'badge-danger']">
            {{ banner.status === 1 ? '显示' : '隐藏' }}
          </span>
          <div class="flex items-center space-x-2">
            <button @click="openBannerModal(banner)" class="text-indigo-400 hover:text-indigo-300">
              编辑
            </button>
            <button @click="deleteBanner(banner.id)" class="text-rose-400 hover:text-rose-300">
              删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Banner Modal -->
    <div v-if="showBannerModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-slate-800 rounded-2xl p-6 w-full max-w-lg">
        <h2 class="text-xl font-bold text-white mb-6">
          {{ editingBanner ? '编辑轮播图' : '添加轮播图' }}
        </h2>

        <form @submit.prevent="saveBanner" class="space-y-4">
          <div>
            <label class="block text-slate-300 mb-2">标题</label>
            <input v-model="bannerForm.title" type="text" class="input" placeholder="轮播图标题" />
          </div>

          <div>
            <label class="block text-slate-300 mb-2">图片</label>
            <div v-if="bannerForm.image" class="mb-3">
              <img :src="bannerForm.image" class="w-full h-32 rounded-lg object-cover" />
            </div>
            <label class="btn-secondary cursor-pointer inline-block">
              {{ bannerForm.image ? '更换图片' : '上传图片' }}
              <input type="file" accept="image/*" class="hidden" @change="handleBannerImageUpload" />
            </label>
            <p class="text-slate-500 text-sm mt-2">建议尺寸：1200 × 400</p>
          </div>

          <div>
            <label class="block text-slate-300 mb-2">链接地址</label>
            <input v-model="bannerForm.link" type="text" class="input" placeholder="/products?keyword=xxx" />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-slate-300 mb-2">排序</label>
              <input v-model.number="bannerForm.sortOrder" type="number" min="0" class="input" />
            </div>
            <div>
              <label class="block text-slate-300 mb-2">状态</label>
              <select v-model="bannerForm.status" class="select">
                <option :value="1">显示</option>
                <option :value="0">隐藏</option>
              </select>
            </div>
          </div>

          <div class="flex items-center justify-end space-x-4 pt-4">
            <button type="button" @click="showBannerModal = false" class="btn-secondary">
              取消
            </button>
            <button type="submit" class="btn-primary">
              保存
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

