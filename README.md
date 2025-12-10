# LightShop

一个现代化的 B2C 商城系统，采用液态玻璃 (Liquid Glass) UI 设计风格。

## 技术栈

### 后端
- **运行环境**: Java 11 + Tomcat 9 (Docker 容器化部署)
- **框架**: JSP/Servlet
- **数据库**: SQLite
- **认证**: JWT Token

### 前端
- **框架**: Vue 3 + Vite
- **状态管理**: Pinia
- **路由**: Vue Router
- **样式**: TailwindCSS
- **HTTP**: Axios

## 项目结构

```
LightShop/
├── backend/                    # JSP 后端项目
│   ├── src/main/java/         
│   │   └── com/lightshop/
│   │       ├── servlet/       # Servlet 控制器
│   │       ├── dao/           # 数据访问层
│   │       ├── model/         # 实体类
│   │       ├── util/          # 工具类
│   │       ├── filter/        # 过滤器
│   │       └── listener/      # 监听器
│   ├── src/main/webapp/       # Web 资源
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                   # 用户端 Vue3
│   ├── src/
│   │   ├── views/             # 页面组件
│   │   ├── components/        # 通用组件
│   │   ├── stores/            # Pinia 状态
│   │   ├── router/            # 路由配置
│   │   ├── api/               # API 接口
│   │   └── assets/            # 静态资源
│   └── package.json
├── admin/                      # 后台管理 Vue3
│   └── (结构同 frontend)
├── docker-compose.yml          # Docker 编排
├── init.sql                    # 示例数据
└── README.md
```

## 功能特性

### 用户端功能
- 🛒 **商品浏览**: 首页展示、分类浏览、商品搜索、商品详情
- ❤️ **商品收藏**: 收藏喜欢的商品，方便后续查看
- 🛍️ **购物车**: 添加商品、修改数量、全选/反选、删除商品
- 📦 **订单系统**: 创建订单、订单列表、订单详情、取消订单、确认收货
- 💳 **模拟支付**: 余额支付（用于演示）
- ⭐ **评价系统**: 订单评价、查看商品评价
- 👤 **用户中心**: 个人信息、收货地址管理、密码修改

### 后台管理功能
- 📊 **仪表盘**: 销售统计、订单统计、用户统计
- 📦 **商品管理**: 商品增删改查、上下架、库存管理
- 🏷️ **分类管理**: 分类增删改查、排序设置
- 📋 **订单管理**: 订单列表、发货处理、订单状态管理
- 👥 **用户管理**: 用户列表、禁用/启用用户、余额调整
- ⚙️ **系统设置**: 网站信息、轮播图管理

## 快速开始

### 环境要求
- Docker & Docker Compose
- Node.js 18+ (用于前端开发)

### 1. 启动后端服务

```bash
# 启动后端 Docker 容器
docker-compose up -d backend

# 查看日志
docker-compose logs -f backend
```

后端 API 将在 http://localhost:8080 运行

### 2. 启动前端开发服务器

**用户端**:
```bash
cd frontend
npm install
npm run dev
```
用户端将在 http://localhost:5173 运行

**管理端**:
```bash
cd admin
npm install
npm run dev -- --port 5174
```
管理端将在 http://localhost:5174 运行

### 3. 使用 Docker 启动所有服务（开发模式）

```bash
docker-compose --profile dev up -d
```

这将同时启动后端、用户端前端和管理端前端。

## 默认账号

| 角色 | 用户名 | 密码 | 备注 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 可访问后台管理 |

## API 文档

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/profile` - 获取个人信息
- `PUT /api/auth/profile` - 更新个人信息

### 商品相关
- `GET /api/products` - 商品列表
- `GET /api/products/:id` - 商品详情
- `GET /api/products/hot` - 热销商品
- `GET /api/products/new` - 新品推荐
- `GET /api/categories` - 分类列表

### 购物车
- `GET /api/cart` - 获取购物车
- `POST /api/cart` - 添加商品到购物车
- `PUT /api/cart/:id` - 更新购物车商品
- `DELETE /api/cart/:id` - 删除购物车商品

### 订单
- `GET /api/orders` - 订单列表
- `GET /api/orders/:orderNo` - 订单详情
- `POST /api/orders` - 创建订单
- `PUT /api/orders/:orderNo/pay` - 支付订单
- `PUT /api/orders/:orderNo/cancel` - 取消订单
- `PUT /api/orders/:orderNo/confirm` - 确认收货

### 收货地址
- `GET /api/addresses` - 地址列表
- `POST /api/addresses` - 添加地址
- `PUT /api/addresses/:id` - 更新地址
- `DELETE /api/addresses/:id` - 删除地址

### 收藏
- `GET /api/favorites` - 收藏列表
- `POST /api/favorites` - 添加/取消收藏
- `GET /api/favorites/check/:productId` - 检查是否已收藏

## 部署

### 生产环境部署

1. 构建前端项目:
```bash
# 用户端
cd frontend
npm run build

# 管理端
cd admin
npm run build
```

2. 配置反向代理 (Nginx):
```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 用户端
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # 管理端
    location /admin/ {
        alias /path/to/admin/dist/;
        try_files $uri $uri/ /admin/index.html;
    }

    # API 代理
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

3. 启动后端:
```bash
docker-compose up -d backend
```

## 环境变量

### 后端环境变量
| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| DB_PATH | SQLite 数据库路径 | /app/data/lightshop.db |
| UPLOAD_PATH | 文件上传路径 | /app/uploads |
| JWT_SECRET | JWT 密钥 | (必须配置) |
| CORS_ORIGINS | 允许的跨域来源 | http://localhost:5173,http://localhost:5174 |

### 前端环境变量
| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| VITE_API_BASE_URL | API 基础地址 | /api |

## UI 设计

采用液态玻璃 (Liquid Glass) 设计风格：

- **毛玻璃效果**: `backdrop-filter: blur(20px)`
- **半透明背景**: `background: rgba(255,255,255,0.15)`
- **柔和边框**: `border: 1px solid rgba(255,255,255,0.2)`
- **圆角设计**: `border-radius: 16px - 24px`
- **渐变背景**: 动态彩色渐变作为页面底色
- **流畅动画**: 页面切换和交互动画

## 响应式设计

支持多种设备尺寸：
- 移动端: < 768px
- 平板: 768px - 1024px
- 桌面: > 1024px

## 开发说明

### 添加新的 API 端点

1. 创建 DAO 类 (`backend/src/main/java/com/lightshop/dao/`)
2. 创建 Servlet (`backend/src/main/java/com/lightshop/servlet/`)
3. 在 `web.xml` 中注册 Servlet
4. 在前端 `api/index.js` 中添加 API 方法

### 添加新的页面

1. 在 `views/` 目录创建 Vue 组件
2. 在 `router/index.js` 中添加路由
3. 如需状态管理，在 `stores/` 目录创建 Store

## License

MIT License
