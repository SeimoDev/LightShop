# LightShop 商城系统技术架构介绍
## PPT 设计稿

---

## 第1页：封面

### LightShop B2C 商城系统
**技术架构与实现详解**

- 副标题：基于 Vue3 + JSP/Servlet 的现代化电商平台
- 设计风格：Liquid Glass（液态玻璃）
- 版本：1.0.0

**视觉建议**：
- 背景使用紫色/粉色渐变，体现液态玻璃风格
- 展示商城首页截图的毛玻璃效果

---

## 第2页：目录

### 内容概览

1. 项目概述与技术选型
2. 系统整体架构
3. 后端技术详解
4. 前端技术详解
5. 数据库设计
6. 用户端功能模块
7. 管理端功能模块
8. 安全机制
9. 部署方案
10. 总结与展望

---

## 第3页：项目概述

### 项目背景与目标

**项目定位**
- B2C 电子商务平台
- 支持商品浏览、购物车、订单、支付等完整购物流程
- 提供后台管理系统

**核心特性**
| 特性 | 说明 |
|------|------|
| 🎨 液态玻璃UI | 现代化毛玻璃视觉效果 |
| 📱 响应式设计 | 适配移动端/平板/桌面 |
| 🔐 JWT认证 | 无状态安全认证 |
| 🐳 容器化部署 | Docker一键部署 |
| 🌏 省市区联动 | 网络数据源地址选择 |

---

## 第4页：技术选型总览

### 前后端技术栈

```
┌─────────────────────────────────────────────────────────┐
│                    前端 (Frontend)                       │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────────┐   │
│  │  Vue 3  │ │ Pinia   │ │  Axios  │ │ TailwindCSS │   │
│  └─────────┘ └─────────┘ └─────────┘ └─────────────┘   │
│  ┌─────────┐ ┌─────────┐                               │
│  │  Vite   │ │Vue Router│                              │
│  └─────────┘ └─────────┘                               │
└─────────────────────────────────────────────────────────┘
                          │ HTTP/REST API
                          ▼
┌─────────────────────────────────────────────────────────┐
│                    后端 (Backend)                        │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────────┐   │
│  │ Servlet │ │   JSP   │ │  JDBC   │ │    Gson     │   │
│  └─────────┘ └─────────┘ └─────────┘ └─────────────┘   │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐                   │
│  │   JWT   │ │ BCrypt  │ │ SQLite  │                   │
│  └─────────┘ └─────────┘ └─────────┘                   │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│               运行环境 (Runtime)                         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐   │
│  │  Tomcat 9   │ │   Java 11   │ │     Docker      │   │
│  └─────────────┘ └─────────────┘ └─────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## 第5页：系统架构图

### 三层架构设计

```
┌────────────────────────────────────────────────────────────────┐
│                         客户端层                                │
│    ┌──────────────────┐        ┌──────────────────┐           │
│    │   用户端 Vue3    │        │  管理端 Vue3     │           │
│    │   :5173          │        │  :5174           │           │
│    └────────┬─────────┘        └────────┬─────────┘           │
└─────────────│────────────────────────────│────────────────────┘
              │          HTTP              │
              ▼                            ▼
