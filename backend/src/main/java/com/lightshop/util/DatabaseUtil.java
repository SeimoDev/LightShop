package com.lightshop.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    private static String dbPath;
    private static boolean initialized = false;

    public static void init(String path) {
        dbPath = path;
        try {
            Class.forName("org.sqlite.JDBC");
            if (!initialized) {
                initDatabase();
                initialized = true;
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dbPath == null) {
            dbPath = System.getenv("DB_PATH");
            if (dbPath == null) {
                dbPath = "./data/lightshop.db";
            }
        }
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    private static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Users table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    username VARCHAR(50) UNIQUE NOT NULL," +
                "    password VARCHAR(255) NOT NULL," +
                "    email VARCHAR(100) UNIQUE," +
                "    phone VARCHAR(20)," +
                "    avatar VARCHAR(255)," +
                "    balance DECIMAL(10,2) DEFAULT 0," +
                "    role INTEGER DEFAULT 0," +
                "    status INTEGER DEFAULT 1," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Categories table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS categories (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    name VARCHAR(50) NOT NULL," +
                "    icon VARCHAR(255)," +
                "    parent_id INTEGER DEFAULT 0," +
                "    sort_order INTEGER DEFAULT 0," +
                "    status INTEGER DEFAULT 1," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Products table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS products (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    name VARCHAR(200) NOT NULL," +
                "    description TEXT," +
                "    price DECIMAL(10,2) NOT NULL," +
                "    original_price DECIMAL(10,2)," +
                "    stock INTEGER DEFAULT 0," +
                "    images TEXT," +
                "    category_id INTEGER," +
                "    sales INTEGER DEFAULT 0," +
                "    status INTEGER DEFAULT 1," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    FOREIGN KEY (category_id) REFERENCES categories(id)" +
                ")"
            );

            // Cart items table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS cart_items (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    user_id INTEGER NOT NULL," +
                "    product_id INTEGER NOT NULL," +
                "    quantity INTEGER DEFAULT 1," +
                "    selected INTEGER DEFAULT 1," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    FOREIGN KEY (user_id) REFERENCES users(id)," +
                "    FOREIGN KEY (product_id) REFERENCES products(id)," +
                "    UNIQUE(user_id, product_id)" +
                ")"
            );

            // Orders table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS orders (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    order_no VARCHAR(50) UNIQUE NOT NULL," +
                "    user_id INTEGER NOT NULL," +
                "    total_amount DECIMAL(10,2) NOT NULL," +
                "    shipping_fee DECIMAL(10,2) DEFAULT 0," +
                "    status INTEGER DEFAULT 0," +
                "    address_id INTEGER," +
                "    address_snapshot TEXT," +
                "    remark TEXT," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    paid_at TIMESTAMP," +
                "    shipped_at TIMESTAMP," +
                "    completed_at TIMESTAMP," +
                "    FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")"
            );

            // Order items table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS order_items (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    order_id INTEGER NOT NULL," +
                "    product_id INTEGER NOT NULL," +
                "    product_name VARCHAR(200)," +
                "    product_image VARCHAR(255)," +
                "    price DECIMAL(10,2) NOT NULL," +
                "    quantity INTEGER NOT NULL," +
                "    FOREIGN KEY (order_id) REFERENCES orders(id)," +
                "    FOREIGN KEY (product_id) REFERENCES products(id)" +
                ")"
            );

            // Addresses table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS addresses (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    user_id INTEGER NOT NULL," +
                "    receiver_name VARCHAR(50) NOT NULL," +
                "    phone VARCHAR(20) NOT NULL," +
                "    province VARCHAR(50)," +
                "    city VARCHAR(50)," +
                "    district VARCHAR(50)," +
                "    detail_address VARCHAR(200)," +
                "    is_default INTEGER DEFAULT 0," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")"
            );

            // Favorites table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS favorites (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    user_id INTEGER NOT NULL," +
                "    product_id INTEGER NOT NULL," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    FOREIGN KEY (user_id) REFERENCES users(id)," +
                "    FOREIGN KEY (product_id) REFERENCES products(id)," +
                "    UNIQUE(user_id, product_id)" +
                ")"
            );

            // Reviews table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS reviews (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    order_id INTEGER NOT NULL," +
                "    order_item_id INTEGER NOT NULL," +
                "    user_id INTEGER NOT NULL," +
                "    product_id INTEGER NOT NULL," +
                "    rating INTEGER NOT NULL," +
                "    content TEXT," +
                "    images TEXT," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    FOREIGN KEY (order_id) REFERENCES orders(id)," +
                "    FOREIGN KEY (user_id) REFERENCES users(id)," +
                "    FOREIGN KEY (product_id) REFERENCES products(id)" +
                ")"
            );

            // Banners table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS banners (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    title VARCHAR(100)," +
                "    image VARCHAR(255) NOT NULL," +
                "    link VARCHAR(255)," +
                "    sort_order INTEGER DEFAULT 0," +
                "    status INTEGER DEFAULT 1," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Settings table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS settings (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    site_name VARCHAR(100) DEFAULT 'LightShop'," +
                "    logo VARCHAR(255)," +
                "    description TEXT," +
                "    keywords VARCHAR(255)," +
                "    contact_phone VARCHAR(50)," +
                "    contact_email VARCHAR(100)," +
                "    address VARCHAR(255)," +
                "    copyright VARCHAR(255)" +
                ")"
            );

            // Create indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_products_status ON products(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_cart_user ON cart_items(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_favorites_user ON favorites(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_reviews_product ON reviews(product_id)");

            // Insert default admin user if not exists (password: admin123)
            String adminPassword = PasswordUtil.hashPassword("admin123");
            stmt.execute(
                "INSERT OR IGNORE INTO users (id, username, password, email, role, status) " +
                "VALUES (1, 'admin', '" + adminPassword + "', 'admin@lightshop.com', 1, 1)"
            );

            // Insert default settings
            stmt.execute(
                "INSERT OR REPLACE INTO settings (id, site_name, logo, description, keywords, contact_phone, contact_email, address, copyright) " +
                "VALUES (1, 'LightShop', '/logo.png', '优质商品，尽在LightShop - 您的一站式购物平台', '商城,购物,电商,网购', '400-123-4567', 'support@lightshop.com', '北京市朝阳区xxx街道xxx号', '© 2024 LightShop. All rights reserved.')"
            );

            // Insert sample categories
            stmt.execute("INSERT OR IGNORE INTO categories (id, name, icon, parent_id, sort_order, status) VALUES (1, '手机数码', '📱', 0, 1, 1)");
            stmt.execute("INSERT OR IGNORE INTO categories (id, name, icon, parent_id, sort_order, status) VALUES (2, '电脑办公', '💻', 0, 2, 1)");
            stmt.execute("INSERT OR IGNORE INTO categories (id, name, icon, parent_id, sort_order, status) VALUES (3, '家用电器', '🏠', 0, 3, 1)");
            stmt.execute("INSERT OR IGNORE INTO categories (id, name, icon, parent_id, sort_order, status) VALUES (4, '服装鞋帽', '👕', 0, 4, 1)");
            stmt.execute("INSERT OR IGNORE INTO categories (id, name, icon, parent_id, sort_order, status) VALUES (5, '美妆护肤', '💄', 0, 5, 1)");
            stmt.execute("INSERT OR IGNORE INTO categories (id, name, icon, parent_id, sort_order, status) VALUES (6, '食品饮料', '🍎', 0, 6, 1)");
            stmt.execute("INSERT OR IGNORE INTO categories (id, name, icon, parent_id, sort_order, status) VALUES (7, '图书音像', '📚', 0, 7, 1)");
            stmt.execute("INSERT OR IGNORE INTO categories (id, name, icon, parent_id, sort_order, status) VALUES (8, '运动户外', '⚽', 0, 8, 1)");

            // Insert sample products
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (1, 'iPhone 15 Pro Max 256GB', '苹果最新旗舰手机，A17 Pro芯片，钛金属边框，超强性能。', 9999.00, 10999.00, 100, '[\"https://picsum.photos/seed/iphone15/400/400\"]', 1, 56, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (2, 'MacBook Pro 14英寸 M3 Pro', '专业级笔记本电脑，M3 Pro芯片，18GB统一内存，512GB固态硬盘。', 16999.00, 17999.00, 50, '[\"https://picsum.photos/seed/macbook/400/400\"]', 2, 32, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (3, '戴森 V15 Detect 无线吸尘器', '激光探测灰尘，智能除尘，长续航大吸力。', 5499.00, 5999.00, 80, '[\"https://picsum.photos/seed/dyson/400/400\"]', 3, 128, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (4, '优衣库 男士轻薄羽绒服', '轻盈保暖，90%白鸭绒填充，可收纳设计。', 499.00, 599.00, 200, '[\"https://picsum.photos/seed/uniqlo/400/400\"]', 4, 89, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (5, 'SK-II 神仙水 230ml', '护肤精华露，焕亮肌肤，细腻毛孔。', 1590.00, 1790.00, 150, '[\"https://picsum.photos/seed/skii/400/400\"]', 5, 234, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (6, '三只松鼠坚果大礼包 1428g', '每日坚果混合装，健康美味，送礼佳品。', 168.00, 198.00, 500, '[\"https://picsum.photos/seed/nuts/400/400\"]', 6, 567, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (7, '《认知觉醒》周岭 著', '开启自我改变的原动力，畅销心理励志书籍。', 45.00, 59.00, 300, '[\"https://picsum.photos/seed/book1/400/400\"]', 7, 189, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (8, '小米智能手表 S3', '全天候心率监测，120+运动模式，超长续航。', 999.00, 1199.00, 120, '[\"https://picsum.photos/seed/watch/400/400\"]', 1, 78, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (9, 'ThinkPad X1 Carbon Gen 11', '商务轻薄本，14英寸2.8K屏，Intel 13代酷睿。', 12999.00, 14999.00, 40, '[\"https://picsum.photos/seed/thinkpad/400/400\"]', 2, 23, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (10, '海尔对开门冰箱 515L', '风冷无霜，变频节能，智能控温。', 3999.00, 4599.00, 60, '[\"https://picsum.photos/seed/fridge/400/400\"]', 3, 45, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (11, '耐克 Air Force 1 白色经典', '经典复古篮球鞋，百搭舒适，永不过时。', 799.00, 899.00, 180, '[\"https://picsum.photos/seed/nike/400/400\"]', 4, 312, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (12, '兰蔻小黑瓶精华肌底液 100ml', '修护肌肤屏障，细腻肤质，紧致轮廓。', 1280.00, 1480.00, 90, '[\"https://picsum.photos/seed/lancome/400/400\"]', 5, 156, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (13, '农夫山泉 天然矿泉水 550ml*24瓶', '天然弱碱性水，甘甜可口，健康饮用。', 36.00, 42.00, 1000, '[\"https://picsum.photos/seed/water/400/400\"]', 6, 892, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (14, '《三体》全集 刘慈欣', '雨果奖科幻巨著，人类文明与宇宙的史诗。', 99.00, 128.00, 250, '[\"https://picsum.photos/seed/scifi/400/400\"]', 7, 423, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (15, '迪卡侬 跑步机 家用折叠', '静音减震，多档调速，LCD显示屏。', 1999.00, 2499.00, 45, '[\"https://picsum.photos/seed/treadmill/400/400\"]', 8, 67, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (16, 'AirPods Pro 2 USB-C版', '主动降噪，通透模式，个性化空间音频。', 1899.00, 1999.00, 200, '[\"https://picsum.photos/seed/airpods/400/400\"]', 1, 234, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (17, '华为 MatePad Pro 12.6英寸', '鸿蒙系统，120Hz OLED屏，手写笔支持。', 4999.00, 5499.00, 70, '[\"https://picsum.photos/seed/matepad/400/400\"]', 2, 89, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (18, '美的 变频空调挂机 1.5匹', '一级能效，智能WiFi控制，急速冷暖。', 2999.00, 3499.00, 100, '[\"https://picsum.photos/seed/aircond/400/400\"]', 3, 156, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (19, '阿迪达斯 三叶草卫衣', '经典三条杠设计，纯棉舒适，休闲百搭。', 599.00, 699.00, 150, '[\"https://picsum.photos/seed/adidas/400/400\"]', 4, 178, 1)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES (20, '雅诗兰黛 第七代小棕瓶 50ml', '抗初老精华，修护肌肤，提亮肤色。', 890.00, 1050.00, 85, '[\"https://picsum.photos/seed/estee/400/400\"]', 5, 234, 1)");

            // Insert sample banners
            stmt.execute("INSERT OR IGNORE INTO banners (id, title, image, link, sort_order, status) VALUES (1, '双十一狂欢节', 'https://picsum.photos/seed/banner1/1200/400', '/products?keyword=双十一', 1, 1)");
            stmt.execute("INSERT OR IGNORE INTO banners (id, title, image, link, sort_order, status) VALUES (2, '新品首发', 'https://picsum.photos/seed/banner2/1200/400', '/products?sort=newest', 2, 1)");
            stmt.execute("INSERT OR IGNORE INTO banners (id, title, image, link, sort_order, status) VALUES (3, '限时特惠', 'https://picsum.photos/seed/banner3/1200/400', '/products?sort=price_asc', 3, 1)");

            System.out.println("Database initialized with sample data successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static void close(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
}
