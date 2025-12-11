package com.lightshop.servlet.auth;

import com.google.gson.JsonObject;
import com.lightshop.dao.UserDao;
import com.lightshop.model.User;
import com.lightshop.util.JsonUtil;
import com.lightshop.util.PasswordUtil;
import com.lightshop.util.StringUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                JsonUtil.writeError(response, 401, "未登录");
                return;
            }

            User user = userDao.findById(userId);
            if (user == null) {
                JsonUtil.writeError(response, 404, "用户不存在");
                return;
            }

            JsonUtil.writeSuccess(response, buildUserInfo(user));
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

            User user = userDao.findById(userId);
            if (user == null) {
                JsonUtil.writeError(response, 404, "用户不存在");
                return;
            }

            String body = JsonUtil.readRequestBody(request);
            JsonObject json = JsonUtil.parseJson(body);

            // Update fields
            if (json.has("email") && !json.get("email").isJsonNull()) {
                String email = json.get("email").getAsString();
                if (StringUtil.isNotEmpty(email)) {
                    if (!StringUtil.isValidEmail(email)) {
                        JsonUtil.writeError(response, 400, "邮箱格式不正确");
                        return;
                    }
                    User existingUser = userDao.findByEmail(email);
                    if (existingUser != null && existingUser.getId() != userId) {
                        JsonUtil.writeError(response, 400, "邮箱已被使用");
                        return;
                    }
                    user.setEmail(email);
                }
            }

            if (json.has("phone") && !json.get("phone").isJsonNull()) {
                user.setPhone(json.get("phone").getAsString());
            }

            if (json.has("avatar") && !json.get("avatar").isJsonNull()) {
                user.setAvatar(json.get("avatar").getAsString());
            }

            // Handle password change
            if (json.has("oldPassword") && json.has("newPassword")) {
                String oldPassword = json.get("oldPassword").getAsString();
                String newPassword = json.get("newPassword").getAsString();

                if (StringUtil.isNotEmpty(oldPassword) && StringUtil.isNotEmpty(newPassword)) {
                    if (!PasswordUtil.checkPassword(oldPassword, user.getPassword())) {
                        JsonUtil.writeError(response, 400, "原密码错误");
                        return;
                    }

                    if (!StringUtil.isValidPassword(newPassword)) {
                        JsonUtil.writeError(response, 400, "新密码长度应为6-32位");
                        return;
                    }

                    userDao.updatePassword(userId, PasswordUtil.hashPassword(newPassword));
                }
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
        info.put("createdAt", user.getCreatedAt());
        return info;
    }
}