┌────────────────────────────────────────────────────────────────┐
│                        服务端层                                 │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                    Tomcat 容器 :8080                      │ │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────────────┐  │ │
│  │  │ CorsFilter │→ │ AuthFilter │→ │    Servlets        │  │ │
│  │  └────────────┘  └────────────┘  │  ├─ AuthServlet    │  │ │
│  │                                   │  ├─ ProductServlet│  │ │
│  │                                   │  ├─ CartServlet   │  │ │
│  │                                   │  ├─ OrderServlet  │  │ │
│  │                                   │  └─ ...           │  │ │
│  │                                   └────────────────────┘  │ │
│  └──────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                         数据层                                  │
│          ┌───────────────────────────────────┐                 │
│          │         SQLite Database           │                 │
│          │     /app/data/lightshop.db        │                 │
│          └───────────────────────────────────┘                 │
└────────────────────────────────────────────────────────────────┘
```

---

## 第6页：后端架构详解

### Java 后端分层结构

```
com.lightshop/
├── servlet/        # 控制器层 - 处理HTTP请求
│   ├── auth/       # 认证相关 (登录/注册/个人信息)
│   ├── admin/      # 管理端API
│   └── *.java      # 用户端API
├── dao/            # 数据访问层 - 操作数据库
├── model/          # 实体模型层 - JavaBean
├── filter/         # 过滤器 - 请求预处理
├── listener/       # 监听器 - 应用生命周期
└── util/           # 工具类
```

**核心依赖**
| 依赖 | 版本 | 用途 |
|------|------|------|
| javax.servlet-api | 4.0.1 | Servlet 规范 |
| sqlite-jdbc | 3.42.0 | SQLite 驱动 |
| gson | 2.10.1 | JSON 序列化 |
| jjwt | 0.11.5 | JWT 令牌 |
| jbcrypt | 0.4 | 密码加密 |
| commons-fileupload | 1.5 | 文件上传 |

---

## 第7页：Servlet 控制器设计

### RESTful API 设计模式

**请求处理流程**
```
HTTP Request
     │
     ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ CorsFilter  │ → │ AuthFilter  │ → │ JsonFilter  │
└─────────────┘    └─────────────┘    └─────────────┘
                                            │
                                            ▼
                                    ┌─────────────┐
                                    │   Servlet   │
                                    │  doGet()    │
                                    │  doPost()   │
                                    │  doPut()    │
                                    │  doDelete() │
                                    └─────────────┘
```

**Servlet 示例代码结构**
```java
@WebServlet("/api/products/*")
public class ProductServlet extends HttpServlet {
    private ProductDao productDao = new ProductDao();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // 1. 解析请求参数
        // 2. 调用 DAO 查询数据
        // 3. 使用 Gson 序列化响应
        // 4. 返回 JSON 结果
    }
}
```

---

## 第8页：过滤器链设计

### 三层过滤器机制

**1. CorsFilter - 跨域处理**
```java
// 允许跨域请求
response.setHeader("Access-Control-Allow-Origin", origin);
response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
```

**2. AuthFilter - JWT 认证**
```java
// 验证 JWT Token
String token = request.getHeader("Authorization").replace("Bearer ", "");
Claims claims = JwtUtil.parseToken(token);
request.setAttribute("userId", claims.get("userId"));
```

**3. JsonFilter - 响应格式化**
```java
// 统一设置 JSON 响应头
response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
```

---

## 第9页：DAO 数据访问层

### JDBC 直接操作数据库

**设计特点**
- 每个实体对应一个 DAO 类
- 使用 PreparedStatement 防止 SQL 注入
- 手动管理数据库连接

**DAO 类列表**
| DAO 类 | 职责 |
|--------|------|
| UserDao | 用户增删改查、登录验证 |
| ProductDao | 商品管理、搜索、分页 |
| CartDao | 购物车操作 |
| OrderDao | 订单创建、状态管理 |
| AddressDao | 收货地址管理 |
| FavoriteDao | 收藏管理 |
| ReviewDao | 评价管理 |
| CategoryDao | 分类管理 |
| BannerDao | 轮播图管理 |
| SettingsDao | 系统配置 |

**代码示例**
```java
public Product findById(int id) {
    String sql = "SELECT p.*, c.name as category_name FROM products p " +
                 "LEFT JOIN categories c ON p.category_id = c.id WHERE p.id = ?";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        // 映射结果集到对象
    }
}
```

---

## 第10页：工具类详解

### 核心工具类

**1. DatabaseUtil - 数据库管理**
```java
// 功能：
// - 数据库连接池管理
// - 表结构自动初始化
// - 示例数据填充
public static Connection getConnection() {
    return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
}
```

**2. JwtUtil - JWT 令牌**
```java
// 功能：生成和解析 JWT Token
public static String generateToken(User user) {
    return Jwts.builder()
        .setSubject(String.valueOf(user.getId()))
        .claim("userId", user.getId())
        .claim("username", user.getUsername())
        .claim("role", user.getRole())
        .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000))
        .signWith(secretKey)
        .compact();
}
```

**3. PasswordUtil - 密码加密**
```java
// 使用 BCrypt 算法加密密码
public static String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt(10));
}
public static boolean checkPassword(String password, String hash) {
    return BCrypt.checkpw(password, hash);
}
```

---

## 第11页：前端架构详解

### Vue 3 项目结构

```
frontend/src/
├── api/            # API 接口封装
│   └── index.js    # Axios 实例 + API 模块
├── assets/         # 静态资源
│   └── main.css    # Tailwind + 自定义样式
├── components/     # 通用组件
│   ├── Navbar.vue      # 导航栏
│   ├── Footer.vue      # 页脚
│   ├── ProductCard.vue # 商品卡片
│   ├── RegionPicker.vue# 省市区选择器
│   ├── Loading.vue     # 加载动画
│   ├── Empty.vue       # 空状态
│   └── Toast.vue       # 消息提示
├── router/         # 路由配置
│   └── index.js    # Vue Router 配置
├── stores/         # 状态管理
│   ├── user.js     # 用户状态
│   ├── cart.js     # 购物车状态
│   └── toast.js    # 提示消息状态
├── views/          # 页面组件
│   └── *.vue       # 13 个页面
├── App.vue         # 根组件
└── main.js         # 入口文件
```

---

## 第12页：Vue 3 组合式 API

### Composition API 使用

**状态管理 - Pinia Store**
```javascript
// stores/user.js
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  
  async function login(credentials) {
    const res = await authApi.login(credentials)
    setAuth(res.data.token, res.data.user)
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    router.push('/login')
  }

  return { token, user, isLoggedIn, login, logout }
})
```

**组件示例**
```vue
<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)

