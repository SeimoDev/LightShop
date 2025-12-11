package com.lightshop.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lightshop.dao.*;
import com.lightshop.model.*;
import com.lightshop.util.JsonUtil;
import com.lightshop.util.StringUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrderServlet extends HttpServlet {
    private final OrderDao orderDao = new OrderDao();
    private final CartDao cartDao = new CartDao();
    private final ProductDao productDao = new ProductDao();
    private final AddressDao addressDao = new AddressDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                JsonUtil.writeError(response, 401, "未登录");
                return;
            }

            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // Get order list
                int page = getIntParam(request, "page", 1);
                int pageSize = getIntParam(request, "pageSize", 10);
                Integer status = getIntParamOrNull(request, "status");

                List<Order> orders = orderDao.findByUserId(userId, status, page, pageSize);
                int total = orderDao.countByUserId(userId, status);

                JsonUtil.writePageData(response, orders, total, page, pageSize);
            } else {
                // Get order detail
                String orderNo = pathInfo.substring(1);
                Order order = orderDao.findByOrderNo(orderNo);
                
                if (order == null) {
                    JsonUtil.writeError(response, 404, "订单不存在");
                    return;
                }

                if (order.getUserId() != userId) {
                    JsonUtil.writeError(response, 403, "无权查看此订单");
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                JsonUtil.writeError(response, 401, "未登录");
                return;
            }

            String body = JsonUtil.readRequestBody(request);
            JsonObject json = JsonUtil.parseJson(body);

            int addressId = json.get("addressId").getAsInt();
            String remark = json.has("remark") ? json.get("remark").getAsString() : "";
            boolean fromCart = json.has("fromCart") && json.get("fromCart").getAsBoolean();

            // Validate address
            Address address = addressDao.findById(addressId);
            if (address == null || address.getUserId() != userId) {
                JsonUtil.writeError(response, 400, "收货地址无效");
                return;
            }

            List<OrderItem> orderItems = new ArrayList<>();
            double totalAmount = 0;

            if (fromCart) {
                // Create from cart (selected items)
                List<CartItem> cartItems = cartDao.findSelectedByUserId(userId);
                if (cartItems.isEmpty()) {
                    JsonUtil.writeError(response, 400, "购物车中没有选中的商品");
                    return;
                }

                for (CartItem cartItem : cartItems) {
                    Product product = productDao.findById(cartItem.getProductId());
                    if (product == null || product.getStatus() != 1) {
                        JsonUtil.writeError(response, 400, "商品 " + cartItem.getProductName() + " 已下架");
                        return;
                    }
                    if (product.getStock() < cartItem.getQuantity()) {
                        JsonUtil.writeError(response, 400, "商品 " + product.getName() + " 库存不足");
                        return;
                    }

                    String productImage = product.getImages();
                    if (productImage != null && productImage.startsWith("[")) {
                        productImage = productImage.replace("[", "").replace("]", "").replace("\"", "").split(",")[0];
                    }

                    OrderItem orderItem = new OrderItem(
                        product.getId(),
                        product.getName(),
                        productImage,
                        product.getPrice(),
                        cartItem.getQuantity()
                    );
                    orderItems.add(orderItem);
                    totalAmount += orderItem.getSubtotal();
                }
            } else {
                // Create from direct buy
                JsonArray items = json.getAsJsonArray("items");
                for (int i = 0; i < items.size(); i++) {
                    JsonObject item = items.get(i).getAsJsonObject();
                    int productId = item.get("productId").getAsInt();
                    int quantity = item.get("quantity").getAsInt();

                    Product product = productDao.findById(productId);
                    if (product == null || product.getStatus() != 1) {
                        JsonUtil.writeError(response, 400, "商品已下架");
                        return;
                    }
                    if (product.getStock() < quantity) {
                        JsonUtil.writeError(response, 400, "商品 " + product.getName() + " 库存不足");
                        return;
                    }

                    String productImage = product.getImages();
                    if (productImage != null && productImage.startsWith("[")) {
                        productImage = productImage.replace("[", "").replace("]", "").replace("\"", "").split(",")[0];
                    }

                    OrderItem orderItem = new OrderItem(
                        product.getId(),
                        product.getName(),
                        productImage,
                        product.getPrice(),
                        quantity
                    );
                    orderItems.add(orderItem);
                    totalAmount += orderItem.getSubtotal();
                }
            }

            if (orderItems.isEmpty()) {
                JsonUtil.writeError(response, 400, "订单商品不能为空");
                return;
            }

            // Create order
            Order order = new Order();
            order.setOrderNo(StringUtil.generateOrderNo());
            order.setUserId(userId);
            order.setTotalAmount(totalAmount);
            order.setAddressId(addressId);
            order.setAddressSnapshot(JsonUtil.toJson(address));
            order.setRemark(remark);

            int orderId = orderDao.create(order, orderItems);
            if (orderId < 0) {
                JsonUtil.writeError(response, 500, "创建订单失败");
                return;
            }

            // Deduct stock
            for (OrderItem item : orderItems) {
                productDao.updateStock(item.getProductId(), -item.getQuantity());
            }

            // Clear cart if from cart
            if (fromCart) {
                cartDao.deleteSelected(userId);
            }

            order.setId(orderId);
            JsonUtil.writeSuccess(response, "创建订单成功", order);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                JsonUtil.writeError(response, 401, "未登录");
                return;
            }

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

            if (order.getUserId() != userId) {
                JsonUtil.writeError(response, 403, "无权操作此订单");
                return;
            }

            switch (action) {
                case "pay":
                    handlePay(order, userId, response);
                    break;
                case "cancel":
                    handleCancel(order, response);
                    break;
                case "confirm":
                    handleConfirm(order, response);
                    break;
                default:
                    JsonUtil.writeError(response, 400, "未知操作");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    private void handlePay(Order order, int userId, HttpServletResponse response) throws IOException {
        if (order.getStatus() != Order.STATUS_PENDING_PAYMENT) {
            JsonUtil.writeError(response, 400, "订单状态不允许支付");
            return;
        }

        // Mock payment - check balance
        User user = userDao.findById(userId);
        if (user.getBalance() < order.getTotalAmount()) {
            JsonUtil.writeError(response, 400, "余额不足");
            return;
        }

        // Deduct balance
        userDao.updateBalance(userId, -order.getTotalAmount());

        // Update order status
        orderDao.updatePaidAt(order.getId());

        // Update product sales
        for (OrderItem item : order.getItems()) {
            productDao.updateSales(item.getProductId(), item.getQuantity());
        }

        JsonUtil.writeSuccess(response, "支付成功", null);
    }

    private void handleCancel(Order order, HttpServletResponse response) throws IOException {
        if (order.getStatus() != Order.STATUS_PENDING_PAYMENT) {
            JsonUtil.writeError(response, 400, "订单状态不允许取消");
            return;
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            productDao.updateStock(item.getProductId(), item.getQuantity());
        }

        orderDao.updateStatus(order.getId(), Order.STATUS_CANCELLED);
        JsonUtil.writeSuccess(response, "取消成功", null);
    }

    private void handleConfirm(Order order, HttpServletResponse response) throws IOException {
        if (order.getStatus() != Order.STATUS_SHIPPED) {
            JsonUtil.writeError(response, 400, "订单状态不允许确认收货");
            return;
        }

        orderDao.updateCompletedAt(order.getId());
        JsonUtil.writeSuccess(response, "确认收货成功", null);
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

