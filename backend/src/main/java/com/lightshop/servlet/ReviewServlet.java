package com.lightshop.servlet;

import com.google.gson.JsonObject;
import com.lightshop.dao.OrderDao;
import com.lightshop.dao.OrderItemDao;
import com.lightshop.dao.ReviewDao;
import com.lightshop.model.Order;
import com.lightshop.model.OrderItem;
import com.lightshop.model.Review;
import com.lightshop.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/api/reviews", "/api/reviews/*"})
public class ReviewServlet extends HttpServlet {
    private final ReviewDao reviewDao = new ReviewDao();
    private final OrderDao orderDao = new OrderDao();
    private final OrderItemDao orderItemDao = new OrderItemDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // Get reviews by product
                String productIdStr = request.getParameter("productId");
                if (productIdStr == null) {
                    JsonUtil.writeError(response, 400, "商品ID不能为空");
                    return;
                }

                int productId = Integer.parseInt(productIdStr);
                int page = getIntParam(request, "page", 1);
                int pageSize = getIntParam(request, "pageSize", 10);

                List<Review> reviews = reviewDao.findByProductId(productId, page, pageSize);
                int total = reviewDao.countByProductId(productId);

                JsonUtil.writePageData(response, reviews, total, page, pageSize);
            } else {
                // Check if order item has been reviewed
                if (pathInfo.startsWith("/check/")) {
                    int orderItemId = Integer.parseInt(pathInfo.substring(7));
                    Review review = reviewDao.findByOrderItemId(orderItemId);
                    JsonUtil.writeSuccess(response, review);
                }
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

            int orderId = json.get("orderId").getAsInt();
            int orderItemId = json.get("orderItemId").getAsInt();
            int rating = json.get("rating").getAsInt();
            String content = json.has("content") ? json.get("content").getAsString() : "";
            String images = json.has("images") ? json.get("images").toString() : null;

            // Validate rating
            if (rating < 1 || rating > 5) {
                JsonUtil.writeError(response, 400, "评分必须在1-5之间");
                return;
            }

            // Check order exists and belongs to user
            Order order = orderDao.findById(orderId);
            if (order == null) {
                JsonUtil.writeError(response, 404, "订单不存在");
                return;
            }
            if (order.getUserId() != userId) {
                JsonUtil.writeError(response, 403, "无权评价此订单");
                return;
            }
            if (order.getStatus() != Order.STATUS_COMPLETED) {
                JsonUtil.writeError(response, 400, "订单未完成，无法评价");
                return;
            }

            // Check order item exists
            OrderItem orderItem = orderItemDao.findById(orderItemId);
            if (orderItem == null || orderItem.getOrderId() != orderId) {
                JsonUtil.writeError(response, 404, "订单项不存在");
                return;
            }

            // Check if already reviewed
            if (reviewDao.findByOrderItemId(orderItemId) != null) {
                JsonUtil.writeError(response, 400, "已评价过此商品");
                return;
            }

            // Create review
            Review review = new Review(orderId, orderItemId, userId, orderItem.getProductId(), rating, content);
            review.setImages(images);

            int reviewId = reviewDao.create(review);
            if (reviewId < 0) {
                JsonUtil.writeError(response, 500, "评价失败");
                return;
            }

            JsonUtil.writeSuccess(response, "评价成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
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
}