onMounted(async () => {
  await userStore.fetchProfile()
})
</script>
```

---

## 第13页：API 请求封装

### Axios 实例与拦截器

**API 配置**
```javascript
// api/index.js
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

// 请求拦截器 - 添加 Token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器 - 统一错误处理
api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      // Token 过期，跳转登录
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

**API 模块化**
```javascript
export const productApi = {
  getList: (params) => api.get('/products', { params }),
  getDetail: (id) => api.get(`/products/${id}`),
  getHot: () => api.get('/products/hot'),
  getNew: () => api.get('/products/new')
}
```

---

## 第14页：路由与导航守卫

### Vue Router 配置

**路由定义**
```javascript
const routes = [
  { path: '/', name: 'home', component: () => import('@/views/Home.vue') },
  { path: '/products', name: 'products', component: () => import('@/views/Products.vue') },
  { path: '/product/:id', name: 'product-detail', component: () => import('@/views/ProductDetail.vue') },
  { path: '/cart', name: 'cart', component: () => import('@/views/Cart.vue'), meta: { requiresAuth: true } },
  { path: '/orders', name: 'orders', component: () => import('@/views/Orders.vue'), meta: { requiresAuth: true } },
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue') },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFound.vue') }
]
```

**导航守卫**
```javascript
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 需要登录的页面
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }
  
  // 已登录用户访问登录页
  if (to.name === 'login' && userStore.isLoggedIn) {
    next({ name: 'home' })
    return
  }
  
  next()
})
```

---

## 第15页：液态玻璃 UI 设计

### Liquid Glass 设计系统

**核心 CSS 变量**
```css
:root {
  --glass-bg: rgba(255, 255, 255, 0.1);
  --glass-border: rgba(255, 255, 255, 0.2);
  --glass-blur: 20px;
  --glass-radius: 16px;
}
```

