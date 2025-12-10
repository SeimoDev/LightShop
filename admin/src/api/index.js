import axios from 'axios'
import { useUserStore } from '@/stores/user'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  error => Promise.reject(error)
)

api.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code === 200) {
      return data
    } else {
      return Promise.reject(new Error(data.message))
    }
  },
  error => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
    }
    return Promise.reject(error)
  }
)

// Auth
export const authApi = {
  login: (data) => api.post('/auth/login', data),
  getProfile: () => api.get('/auth/profile')
}

// Dashboard
export const dashboardApi = {
  getStats: () => api.get('/admin/dashboard')
}

// Products
export const productApi = {
  getList: (params) => api.get('/admin/products', { params }),
  getDetail: (id) => api.get(`/admin/products/${id}`),
  create: (data) => api.post('/admin/products', data),
  update: (id, data) => api.put(`/admin/products/${id}`, data),
  delete: (id) => api.delete(`/admin/products/${id}`)
}

// Categories
export const categoryApi = {
  getList: () => api.get('/admin/categories'),
  create: (data) => api.post('/admin/categories', data),
  update: (id, data) => api.put(`/admin/categories/${id}`, data),
  delete: (id) => api.delete(`/admin/categories/${id}`)
}

// Orders
export const orderApi = {
  getList: (params) => api.get('/admin/orders', { params }),
  getDetail: (orderNo) => api.get(`/admin/orders/${orderNo}`),
  ship: (orderNo) => api.put(`/admin/orders/${orderNo}/ship`),
  cancel: (orderNo) => api.put(`/admin/orders/${orderNo}/cancel`),
  refund: (orderNo) => api.put(`/admin/orders/${orderNo}/refund`)
}

// Users
export const userApi = {
  getList: (params) => api.get('/admin/users', { params }),
  getDetail: (id) => api.get(`/admin/users/${id}`),
  update: (id, data) => api.put(`/admin/users/${id}`, data)
}

// Settings
export const settingsApi = {
  get: () => api.get('/admin/settings'),
  update: (data) => api.put('/admin/settings', data)
}

// Banners
export const bannerApi = {
  getList: () => api.get('/admin/banners'),
  create: (data) => api.post('/admin/settings', data),
  delete: (id) => api.delete(`/admin/settings/banners/${id}`)
}

// Upload
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

