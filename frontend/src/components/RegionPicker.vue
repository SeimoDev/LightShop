<script setup>
import { ref, watch, onMounted } from 'vue'

const props = defineProps({
  province: { type: String, default: '' },
  city: { type: String, default: '' },
  district: { type: String, default: '' }
})

const emit = defineEmits(['update:province', 'update:city', 'update:district'])

// 省市区数据 - 对象格式 { 省: { 市: [区, 区, ...] } }
const regionData = ref({})
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
    // 使用开源的省市区数据 API - pca.json 格式: { 省: { 市: [区] } }
    const response = await fetch('https://cdn.jsdelivr.net/gh/modood/Administrative-divisions-of-China@master/dist/pca.json')
    if (!response.ok) {
      throw new Error('Network response was not ok')
    }
    const data = await response.json()
    regionData.value = data
    
    // 初始化省份列表
    provinces.value = Object.keys(data)
    
    initFromProps()
  } catch (error) {
    console.error('Failed to load region data:', error)
    // 使用备用数据源
    try {
      const fallbackResponse = await fetch('https://unpkg.com/china-division/dist/pca.json')
      const data = await fallbackResponse.json()
      regionData.value = data
      provinces.value = Object.keys(data)
      initFromProps()
    } catch {
      console.error('Fallback also failed, using static data')
      useStaticData()
    }
  } finally {
    loading.value = false
  }
}

