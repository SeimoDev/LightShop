package com.lightshop.servlet.auth;

import com.google.gson.JsonObject;
import com.lightshop.dao.UserDao;
import com.lightshop.model.User;
import com.lightshop.util.JsonUtil;
import com.lightshop.util.JwtUtil;
import com.lightshop.util.PasswordUtil;
import com.lightshop.util.StringUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/register")
public class RegisterServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String body = JsonUtil.readRequestBody(request);
            JsonObject json = JsonUtil.parseJson(body);

            String username = json.has("username") ? json.get("username").getAsString() : null;
            String password = json.has("password") ? json.get("password").getAsString() : null;
            String email = json.has("email") ? json.get("email").getAsString() : null;
            String phone = json.has("phone") ? json.get("phone").getAsString() : null;

            // Validation
            if (StringUtil.isEmpty(username)) {
                JsonUtil.writeError(response, 400, "用户名不能为空");
                return;
            }

            if (!StringUtil.isValidUsername(username)) {
                JsonUtil.writeError(response, 400, "用户名格式不正确（3-20位字母数字下划线）");
                return;
            }

            if (StringUtil.isEmpty(password)) {
                JsonUtil.writeError(response, 400, "密码不能为空");
                return;
            }

            if (!StringUtil.isValidPassword(password)) {
                JsonUtil.writeError(response, 400, "密码长度应为6-32位");
                return;
            }

            if (StringUtil.isNotEmpty(email) && !StringUtil.isValidEmail(email)) {
                JsonUtil.writeError(response, 400, "邮箱格式不正确");
                return;
            }

            // Check if username exists
            if (userDao.findByUsername(username) != null) {
                JsonUtil.writeError(response, 400, "用户名已存在");
                return;
            }

            // Check if email exists
            if (StringUtil.isNotEmpty(email) && userDao.findByEmail(email) != null) {
                JsonUtil.writeError(response, 400, "邮箱已被注册");
                return;
            }

            // Create user
            User user = new User(username, PasswordUtil.hashPassword(password), email);
            user.setPhone(phone);
            user.setBalance(100); // Give new users 100 initial balance for testing

            int userId = userDao.create(user);
            if (userId < 0) {
                JsonUtil.writeError(response, 500, "注册失败，请稍后重试");
                return;
            }

            user.setId(userId);

            // Generate token
            String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

            // Build response
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", buildUserInfo(user));

            JsonUtil.writeSuccess(response, "注册成功", data);
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

