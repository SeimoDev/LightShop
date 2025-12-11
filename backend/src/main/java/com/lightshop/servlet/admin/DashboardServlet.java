package com.lightshop.servlet.admin;

import com.lightshop.dao.*;
import com.lightshop.util.JsonUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DashboardServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();
    private final ProductDao productDao = new ProductDao();
    private final OrderDao orderDao = new OrderDao();
    private final CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> data = new HashMap<>();

            // Basic statistics
            data.put("totalUsers", userDao.countTotal());
            data.put("totalProducts", productDao.countTotal());
            data.put("totalOrders", orderDao.countTotal());
            data.put("totalCategories", categoryDao.count());
            data.put("totalSales", orderDao.sumTotalAmount());

            // Order statistics by status
            Map<String, Integer> orderStats = new HashMap<>();
            orderStats.put("pending", orderDao.countAll(0, null)); // Pending payment
            orderStats.put("paid", orderDao.countAll(1, null));    // Paid
            orderStats.put("shipped", orderDao.countAll(2, null)); // Shipped
            orderStats.put("completed", orderDao.countAll(4, null)); // Completed
            orderStats.put("cancelled", orderDao.countAll(5, null)); // Cancelled
            data.put("orderStats", orderStats);

            // Recent orders
            data.put("recentOrders", orderDao.findAll(null, null, 1, 5));

            // Hot products
            data.put("hotProducts", productDao.findHotProducts(5));

            JsonUtil.writeSuccess(response, data);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }
}

