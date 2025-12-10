package com.lightshop.servlet.admin;

import com.google.gson.JsonObject;
import com.lightshop.dao.ProductDao;
import com.lightshop.model.Product;
import com.lightshop.util.JsonUtil;
import com.lightshop.util.StringUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/api/admin/products", "/api/admin/products/*"})
public class AdminProductServlet extends HttpServlet {
    private final ProductDao productDao = new ProductDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // Get product list
                int page = getIntParam(request, "page", 1);
                int pageSize = getIntParam(request, "pageSize", 10);
                Integer categoryId = getIntParamOrNull(request, "categoryId");
                String keyword = request.getParameter("keyword");
                String sort = request.getParameter("sort");

                List<Product> products = productDao.findAll(page, pageSize, categoryId, keyword, sort, null);
                int total = productDao.count(categoryId, keyword, null);

                JsonUtil.writePageData(response, products, total, page, pageSize);
            } else {
                // Get single product
                int productId = Integer.parseInt(pathInfo.substring(1));
                Product product = productDao.findById(productId);
                if (product == null) {
                    JsonUtil.writeError(response, 404, "商品不存在");
                    return;
                }
                JsonUtil.writeSuccess(response, product);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String body = JsonUtil.readRequestBody(request);
            JsonObject json = JsonUtil.parseJson(body);

            String name = json.has("name") ? json.get("name").getAsString() : null;
            String description = json.has("description") ? json.get("description").getAsString() : "";
            double price = json.has("price") ? json.get("price").getAsDouble() : 0;
            double originalPrice = json.has("originalPrice") ? json.get("originalPrice").getAsDouble() : price;
            int stock = json.has("stock") ? json.get("stock").getAsInt() : 0;
            String images = json.has("images") ? json.get("images").toString() : "[]";
            int categoryId = json.has("categoryId") ? json.get("categoryId").getAsInt() : 0;
            int status = json.has("status") ? json.get("status").getAsInt() : 1;

            // Validation
            if (StringUtil.isEmpty(name)) {
                JsonUtil.writeError(response, 400, "商品名称不能为空");
                return;
            }
            if (price <= 0) {
                JsonUtil.writeError(response, 400, "商品价格必须大于0");
                return;
            }

            Product product = new Product(name, description, price, stock, categoryId);
            product.setOriginalPrice(originalPrice);
            product.setImages(images);
            product.setStatus(status);

            int productId = productDao.create(product);
            if (productId < 0) {
                JsonUtil.writeError(response, 500, "创建商品失败");
                return;
            }

            product.setId(productId);
            JsonUtil.writeSuccess(response, "创建成功", product);
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
                JsonUtil.writeError(response, 400, "商品ID不能为空");
                return;
            }

            int productId = Integer.parseInt(pathInfo.substring(1));
            Product product = productDao.findById(productId);
            if (product == null) {
                JsonUtil.writeError(response, 404, "商品不存在");
                return;
            }

            String body = JsonUtil.readRequestBody(request);
            JsonObject json = JsonUtil.parseJson(body);

            if (json.has("name")) product.setName(json.get("name").getAsString());
            if (json.has("description")) product.setDescription(json.get("description").getAsString());
            if (json.has("price")) product.setPrice(json.get("price").getAsDouble());
            if (json.has("originalPrice")) product.setOriginalPrice(json.get("originalPrice").getAsDouble());
            if (json.has("stock")) product.setStock(json.get("stock").getAsInt());
            if (json.has("images")) product.setImages(json.get("images").toString());
            if (json.has("categoryId")) product.setCategoryId(json.get("categoryId").getAsInt());
            if (json.has("status")) product.setStatus(json.get("status").getAsInt());

            productDao.update(product);
            JsonUtil.writeSuccess(response, "更新成功", product);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonUtil.writeError(response, 400, "商品ID不能为空");
                return;
            }

            int productId = Integer.parseInt(pathInfo.substring(1));
            productDao.delete(productId);

            JsonUtil.writeSuccess(response, "删除成功", null);
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

