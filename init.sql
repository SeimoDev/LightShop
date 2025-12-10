-- LightShop Database Initialization Script
-- This script contains sample data for testing

-- Insert sample categories
INSERT OR IGNORE INTO categories (id, name, icon, parent_id, sort_order, status) VALUES
(1, '手机数码', '📱', 0, 1, 1),
(2, '电脑办公', '💻', 0, 2, 1),
(3, '家用电器', '🏠', 0, 3, 1),
(4, '服装鞋帽', '👕', 0, 4, 1),
(5, '美妆护肤', '💄', 0, 5, 1),
(6, '食品饮料', '🍎', 0, 6, 1),
(7, '图书音像', '📚', 0, 7, 1),
(8, '运动户外', '⚽', 0, 8, 1);

-- Insert sample products
INSERT OR IGNORE INTO products (id, name, description, price, original_price, stock, images, category_id, sales, status) VALUES
(1, 'iPhone 15 Pro Max 256GB', '苹果最新旗舰手机，A17 Pro芯片，钛金属边框，超强性能。', 9999.00, 10999.00, 100, '["https://picsum.photos/seed/iphone15/400/400"]', 1, 56, 1),
(2, 'MacBook Pro 14英寸 M3 Pro', '专业级笔记本电脑，M3 Pro芯片，18GB统一内存，512GB固态硬盘。', 16999.00, 17999.00, 50, '["https://picsum.photos/seed/macbook/400/400"]', 2, 32, 1),
(3, '戴森 V15 Detect 无线吸尘器', '激光探测灰尘，智能除尘，长续航大吸力。', 5499.00, 5999.00, 80, '["https://picsum.photos/seed/dyson/400/400"]', 3, 128, 1),
(4, '优衣库 男士轻薄羽绒服', '轻盈保暖，90%白鸭绒填充，可收纳设计。', 499.00, 599.00, 200, '["https://picsum.photos/seed/uniqlo/400/400"]', 4, 89, 1),
(5, 'SK-II 神仙水 230ml', '护肤精华露，焕亮肌肤，细腻毛孔。', 1590.00, 1790.00, 150, '["https://picsum.photos/seed/skii/400/400"]', 5, 234, 1),
(6, '三只松鼠坚果大礼包 1428g', '每日坚果混合装，健康美味，送礼佳品。', 168.00, 198.00, 500, '["https://picsum.photos/seed/nuts/400/400"]', 6, 567, 1),
(7, '《认知觉醒》周岭 著', '开启自我改变的原动力，畅销心理励志书籍。', 45.00, 59.00, 300, '["https://picsum.photos/seed/book1/400/400"]', 7, 189, 1),
(8, '小米智能手表 S3', '全天候心率监测，120+运动模式，超长续航。', 999.00, 1199.00, 120, '["https://picsum.photos/seed/watch/400/400"]', 1, 78, 1),
(9, 'ThinkPad X1 Carbon Gen 11', '商务轻薄本，14英寸2.8K屏，Intel 13代酷睿。', 12999.00, 14999.00, 40, '["https://picsum.photos/seed/thinkpad/400/400"]', 2, 23, 1),
(10, '海尔对开门冰箱 515L', '风冷无霜，变频节能，智能控温。', 3999.00, 4599.00, 60, '["https://picsum.photos/seed/fridge/400/400"]', 3, 45, 1),
(11, '耐克 Air Force 1 白色经典', '经典复古篮球鞋，百搭舒适，永不过时。', 799.00, 899.00, 180, '["https://picsum.photos/seed/nike/400/400"]', 4, 312, 1),
(12, '兰蔻小黑瓶精华肌底液 100ml', '修护肌肤屏障，细腻肤质，紧致轮廓。', 1280.00, 1480.00, 90, '["https://picsum.photos/seed/lancome/400/400"]', 5, 156, 1),
(13, '农夫山泉 天然矿泉水 550ml*24瓶', '天然弱碱性水，甘甜可口，健康饮用。', 36.00, 42.00, 1000, '["https://picsum.photos/seed/water/400/400"]', 6, 892, 1),
(14, '《三体》全集 刘慈欣', '雨果奖科幻巨著，人类文明与宇宙的史诗。', 99.00, 128.00, 250, '["https://picsum.photos/seed/scifi/400/400"]', 7, 423, 1),
(15, '迪卡侬 跑步机 家用折叠', '静音减震，多档调速，LCD显示屏。', 1999.00, 2499.00, 45, '["https://picsum.photos/seed/treadmill/400/400"]', 8, 67, 1),
(16, 'AirPods Pro 2 USB-C版', '主动降噪，通透模式，个性化空间音频。', 1899.00, 1999.00, 200, '["https://picsum.photos/seed/airpods/400/400"]', 1, 234, 1),
(17, '华为 MatePad Pro 12.6英寸', '鸿蒙系统，120Hz OLED屏，手写笔支持。', 4999.00, 5499.00, 70, '["https://picsum.photos/seed/matepad/400/400"]', 2, 89, 1),
(18, '美的 变频空调挂机 1.5匹', '一级能效，智能WiFi控制，急速冷暖。', 2999.00, 3499.00, 100, '["https://picsum.photos/seed/aircond/400/400"]', 3, 156, 1),
(19, '阿迪达斯 三叶草卫衣', '经典三条杠设计，纯棉舒适，休闲百搭。', 599.00, 699.00, 150, '["https://picsum.photos/seed/adidas/400/400"]', 4, 178, 1),
(20, '雅诗兰黛 第七代小棕瓶 50ml', '抗初老精华，修护肌肤，提亮肤色。', 890.00, 1050.00, 85, '["https://picsum.photos/seed/estee/400/400"]', 5, 234, 1);

-- Insert sample banners
INSERT OR IGNORE INTO banners (id, title, image, link, sort_order, status) VALUES
(1, '双十一狂欢节', 'https://picsum.photos/seed/banner1/1200/400', '/products?keyword=双十一', 1, 1),
(2, '新品首发', 'https://picsum.photos/seed/banner2/1200/400', '/products?sort=newest', 2, 1),
(3, '限时特惠', 'https://picsum.photos/seed/banner3/1200/400', '/products?sort=price_asc', 3, 1);

-- Insert default settings
INSERT OR REPLACE INTO settings (id, site_name, logo, description, keywords, contact_phone, contact_email, address, copyright) VALUES
(1, 'LightShop', '/logo.png', '优质商品，尽在LightShop - 您的一站式购物平台', '商城,购物,电商,网购', '400-123-4567', 'support@lightshop.com', '北京市朝阳区xxx街道xxx号', '© 2024 LightShop. All rights reserved.');

