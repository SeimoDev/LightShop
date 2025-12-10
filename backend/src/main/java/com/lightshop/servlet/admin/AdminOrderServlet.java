package com.lightshop.servlet.admin;

import com.google.gson.JsonObject;
import com.lightshop.dao.OrderDao;
import com.lightshop.dao.ProductDao;
import com.lightshop.dao.UserDao;
import com.lightshop.model.Order;
import com.lightshop.model.OrderItem;
import com.lightshop.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/api/admin/orders", "/api/admin/orders/*"})
public class AdminOrderServlet extends HttpServlet {
    private final OrderDao orderDao = new OrderDao();
    private final ProductDao productDao = new ProductDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // Get order list
                int page = getIntParam(request, "page", 1);
                int pageSize = getIntParam(request, "pageSize", 10);
                Integer status = getIntParamOrNull(request, "status");
                String keyword = request.getParameter("keyword");

                List<Order> orders = orderDao.findAll(status, keyword, page, pageSize);
                int total = orderDao.countAll(status, keyword);

                JsonUtil.writePageData(response, orders, total, page, pageSize);
            } else {
                // Get single order
                String orderNo = pathInfo.substring(1);
                Order order = orderDao.findByOrderNo(orderNo);
                if (order == null) {
                    JsonUtil.writeError(response, 404, "订单不存在");
                    return;
                }
                JsonUtil.writeSuccess(response, order);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonUtil.writeError(response, 400, "订单号不能为空");
                return;
            }

            String[] parts = pathInfo.substring(1).split("/");
            String orderNo = parts[0];
            String action = parts.length > 1 ? parts[1] : "";

            Order order = orderDao.findByOrderNo(orderNo);
            if (order == null) {
                JsonUtil.writeError(response, 404, "订单不存在");
                return;
            }

            switch (action) {
                case "ship":
                    handleShip(order, response);
                    break;
                case "refund":
                    handleRefund(order, response);
                    break;
                case "cancel":
                    handleCancel(order, response);
                    break;
                default:
                    // Update status directly
                    String body = JsonUtil.readRequestBody(request);
                    JsonObject json = JsonUtil.parseJson(body);
                    if (json.has("status")) {
                        int status = json.get("status").getAsInt();
                        orderDao.updateStatus(order.getId(), status);
                        JsonUtil.writeSuccess(response, "更新成功", null);
                    } else {
                        JsonUtil.writeError(response, 400, "缺少状态参数");
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    private void handleShip(Order order, HttpServletResponse response) throws IOException {
        if (order.getStatus() != Order.STATUS_PAID) {
            JsonUtil.writeError(response, 400, "订单状态不允许发货");
            return;
        }

        orderDao.updateShippedAt(order.getId());
        JsonUtil.writeSuccess(response, "发货成功", null);
    }

    private void handleRefund(Order order, HttpServletResponse response) throws IOException {
        if (order.getStatus() != Order.STATUS_PAID && order.getStatus() != Order.STATUS_SHIPPED) {
            JsonUtil.writeError(response, 400, "订单状态不允许退款");
            return;
        }

        // Refund to user balance
        userDao.updateBalance(order.getUserId(), order.getTotalAmount());

        // Restore stock
        for (OrderItem item : order.getItems()) {
            productDao.updateStock(item.getProductId(), item.getQuantity());
            productDao.updateSales(item.getProductId(), -item.getQuantity());
        }

        orderDao.updateStatus(order.getId(), Order.STATUS_REFUNDED);
        JsonUtil.writeSuccess(response, "退款成功", null);
    }

    private void handleCancel(Order order, HttpServletResponse response) throws IOException {
        if (order.getStatus() > Order.STATUS_PAID) {
            JsonUtil.writeError(response, 400, "订单状态不允许取消");
            return;
        }

        // If paid, refund
        if (order.getStatus() == Order.STATUS_PAID) {
            userDao.updateBalance(order.getUserId(), order.getTotalAmount());
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            productDao.updateStock(item.getProductId(), item.getQuantity());
        }

        orderDao.updateStatus(order.getId(), Order.STATUS_CANCELLED);
        JsonUtil.writeSuccess(response, "取消成功", null);
    }

    private int getIntParam(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Integer getIntParamOrNull(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