// 使用静态数据作为最终备用
function useStaticData() {
  regionData.value = {
    '北京市': { '北京市': ['东城区', '西城区', '朝阳区', '丰台区', '石景山区', '海淀区', '顺义区', '通州区', '大兴区', '房山区', '门头沟区', '昌平区', '平谷区', '密云区', '怀柔区', '延庆区'] },
    '上海市': { '上海市': ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区', '虹口区', '杨浦区', '浦东新区', '闵行区', '宝山区', '嘉定区', '金山区', '松江区', '青浦区', '奉贤区', '崇明区'] },
    '天津市': { '天津市': ['和平区', '河东区', '河西区', '南开区', '河北区', '红桥区', '滨海新区', '东丽区', '西青区', '津南区', '北辰区', '武清区', '宝坻区', '宁河区', '静海区', '蓟州区'] },
    '重庆市': { '重庆市': ['万州区', '涪陵区', '渝中区', '大渡口区', '江北区', '沙坪坝区', '九龙坡区', '南岸区', '北碚区', '渝北区', '巴南区', '黔江区', '长寿区', '江津区', '合川区', '永川区'] },
    '广东省': { '广州市': ['荔湾区', '越秀区', '海珠区', '天河区', '白云区', '黄埔区', '番禺区', '花都区', '南沙区', '从化区', '增城区'], '深圳市': ['罗湖区', '福田区', '南山区', '宝安区', '龙岗区', '盐田区', '龙华区', '坪山区', '光明区'], '珠海市': ['香洲区', '斗门区', '金湾区'], '东莞市': ['东莞市'], '佛山市': ['禅城区', '南海区', '顺德区', '三水区', '高明区'] },
    '江苏省': { '南京市': ['玄武区', '秦淮区', '建邺区', '鼓楼区', '浦口区', '栖霞区', '雨花台区', '江宁区', '六合区', '溧水区', '高淳区'], '苏州市': ['虎丘区', '吴中区', '相城区', '姑苏区', '吴江区', '昆山市', '太仓市', '常熟市', '张家港市'], '无锡市': ['锡山区', '惠山区', '滨湖区', '梁溪区', '新吴区', '江阴市', '宜兴市'] },
    '浙江省': { '杭州市': ['上城区', '拱墅区', '西湖区', '滨江区', '萧山区', '余杭区', '富阳区', '临安区', '临平区', '钱塘区'], '宁波市': ['海曙区', '江北区', '北仑区', '镇海区', '鄞州区', '奉化区', '余姚市', '慈溪市'], '温州市': ['鹿城区', '龙湾区', '瓯海区', '洞头区', '瑞安市', '乐清市'] },
    '山东省': { '济南市': ['历下区', '市中区', '槐荫区', '天桥区', '历城区', '长清区', '章丘区', '济阳区', '莱芜区', '钢城区'], '青岛市': ['市南区', '市北区', '黄岛区', '崂山区', '李沧区', '城阳区', '即墨区'], '烟台市': ['芝罘区', '福山区', '牟平区', '莱山区', '蓬莱区'] },
    '四川省': { '成都市': ['锦江区', '青羊区', '金牛区', '武侯区', '成华区', '龙泉驿区', '青白江区', '新都区', '温江区', '双流区', '郫都区', '新津区'] },
    '湖北省': { '武汉市': ['江岸区', '江汉区', '硚口区', '汉阳区', '武昌区', '青山区', '洪山区', '东西湖区', '汉南区', '蔡甸区', '江夏区', '黄陂区', '新洲区'] },
    '湖南省': { '长沙市': ['芙蓉区', '天心区', '岳麓区', '开福区', '雨花区', '望城区', '长沙县', '浏阳市', '宁乡市'] },
    '河南省': { '郑州市': ['中原区', '二七区', '管城回族区', '金水区', '上街区', '惠济区', '中牟县', '巩义市', '荥阳市', '新密市', '新郑市', '登封市'] },
    '河北省': { '石家庄市': ['长安区', '桥西区', '新华区', '井陉矿区', '裕华区', '藁城区', '鹿泉区', '栾城区'], '唐山市': ['路南区', '路北区', '古冶区', '开平区', '丰南区', '丰润区', '曹妃甸区'] },
    '陕西省': { '西安市': ['新城区', '碑林区', '莲湖区', '灞桥区', '未央区', '雁塔区', '阎良区', '临潼区', '长安区', '高陵区', '鄠邑区'] },
    '福建省': { '福州市': ['鼓楼区', '台江区', '仓山区', '马尾区', '晋安区', '长乐区', '闽侯县', '连江县', '罗源县', '闽清县', '永泰县', '平潭县', '福清市'], '厦门市': ['思明区', '海沧区', '湖里区', '集美区', '同安区', '翔安区'] },
    '安徽省': { '合肥市': ['瑶海区', '庐阳区', '蜀山区', '包河区', '长丰县', '肥东县', '肥西县', '庐江县', '巢湖市'] },
    '辽宁省': { '沈阳市': ['和平区', '沈河区', '大东区', '皇姑区', '铁西区', '苏家屯区', '浑南区', '沈北新区', '于洪区', '辽中区', '康平县', '法库县', '新民市'], '大连市': ['中山区', '西岗区', '沙河口区', '甘井子区', '旅顺口区', '金州区', '普兰店区'] },
    '吉林省': { '长春市': ['南关区', '宽城区', '朝阳区', '二道区', '绿园区', '双阳区', '九台区', '农安县', '榆树市', '德惠市', '公主岭市'] },
    '黑龙江省': { '哈尔滨市': ['道里区', '南岗区', '道外区', '平房区', '松北区', '香坊区', '呼兰区', '阿城区', '双城区'] },
    '江西省': { '南昌市': ['东湖区', '西湖区', '青云谱区', '青山湖区', '新建区', '红谷滩区', '南昌县', '安义县', '进贤县'] },
    '山西省': { '太原市': ['小店区', '迎泽区', '杏花岭区', '尖草坪区', '万柏林区', '晋源区', '清徐县', '阳曲县', '娄烦县', '古交市'] },
    '贵州省': { '贵阳市': ['南明区', '云岩区', '花溪区', '乌当区', '白云区', '观山湖区', '开阳县', '息烽县', '修文县', '清镇市'] },
    '云南省': { '昆明市': ['五华区', '盘龙区', '官渡区', '西山区', '东川区', '呈贡区', '晋宁区', '安宁市'] },
    '广西壮族自治区': { '南宁市': ['兴宁区', '青秀区', '江南区', '西乡塘区', '良庆区', '邕宁区', '武鸣区', '隆安县', '马山县', '上林县', '宾阳县', '横州市'] },
    '海南省': { '海口市': ['秀英区', '龙华区', '琼山区', '美兰区'], '三亚市': ['海棠区', '吉阳区', '天涯区', '崖州区'] },
    '内蒙古自治区': { '呼和浩特市': ['新城区', '回民区', '玉泉区', '赛罕区', '土默特左旗', '托克托县', '和林格尔县', '清水河县', '武川县'] },
    '西藏自治区': { '拉萨市': ['城关区', '堆龙德庆区', '达孜区', '林周县', '当雄县', '尼木县', '曲水县', '墨竹工卡县'] },
    '宁夏回族自治区': { '银川市': ['兴庆区', '西夏区', '金凤区', '永宁县', '贺兰县', '灵武市'] },
    '新疆维吾尔自治区': { '乌鲁木齐市': ['天山区', '沙依巴克区', '新市区', '水磨沟区', '头屯河区', '达坂城区', '米东区', '乌鲁木齐县'] },
    '甘肃省': { '兰州市': ['城关区', '七里河区', '西固区', '安宁区', '红古区', '永登县', '皋兰县', '榆中县'] },
    '青海省': { '西宁市': ['城东区', '城中区', '城西区', '城北区', '湟中区', '大通回族土族自治县', '湟源县'] },
    '台湾省': { '台北市': ['中正区', '大同区', '中山区', '松山区', '大安区', '万华区', '信义区', '士林区', '北投区', '内湖区', '南港区', '文山区'] },
    '香港特别行政区': { '香港': ['中西区', '湾仔区', '东区', '南区', '油尖旺区', '深水埗区', '九龙城区', '黄大仙区', '观塘区', '葵青区', '荃湾区', '屯门区', '元朗区', '北区', '大埔区', '沙田区', '西贡区', '离岛区'] },
    '澳门特别行政区': { '澳门': ['花地玛堂区', '花王堂区', '望德堂区', '大堂区', '风顺堂区', '嘉模堂区', '路凼填海区', '圣方济各堂区'] }
  }
  provinces.value = Object.keys(regionData.value)
  initFromProps()
}

// 从 props 初始化选择
function initFromProps() {
  if (props.province && provinces.value.includes(props.province)) {
    selectedProvince.value = props.province
    updateCitiesInternal()
    
    if (props.city && cities.value.includes(props.city)) {
      selectedCity.value = props.city
      updateDistrictsInternal()
      
      if (props.district && districts.value.includes(props.district)) {
        selectedDistrict.value = props.district
      }
    }
  }
}

// 内部更新城市列表（不触发事件）
function updateCitiesInternal() {
  const provinceData = regionData.value[selectedProvince.value]
  if (provinceData) {
    cities.value = Object.keys(provinceData)
  } else {
    cities.value = []
  }
}

// 内部更新区县列表（不触发事件）
function updateDistrictsInternal() {
  const provinceData = regionData.value[selectedProvince.value]
  if (provinceData && provinceData[selectedCity.value]) {
    districts.value = provinceData[selectedCity.value]
  } else {
    districts.value = []
  }
}

// 更新城市列表
function updateCities() {
  updateCitiesInternal()
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
  updateDistrictsInternal()
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
    if (val) {
      updateCitiesInternal()
    }
  }
})

watch(() => props.city, (val) => {
  if (val !== selectedCity.value) {
    selectedCity.value = val
    if (val) {
      updateDistrictsInternal()
    }
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

