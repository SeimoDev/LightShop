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

            // Insert default settings if not exists
            stmt.execute(
                "INSERT OR IGNORE INTO settings (id, site_name, description) " +
                "VALUES (1, 'LightShop', '优质商品，尽在LightShop')"
            );

            System.out.println("Database initialized successfully");
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
