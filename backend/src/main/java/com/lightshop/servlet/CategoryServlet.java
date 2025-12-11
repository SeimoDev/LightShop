package com.lightshop.servlet;

import com.lightshop.dao.CategoryDao;
import com.lightshop.model.Category;
import com.lightshop.util.JsonUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class CategoryServlet extends HttpServlet {
    private final CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all categories
                List<Category> categories = categoryDao.findAll();
                JsonUtil.writeSuccess(response, categories);
            } else {
                // Get category by ID
                int categoryId = Integer.parseInt(pathInfo.substring(1));
                Category category = categoryDao.findById(categoryId);
                if (category == null) {
                    JsonUtil.writeError(response, 404, "分类不存在");
                    return;
                }
                JsonUtil.writeSuccess(response, category);
            }
        } catch (NumberFormatException e) {
            JsonUtil.writeError(response, 400, "无效的分类ID");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }
}

