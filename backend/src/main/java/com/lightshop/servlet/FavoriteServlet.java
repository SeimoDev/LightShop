package com.lightshop.servlet;

import com.google.gson.JsonObject;
import com.lightshop.dao.FavoriteDao;
import com.lightshop.dao.ProductDao;
import com.lightshop.model.Favorite;
import com.lightshop.model.Product;
import com.lightshop.util.JsonUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteServlet extends HttpServlet {
    private final FavoriteDao favoriteDao = new FavoriteDao();
    private final ProductDao productDao = new ProductDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                JsonUtil.writeError(response, 401, "未登录");
                return;
            }

            String pathInfo = request.getPathInfo();

            // Check if product is favorited
            if (pathInfo != null && pathInfo.startsWith("/check/")) {
                int productId = Integer.parseInt(pathInfo.substring(7));
                boolean isFavorited = favoriteDao.exists(userId, productId);
                Map<String, Object> data = new HashMap<>();
                data.put("isFavorited", isFavorited);
                JsonUtil.writeSuccess(response, data);
                return;
            }

            int page = getIntParam(request, "page", 1);
            int pageSize = getIntParam(request, "pageSize", 10);

            List<Favorite> favorites = favoriteDao.findByUserId(userId, page, pageSize);
            int total = favoriteDao.countByUserId(userId);

            JsonUtil.writePageData(response, favorites, total, page, pageSize);
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

            // Check product exists
            Product product = productDao.findById(productId);
            if (product == null) {
                JsonUtil.writeError(response, 404, "商品不存在");
                return;
            }

            // Check if already favorited
            if (favoriteDao.exists(userId, productId)) {
                // Remove from favorites
                favoriteDao.delete(userId, productId);
                Map<String, Object> data = new HashMap<>();
                data.put("isFavorited", false);
                JsonUtil.writeSuccess(response, "取消收藏", data);
            } else {
                // Add to favorites
                Favorite favorite = new Favorite(userId, productId);
                favoriteDao.create(favorite);
                Map<String, Object> data = new HashMap<>();
                data.put("isFavorited", true);
                JsonUtil.writeSuccess(response, "收藏成功", data);
            }
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
                JsonUtil.writeError(response, 400, "商品ID不能为空");
                return;
            }

            int productId = Integer.parseInt(pathInfo.substring(1));
            favoriteDao.delete(userId, productId);

            JsonUtil.writeSuccess(response, "取消收藏成功", null);
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

