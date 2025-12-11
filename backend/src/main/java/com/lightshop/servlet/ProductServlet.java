package com.lightshop.servlet;

import com.lightshop.dao.ProductDao;
import com.lightshop.dao.ReviewDao;
import com.lightshop.model.Product;
import com.lightshop.util.JsonUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductServlet extends HttpServlet {
    private final ProductDao productDao = new ProductDao();
    private final ReviewDao reviewDao = new ReviewDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // Get product list
                getProductList(request, response);
            } else if (pathInfo.equals("/hot")) {
                // Get hot products
                int limit = getIntParam(request, "limit", 8);
                List<Product> products = productDao.findHotProducts(limit);
                JsonUtil.writeSuccess(response, products);
            } else if (pathInfo.equals("/new")) {
                // Get new products
                int limit = getIntParam(request, "limit", 8);
                List<Product> products = productDao.findNewProducts(limit);
                JsonUtil.writeSuccess(response, products);
            } else if (pathInfo.equals("/recommend")) {
                // Get recommended products
                int limit = getIntParam(request, "limit", 8);
                List<Product> products = productDao.findRecommended(limit);
                JsonUtil.writeSuccess(response, products);
            } else {
                // Get product by ID
                getProductDetail(pathInfo, response);
            }
        } catch (NumberFormatException e) {
            JsonUtil.writeError(response, 400, "无效的商品ID");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    private void getProductList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int page = getIntParam(request, "page", 1);
        int pageSize = getIntParam(request, "pageSize", 12);
        Integer categoryId = getIntParamOrNull(request, "categoryId");
        String keyword = request.getParameter("keyword");
        String sort = request.getParameter("sort");

        List<Product> products = productDao.findAll(page, pageSize, categoryId, keyword, sort, true);
        int total = productDao.count(categoryId, keyword, true);

        JsonUtil.writePageData(response, products, total, page, pageSize);
    }

    private void getProductDetail(String pathInfo, HttpServletResponse response) throws IOException {
        int productId = Integer.parseInt(pathInfo.substring(1));
        Product product = productDao.findById(productId);

        if (product == null || product.getStatus() != 1) {
            JsonUtil.writeError(response, 404, "商品不存在或已下架");
            return;
        }

        // Add review info
        Map<String, Object> data = new HashMap<>();
        data.put("product", product);
        data.put("reviewCount", reviewDao.countByProductId(productId));
        data.put("averageRating", reviewDao.getAverageRating(productId));

        JsonUtil.writeSuccess(response, data);
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

