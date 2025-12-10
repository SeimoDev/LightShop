package com.lightshop.servlet;

import com.google.gson.JsonObject;
import com.lightshop.dao.CartDao;
import com.lightshop.dao.ProductDao;
import com.lightshop.model.CartItem;
import com.lightshop.model.Product;
import com.lightshop.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/cart", "/api/cart/*"})
public class CartServlet extends HttpServlet {
    private final CartDao cartDao = new CartDao();
    private final ProductDao productDao = new ProductDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                JsonUtil.writeError(response, 401, "未登录");
                return;
            }

            List<CartItem> items = cartDao.findByUserId(userId);
            
            // Calculate totals
            double totalAmount = 0;
            int totalQuantity = 0;
            int selectedCount = 0;
            
            for (CartItem item : items) {
                totalQuantity += item.getQuantity();
                if (item.isSelected()) {
                    totalAmount += item.getSubtotal();
                    selectedCount++;
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("items", items);
            data.put("totalAmount", totalAmount);
            data.put("totalQuantity", totalQuantity);
            data.put("selectedCount", selectedCount);
            data.put("allSelected", items.size() > 0 && selectedCount == items.size());

            JsonUtil.writeSuccess(response, data);
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

            int productId = json.get("productId").getAsInt();
            int quantity = json.has("quantity") ? json.get("quantity").getAsInt() : 1;

            // Check product exists and is available
            Product product = productDao.findById(productId);
            if (product == null || product.getStatus() != 1) {
                JsonUtil.writeError(response, 400, "商品不存在或已下架");
                return;
            }

            // Check stock
            if (product.getStock() < quantity) {
                JsonUtil.writeError(response, 400, "库存不足");
                return;
            }

            // Check if already in cart
            CartItem existingItem = cartDao.findByUserAndProduct(userId, productId);
            if (existingItem != null) {
                // Update quantity
                int newQuantity = existingItem.getQuantity() + quantity;
                if (newQuantity > product.getStock()) {
                    JsonUtil.writeError(response, 400, "超出库存数量");
                    return;
                }
                cartDao.updateQuantity(existingItem.getId(), newQuantity);
            } else {
                // Add new item
                CartItem item = new CartItem(userId, productId, quantity);
                cartDao.create(item);
            }

            JsonUtil.writeSuccess(response, "添加成功", null);
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
            String body = JsonUtil.readRequestBody(request);
            JsonObject json = JsonUtil.parseJson(body);

            if (pathInfo != null && pathInfo.equals("/selectAll")) {
                // Select/unselect all
                boolean selected = json.get("selected").getAsBoolean();
                cartDao.updateAllSelected(userId, selected);
                JsonUtil.writeSuccess(response, "更新成功", null);
                return;
            }

            // Update single item
            int itemId = Integer.parseInt(pathInfo.substring(1));

            if (json.has("quantity")) {
                int quantity = json.get("quantity").getAsInt();
                if (quantity <= 0) {
                    cartDao.delete(itemId);
                } else {
                    cartDao.updateQuantity(itemId, quantity);
                }
            }

            if (json.has("selected")) {
                boolean selected = json.get("selected").getAsBoolean();
                cartDao.updateSelected(itemId, selected);
            }

            JsonUtil.writeSuccess(response, "更新成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                JsonUtil.writeError(response, 401, "未登录");
                return;
            }

            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Clear all cart items
                cartDao.clearCart(userId);
            } else if (pathInfo.equals("/selected")) {
                // Delete selected items
                cartDao.deleteSelected(userId);
            } else {
                // Delete single item
                int itemId = Integer.parseInt(pathInfo.substring(1));
                cartDao.delete(itemId);
            }

            JsonUtil.writeSuccess(response, "删除成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }
}

