import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { useToastStore } from '@/stores/toast'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
api.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Response interceptor
api.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code === 200) {
      return data
    } else {
      const toast = useToastStore()
      toast.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
  },
  error => {
    const toast = useToastStore()
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          const userStore = useUserStore()
          userStore.logout()
          toast.error('登录已过期，请重新登录')
          break
        case 403:
          toast.error('没有权限访问')
          break
        case 404:
          toast.error('请求的资源不存在')
          break
        case 500:
          toast.error('服务器错误，请稍后重试')
          break
        default:
          toast.error(error.response.data?.message || '请求失败')
      }
    } else {
      toast.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

// Auth API
export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  getProfile: () => api.get('/auth/profile'),
  updateProfile: (data) => api.put('/auth/profile', data)
}

// Product API
export const productApi = {
  getList: (params) => api.get('/products', { params }),
  getDetail: (id) => api.get(`/products/${id}`),
  getHot: (limit = 8) => api.get('/products/hot', { params: { limit } }),
  getNew: (limit = 8) => api.get('/products/new', { params: { limit } }),
  getRecommend: (limit = 8) => api.get('/products/recommend', { params: { limit } })
}

// Category API
export const categoryApi = {
  getList: () => api.get('/categories'),
  getDetail: (id) => api.get(`/categories/${id}`)
}

// Cart API
export const cartApi = {
  getCart: () => api.get('/cart'),
  addItem: (data) => api.post('/cart', data),
  updateItem: (id, data) => api.put(`/cart/${id}`, data),
  deleteItem: (id) => api.delete(`/cart/${id}`),
  selectAll: (selected) => api.put('/cart/selectAll', { selected }),
  deleteSelected: () => api.delete('/cart/selected'),
  clearCart: () => api.delete('/cart')
}

// Order API
export const orderApi = {
  getList: (params) => api.get('/orders', { params }),
  getDetail: (orderNo) => api.get(`/orders/${orderNo}`),
  create: (data) => api.post('/orders', data),
  pay: (orderNo) => api.put(`/orders/${orderNo}/pay`),
  cancel: (orderNo) => api.put(`/orders/${orderNo}/cancel`),
  confirm: (orderNo) => api.put(`/orders/${orderNo}/confirm`)
}

// Address API
export const addressApi = {
  getList: () => api.get('/addresses'),
  getDefault: () => api.get('/addresses/default'),
  getDetail: (id) => api.get(`/addresses/${id}`),
  create: (data) => api.post('/addresses', data),
  update: (id, data) => api.put(`/addresses/${id}`, data),
  delete: (id) => api.delete(`/addresses/${id}`),
  setDefault: (id) => api.put(`/addresses/${id}/default`)
}

// Favorite API
export const favoriteApi = {
  getList: (params) => api.get('/favorites', { params }),
  toggle: (productId) => api.post('/favorites', { productId }),
  check: (productId) => api.get(`/favorites/check/${productId}`),
  remove: (productId) => api.delete(`/favorites/${productId}`)
}

// Review API
export const reviewApi = {
  getList: (productId, params) => api.get('/reviews', { params: { productId, ...params } }),
  create: (data) => api.post('/reviews', data),
  check: (orderItemId) => api.get(`/reviews/check/${orderItemId}`)
}

// Banner API
export const bannerApi = {
  getList: () => api.get('/banners')
}

// Settings API
export const settingsApi = {
  get: () => api.get('/settings')
}

// Upload API
export const uploadApi = {
  upload: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

export default api