**毛玻璃组件**
```css
.glass-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 1.5rem;
}

.glass-button-primary {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border-radius: 12px;
  padding: 0.75rem 1.5rem;
  transition: all 0.3s ease;
}

.glass-button-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 40px rgba(99, 102, 241, 0.4);
}
```

**动态背景**
```css
/* 渐变背景动画 */
.bg-gradient-animate {
  background: linear-gradient(-45deg, #0f0f23, #1a1a3e, #2d1b4e, #1e3a5f);
  background-size: 400% 400%;
  animation: gradient 15s ease infinite;
}
```

---

## 第16页：省市区选择器组件

### RegionPicker 组件实现

**功能特点**
- 三级联动下拉选择
- 网络数据源 + 本地备用数据
- 支持 v-model 双向绑定
- 支持编辑时数据回显

**技术实现**
```vue
<script setup>
// 数据源：CDN 托管的中国行政区划数据
const API_URL = 'https://cdn.jsdelivr.net/gh/modood/Administrative-divisions-of-China@master/dist/pca.json'

// 数据格式：{ 省: { 市: [区] } }
const regionData = ref({})

async function loadRegionData() {
  try {
    const response = await fetch(API_URL)
    regionData.value = await response.json()
    provinces.value = Object.keys(regionData.value)
  } catch {
    useStaticData() // 降级使用静态数据
  }
}

// 联动更新
function updateCities() {
  cities.value = Object.keys(regionData.value[selectedProvince.value] || {})
  selectedCity.value = ''
  districts.value = []
}
</script>
```

---

## 第17页：数据库设计

### SQLite 表结构

**ER 关系图（简化）**
```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   users     │       │   orders    │       │ order_items │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │←──┐   │ id (PK)     │←──────│ id (PK)     │
│ username    │   │   │ order_no    │       │ order_id(FK)│
│ password    │   └──→│ user_id(FK) │       │ product_id  │
│ email       │       │ total_amount│       │ quantity    │
│ balance     │       │ status      │       │ price       │
│ role        │       │ address_id  │       └─────────────┘
└─────────────┘       └─────────────┘
       │
       │ 1:N
       ▼
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  addresses  │       │ cart_items  │       │  favorites  │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │       │ id (PK)     │       │ id (PK)     │
│ user_id(FK) │       │ user_id(FK) │       │ user_id(FK) │
│ province    │       │ product_id  │       │ product_id  │
│ city        │       │ quantity    │       │ created_at  │
│ district    │       │ selected    │       └─────────────┘
│ detail      │       └─────────────┘
└─────────────┘
```

---

## 第18页：核心数据表

### 数据表详细设计

| 表名 | 记录数 | 主要字段 | 说明 |
|------|--------|----------|------|
| users | - | id, username, password, email, phone, balance, role, status | 用户信息，role=1为管理员 |
| categories | 8 | id, name, icon, parent_id, sort_order | 商品分类，支持多级 |
| products | 20 | id, name, description, price, original_price, stock, images, category_id, sales | 商品信息 |
| cart_items | - | id, user_id, product_id, quantity, selected | 购物车，selected控制勾选状态 |
| orders | - | id, order_no, user_id, total_amount, status, address_snapshot | 订单主表 |
| order_items | - | id, order_id, product_id, product_name, price, quantity | 订单明细 |
| addresses | - | id, user_id, receiver_name, phone, province, city, district, detail | 收货地址 |
| favorites | - | id, user_id, product_id | 收藏记录 |
| reviews | - | id, order_id, product_id, user_id, rating, content | 商品评价 |
| banners | 3 | id, title, image, link, sort_order | 首页轮播图 |
| settings | 1 | id, site_name, logo, description, contact_phone | 系统配置 |

---

## 第19页：用户端功能模块

### 13 个页面组件

