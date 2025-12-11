package com.lightshop.servlet.admin;

import com.google.gson.JsonObject;
import com.lightshop.dao.CategoryDao;
import com.lightshop.model.Category;
import com.lightshop.util.JsonUtil;
import com.lightshop.util.StringUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class AdminCategoryServlet extends HttpServlet {
    private final CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                List<Category> categories = categoryDao.findAllAdmin();
                JsonUtil.writeSuccess(response, categories);
            } else {
                int categoryId = Integer.parseInt(pathInfo.substring(1));
                Category category = categoryDao.findById(categoryId);
                if (category == null) {
                    JsonUtil.writeError(response, 404, "分类不存在");
                    return;
                }
                JsonUtil.writeSuccess(response, category);
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
            String icon = json.has("icon") ? json.get("icon").getAsString() : "";
            int parentId = json.has("parentId") ? json.get("parentId").getAsInt() : 0;
            int sortOrder = json.has("sortOrder") ? json.get("sortOrder").getAsInt() : 0;
            int status = json.has("status") ? json.get("status").getAsInt() : 1;

            if (StringUtil.isEmpty(name)) {
                JsonUtil.writeError(response, 400, "分类名称不能为空");
                return;
            }

            Category category = new Category(name, icon, parentId, sortOrder);
            category.setStatus(status);

            int categoryId = categoryDao.create(category);
            if (categoryId < 0) {
                JsonUtil.writeError(response, 500, "创建分类失败");
                return;
            }

            category.setId(categoryId);
            JsonUtil.writeSuccess(response, "创建成功", category);
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
                JsonUtil.writeError(response, 400, "分类ID不能为空");
                return;
            }

            int categoryId = Integer.parseInt(pathInfo.substring(1));
            Category category = categoryDao.findById(categoryId);
            if (category == null) {
                JsonUtil.writeError(response, 404, "分类不存在");
                return;
            }

            String body = JsonUtil.readRequestBody(request);
            JsonObject json = JsonUtil.parseJson(body);

            if (json.has("name")) category.setName(json.get("name").getAsString());
            if (json.has("icon")) category.setIcon(json.get("icon").getAsString());
            if (json.has("parentId")) category.setParentId(json.get("parentId").getAsInt());
            if (json.has("sortOrder")) category.setSortOrder(json.get("sortOrder").getAsInt());
            if (json.has("status")) category.setStatus(json.get("status").getAsInt());

            categoryDao.update(category);
            JsonUtil.writeSuccess(response, "更新成功", category);
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
                JsonUtil.writeError(response, 400, "分类ID不能为空");
                return;
            }

            int categoryId = Integer.parseInt(pathInfo.substring(1));
            categoryDao.delete(categoryId);

            JsonUtil.writeSuccess(response, "删除成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }
}

