<script setup>
import { ref, watch, onMounted } from 'vue'

const props = defineProps({
  province: { type: String, default: '' },
  city: { type: String, default: '' },
  district: { type: String, default: '' }
})

const emit = defineEmits(['update:province', 'update:city', 'update:district'])

// 省市区数据
const regionData = ref([])
const loading = ref(true)

// 当前选择
const selectedProvince = ref('')
const selectedCity = ref('')
const selectedDistrict = ref('')

// 可选列表
const provinces = ref([])
const cities = ref([])
const districts = ref([])

// 加载省市区数据
async function loadRegionData() {
  loading.value = true
  try {
    // 使用开源的省市区数据 API
    const response = await fetch('https://cdn.jsdelivr.net/gh/modood/Administrative-divisions-of-China@master/dist/pca.json')
    if (!response.ok) {
      throw new Error('Network response was not ok')
    }
    regionData.value = await response.json()
    
    // 初始化省份列表
    provinces.value = regionData.value.map(p => p.name)
    
    // 如果有初始值，设置选择
    if (props.province) {
      selectedProvince.value = props.province
      updateCities()
      
      if (props.city) {
        selectedCity.value = props.city
        updateDistricts()
        
        if (props.district) {
          selectedDistrict.value = props.district
        }
      }
    }
  } catch (error) {
    console.error('Failed to load region data:', error)
    // 使用备用数据源
    try {
      const fallbackResponse = await fetch('https://unpkg.com/china-division/dist/areas.json')
      const data = await fallbackResponse.json()
      // 转换数据格式
      regionData.value = convertFallbackData(data)
      provinces.value = regionData.value.map(p => p.name)
    } catch {
      console.error('Fallback also failed, using static provinces')
      // 最后备用：使用静态省份列表
      provinces.value = [
        '北京市', '天津市', '河北省', '山西省', '内蒙古自治区',
        '辽宁省', '吉林省', '黑龙江省', '上海市', '江苏省',
        '浙江省', '安徽省', '福建省', '江西省', '山东省',
        '河南省', '湖北省', '湖南省', '广东省', '广西壮族自治区',
        '海南省', '重庆市', '四川省', '贵州省', '云南省',
        '西藏自治区', '陕西省', '甘肃省', '青海省', '宁夏回族自治区',
        '新疆维吾尔自治区', '台湾省', '香港特别行政区', '澳门特别行政区'
      ]
    }
  } finally {
    loading.value = false
  }
}

// 转换备用数据格式
function convertFallbackData(data) {
  // china-division 的数据格式和 pca.json 不同，需要转换
  return data.map(province => ({
    name: province.name,
    children: (province.children || []).map(city => ({
      name: city.name,
      children: (city.children || []).map(district => ({
        name: district.name
      }))
    }))
  }))
}

// 更新城市列表
function updateCities() {
  const province = regionData.value.find(p => p.name === selectedProvince.value)
  if (province && province.children) {
    cities.value = province.children.map(c => c.name)
  } else {
    cities.value = []
  }
  // 清空下级选择
  selectedCity.value = ''
  selectedDistrict.value = ''
  districts.value = []
  
  emit('update:province', selectedProvince.value)
  emit('update:city', '')
  emit('update:district', '')
}

// 更新区县列表
function updateDistricts() {
  const province = regionData.value.find(p => p.name === selectedProvince.value)
  if (province && province.children) {
    const city = province.children.find(c => c.name === selectedCity.value)
    if (city && city.children) {
      districts.value = city.children.map(d => d.name)
    } else {
      districts.value = []
    }
  } else {
    districts.value = []
  }
  // 清空区县选择
  selectedDistrict.value = ''
  
  emit('update:city', selectedCity.value)
  emit('update:district', '')
}

// 区县变化
function onDistrictChange() {
  emit('update:district', selectedDistrict.value)
}

// 监听外部属性变化
watch(() => props.province, (val) => {
  if (val !== selectedProvince.value) {
    selectedProvince.value = val
    if (val) updateCities()
  }
})

watch(() => props.city, (val) => {
  if (val !== selectedCity.value) {
    selectedCity.value = val
    if (val) updateDistricts()
  }
})

watch(() => props.district, (val) => {
  if (val !== selectedDistrict.value) {
    selectedDistrict.value = val
  }
})

onMounted(() => {
  loadRegionData()
})
</script>

<template>
  <div class="grid grid-cols-3 gap-4">
    <div>
      <label class="block text-white/70 text-sm mb-2">省份</label>
      <div class="relative">
        <select
          v-model="selectedProvince"
          @change="updateCities"
          :disabled="loading"
          class="glass-select w-full"
        >
          <option value="">{{ loading ? '加载中...' : '请选择省份' }}</option>
          <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
        </select>
        <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-white/50">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
          </svg>
        </div>
      </div>
    </div>
    
    <div>
      <label class="block text-white/70 text-sm mb-2">城市</label>
      <div class="relative">
        <select
          v-model="selectedCity"
          @change="updateDistricts"
          :disabled="!selectedProvince || loading"
          class="glass-select w-full"
        >
          <option value="">请选择城市</option>
          <option v-for="c in cities" :key="c" :value="c">{{ c }}</option>
        </select>
        <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-white/50">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
          </svg>
        </div>
      </div>
    </div>
    
    <div>
      <label class="block text-white/70 text-sm mb-2">区县</label>
      <div class="relative">
        <select
          v-model="selectedDistrict"
          @change="onDistrictChange"
          :disabled="!selectedCity || loading"
          class="glass-select w-full"
        >
          <option value="">请选择区县</option>
          <option v-for="d in districts" :key="d" :value="d">{{ d }}</option>
        </select>
        <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-white/50">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
          </svg>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.glass-select {
  appearance: none;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 0.75rem;
  padding: 0.75rem 2.5rem 0.75rem 1rem;
  color: white;
  font-size: 0.875rem;
  transition: all 0.2s;
  cursor: pointer;
}

.glass-select:hover:not(:disabled) {
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.15);
}

.glass-select:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.glass-select:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.glass-select option {
  background: #1f2937;
  color: white;
  padding: 0.5rem;
}
</style>