| 页面 | 组件 | 功能描述 |
|------|------|----------|
| 首页 | Home.vue | 轮播图、分类入口、热销商品、新品推荐 |
| 商品列表 | Products.vue | 分类筛选、关键词搜索、排序、分页 |
| 商品详情 | ProductDetail.vue | 图片展示、价格信息、加购/收藏、评价列表 |
| 购物车 | Cart.vue | 商品列表、数量修改、全选、删除、结算 |
| 结算 | Checkout.vue | 地址选择、订单确认、提交订单 |
| 订单列表 | Orders.vue | 订单状态筛选、订单卡片、操作按钮 |
| 订单详情 | OrderDetail.vue | 状态展示、收货信息、商品明细、操作 |
| 个人中心 | User.vue | 个人信息、余额展示、修改密码 |
| 收货地址 | Addresses.vue | 地址列表、新增/编辑、省市区选择 |
| 收藏夹 | Favorites.vue | 收藏商品列表、取消收藏 |
| 登录 | Login.vue | 用户名/密码登录 |
| 注册 | Register.vue | 用户注册表单 |
| 404 | NotFound.vue | 页面不存在提示 |

---

## 第20页：购物车模块技术实现

### Cart 功能详解

**Pinia Store 状态管理**
```javascript
// stores/cart.js
export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  
  // 计算属性
  const selectedItems = computed(() => items.value.filter(i => i.selected))
  const totalPrice = computed(() => 
    selectedItems.value.reduce((sum, i) => sum + i.productPrice * i.quantity, 0)
  )
  const totalCount = computed(() => items.value.length)
  
  // 异步操作
  async function fetchCart() {
    const res = await cartApi.getList()
    items.value = res.data || []
  }
  
  async function updateQuantity(id, quantity) {
    await cartApi.update(id, { quantity })
    const item = items.value.find(i => i.id === id)
    if (item) item.quantity = quantity
  }
})
```

**后端 API**
```
GET    /api/cart          获取购物车列表
POST   /api/cart          添加商品到购物车
PUT    /api/cart/:id      更新数量/选中状态
DELETE /api/cart/:id      删除购物车项
DELETE /api/cart          清空购物车
```

---

## 第21页：订单模块技术实现

### Order 业务流程

**订单状态流转**
```
                    ┌──────────┐
                    │  创建订单  │
                    │  status=0 │
                    └─────┬────┘
                          │
          ┌───────────────┼───────────────┐
          ▼               │               ▼
    ┌──────────┐          │         ┌──────────┐
    │  取消订单  │          │         │  支付订单  │
    │  status=5 │          │         │  status=1 │
    └──────────┘          │         └─────┬────┘
                          │               │
                          │               ▼
                          │         ┌──────────┐
                          │         │  发货     │
                          │         │  status=2 │
                          │         └─────┬────┘
                          │               │
                          │               ▼
                          │         ┌──────────┐
                          │         │  确认收货  │
                          │         │  status=4 │
                          │         └──────────┘
```

**订单号生成**
```java
// 格式：年月日时分秒 + 6位随机数
String orderNo = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) 
                 + String.format("%06d", new Random().nextInt(1000000));
```

---

## 第22页：管理端功能模块

### Admin 后台管理系统

**8 个管理页面**

| 页面 | 组件 | 功能 |
|------|------|------|
| 登录 | Login.vue | 管理员登录（需 role=1） |
| 仪表盘 | Dashboard.vue | 数据统计、图表展示 |
| 商品管理 | Products.vue | 商品列表、搜索筛选、状态切换 |
| 商品表单 | ProductForm.vue | 新增/编辑商品 |
| 分类管理 | Categories.vue | 分类增删改、排序 |
| 订单管理 | Orders.vue | 订单列表、发货、状态管理 |
| 用户管理 | Users.vue | 用户列表、禁用/启用、余额调整 |
| 系统设置 | Settings.vue | 网站信息、轮播图管理 |

**权限控制**
```javascript
// 路由守卫检查管理员权限
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAdmin && user.role !== 1) {
    next('/login')
    return
  }
  next()
})
```

