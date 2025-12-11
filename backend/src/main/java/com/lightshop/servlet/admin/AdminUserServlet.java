package com.lightshop.servlet.admin;

import com.google.gson.JsonObject;
import com.lightshop.dao.UserDao;
import com.lightshop.model.User;
import com.lightshop.util.JsonUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminUserServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // Get user list
                int page = getIntParam(request, "page", 1);
                int pageSize = getIntParam(request, "pageSize", 10);
                String keyword = request.getParameter("keyword");

                List<User> users = userDao.findAll(page, pageSize, keyword);
                int total = userDao.count(keyword);

                // Remove password from response
                List<Map<String, Object>> userList = new ArrayList<>();
                for (User user : users) {
                    userList.add(buildUserInfo(user));
                }

                JsonUtil.writePageData(response, userList, total, page, pageSize);
            } else {
                // Get single user
                int userId = Integer.parseInt(pathInfo.substring(1));
                User user = userDao.findById(userId);
                if (user == null) {
                    JsonUtil.writeError(response, 404, "用户不存在");
                    return;
                }
                JsonUtil.writeSuccess(response, buildUserInfo(user));
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
                JsonUtil.writeError(response, 400, "用户ID不能为空");
                return;
            }

            int userId = Integer.parseInt(pathInfo.substring(1));
            User user = userDao.findById(userId);
            if (user == null) {
                JsonUtil.writeError(response, 404, "用户不存在");
                return;
            }

            String body = JsonUtil.readRequestBody(request);
            JsonObject json = JsonUtil.parseJson(body);

            if (json.has("status")) {
                user.setStatus(json.get("status").getAsInt());
            }
            if (json.has("role")) {
                user.setRole(json.get("role").getAsInt());
            }
            if (json.has("balance")) {
                user.setBalance(json.get("balance").getAsDouble());
            }

            userDao.update(user);
            JsonUtil.writeSuccess(response, "更新成功", buildUserInfo(user));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("email", user.getEmail());
        info.put("phone", user.getPhone());
        info.put("avatar", user.getAvatar());
        info.put("balance", user.getBalance());
        info.put("role", user.getRole());
        info.put("status", user.getStatus());
        info.put("createdAt", user.getCreatedAt());
        return info;
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

