package com.lightshop.servlet.auth;

import com.google.gson.JsonObject;
import com.lightshop.dao.UserDao;
import com.lightshop.model.User;
import com.lightshop.util.JsonUtil;
import com.lightshop.util.JwtUtil;
import com.lightshop.util.PasswordUtil;
import com.lightshop.util.StringUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String body = JsonUtil.readRequestBody(request);
            JsonObject json = JsonUtil.parseJson(body);

            String username = json.has("username") ? json.get("username").getAsString() : null;
            String password = json.has("password") ? json.get("password").getAsString() : null;

            // Validation
            if (StringUtil.isEmpty(username) || StringUtil.isEmpty(password)) {
                JsonUtil.writeError(response, 400, "用户名和密码不能为空");
                return;
            }

            // Find user
            User user = userDao.findByUsername(username);
            if (user == null) {
                JsonUtil.writeError(response, 400, "用户名或密码错误");
                return;
            }

            // Check password
            if (!PasswordUtil.checkPassword(password, user.getPassword())) {
                JsonUtil.writeError(response, 400, "用户名或密码错误");
                return;
            }

            // Check status
            if (user.getStatus() != 1) {
                JsonUtil.writeError(response, 400, "账号已被禁用");
                return;
            }

            // Generate token
            String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

            // Build response
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", buildUserInfo(user));

            JsonUtil.writeSuccess(response, "登录成功", data);
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
        return info;
    }
}