---

## 第23页：仪表盘数据统计

### Dashboard 实现

**统计数据 API**
```java
// DashboardServlet.java
{
  "userCount": 100,        // 用户总数
  "productCount": 50,      // 商品总数
  "orderCount": 200,       // 订单总数
  "totalSales": 50000.00,  // 总销售额
  "orderStats": {          // 订单状态统计
    "pending": 10,
    "paid": 20,
    "shipped": 15,
    "completed": 150
  },
  "recentOrders": [...],   // 最近订单
  "hotProducts": [...]     // 热销商品
}
```

**前端展示**
```vue
<template>
  <!-- 统计卡片 -->
  <div class="grid grid-cols-4 gap-6">
    <StatsCard title="用户总数" :value="stats.userCount" icon="users" />
    <StatsCard title="商品总数" :value="stats.productCount" icon="package" />
    <StatsCard title="订单总数" :value="stats.orderCount" icon="shopping-cart" />
    <StatsCard title="总销售额" :value="'¥' + stats.totalSales" icon="dollar" />
  </div>
</template>
```

---

## 第24页：安全机制

### 系统安全设计

**1. 身份认证 - JWT**
```
┌─────────┐     登录请求      ┌─────────┐
│  客户端  │ ───────────────→ │  服务端  │
│         │                   │         │
│         │ ←─────────────── │         │
│         │   返回 JWT Token  │         │
└─────────┘                   └─────────┘
     │
     │ 后续请求携带 Token
     │ Authorization: Bearer xxx
     ▼
┌─────────────────────────────────────────┐
│ AuthFilter 验证 Token                    │
│ - 检查签名有效性                          │
│ - 检查是否过期                            │
│ - 解析用户信息存入 request                │
└─────────────────────────────────────────┘
```

**2. 密码安全 - BCrypt**
```java
// 密码哈希（10轮 salt）
String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
// 验证密码
boolean valid = BCrypt.checkpw(inputPassword, storedHash);
```

**3. SQL 注入防护**
```java
// 使用 PreparedStatement 参数化查询
PreparedStatement ps = conn.prepareStatement(
    "SELECT * FROM users WHERE username = ?"
);
ps.setString(1, username);  // 参数绑定，防止注入
```

---

## 第25页：Docker 部署方案

### 容器化部署架构

**docker-compose.yml**
```yaml
services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    volumes:
      - ./data:/app/data          # 数据持久化
      - ./uploads:/app/uploads    # 上传文件
    environment:
      - DB_PATH=/app/data/lightshop.db
      - JWT_SECRET=your-secret-key
      - CORS_ORIGINS=http://localhost:5173,http://localhost:5174
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

**Dockerfile (多阶段构建)**
```dockerfile
# 构建阶段
FROM maven:3.9-eclipse-temurin-11 AS build
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

# 运行阶段
FROM tomcat:9-jdk11-temurin
COPY --from=build /target/lightshop.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
```

---

## 第26页：开发与调试

### 开发环境配置

**前端开发**
```bash
# 安装依赖
cd frontend && npm install

# 启动开发服务器（热重载）
npm run dev

# 构建生产版本
npm run build
```

**后端开发**
```bash
# 构建并启动 Docker 容器
docker-compose up -d backend

# 查看实时日志
docker-compose logs -f backend

# 重新构建（代码修改后）
docker-compose build backend && docker-compose up -d backend
```

**API 调试**
```bash
# 测试登录 API
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 测试商品列表
curl http://localhost:8080/api/products
```

---

## 第27页：性能优化

### 前后端性能优化策略

**前端优化**
| 策略 | 实现方式 |
|------|----------|
| 路由懒加载 | `() => import('@/views/Home.vue')` |
| 组件按需加载 | 动态 import 组件 |
| 图片懒加载 | `loading="lazy"` 属性 |
| 状态持久化 | localStorage 缓存用户信息 |
| 构建优化 | Vite 自动 tree-shaking |

**后端优化**
| 策略 | 实现方式 |
|------|----------|
| 数据库索引 | 对常用查询字段建立索引 |
| 连接复用 | SQLite 连接池 |
| 分页查询 | LIMIT/OFFSET 分页 |
| 缓存静态数据 | 分类等数据可缓存 |

**SQL 索引**
```sql
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
```

---

## 第28页：响应式设计

### 多端适配方案

**Tailwind CSS 断点**
```css
/* 移动优先设计 */
sm: 640px   /* 小屏手机 */
md: 768px   /* 平板 */
lg: 1024px  /* 桌面 */
xl: 1280px  /* 大屏 */
```

**响应式布局示例**
```vue
<template>
  <!-- 商品网格：移动1列，平板2列，桌面4列 -->
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
    <ProductCard v-for="product in products" :key="product.id" :product="product" />
  </div>
  
  <!-- 导航栏：移动端汉堡菜单，桌面端横向菜单 -->
  <nav class="hidden md:flex space-x-6">
    <RouterLink to="/">首页</RouterLink>
  </nav>
  <button class="md:hidden" @click="toggleMenu">☰</button>
</template>
```

---

## 第29页：技术亮点总结

### 项目技术亮点

**1. 现代化前端架构**
- Vue 3 Composition API
- Pinia 状态管理
- Vite 极速构建

**2. 安全可靠的后端**
- JWT 无状态认证
- BCrypt 密码加密
- PreparedStatement 防注入

**3. 优雅的 UI 设计**
- 液态玻璃视觉效果
- 流畅的动画过渡
- 完善的响应式适配

**4. 便捷的部署方案**
- Docker 容器化
- 一键启动
- 数据持久化

**5. 完整的电商功能**
- 商品浏览与搜索
- 购物车与订单
- 收货地址管理
- 后台管理系统

---

## 第30页：未来展望

### 可扩展方向

**功能扩展**
- 🔍 商品搜索 Elasticsearch
- 💬 在线客服 WebSocket
- 📊 数据分析 ECharts
- 🎫 优惠券系统
- 📦 物流跟踪集成
- 💳 对接真实支付 (支付宝/微信)

**技术升级**
- 后端迁移到 Spring Boot
- 数据库升级到 MySQL/PostgreSQL
- 引入 Redis 缓存
- 微服务架构拆分
- Kubernetes 容器编排

**性能优化**
- CDN 静态资源加速
- 数据库读写分离
- 接口缓存策略
- 前端 SSR/SSG

---

## 第31页：结束页

### 感谢观看

**LightShop B2C 商城系统**

- 📦 完整源码：GitHub Repository
- 📖 技术文档：README.md
- 🐳 快速部署：`docker-compose up -d`

**技术栈回顾**
```
Frontend: Vue 3 + Pinia + TailwindCSS + Vite
Backend:  Java 11 + Servlet + SQLite + JWT
Deploy:   Docker + Tomcat 9
```

**联系方式**
- 项目维护：LightShop Team
- 许可证：MIT License

---

## 附录：PPT 设计建议

### 视觉风格指南

**配色方案**
- 主色：#6366f1 (Indigo)
- 辅色：#8b5cf6 (Purple)  
- 强调：#ec4899 (Pink)
- 背景：#0f0f23 → #1a1a3e 渐变
- 文字：#ffffff / rgba(255,255,255,0.7)

**字体建议**
- 标题：思源黑体 Bold / SF Pro Display
- 正文：思源黑体 Regular / SF Pro Text
- 代码：JetBrains Mono / Fira Code

**图表风格**
- 使用毛玻璃效果的卡片容器
- 代码块使用深色背景 + 语法高亮
- 架构图使用圆角矩形 + 虚线连接
- 图标使用 Heroicons 或 Lucide Icons

**动画建议**
- 页面切换：淡入淡出
- 元素出现：从下往上滑入
- 图表：依次显示动画

