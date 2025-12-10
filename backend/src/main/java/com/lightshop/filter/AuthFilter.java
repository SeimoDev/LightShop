package com.lightshop.filter;

import com.lightshop.util.JsonUtil;
import com.lightshop.util.JwtUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AuthFilter implements Filter {
    // Paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/health",
            "/api/products",
            "/api/categories",
            "/api/banners",
            "/api/settings",
            "/api/reviews"
    );

    // Paths that require admin role
    private static final List<String> ADMIN_PATHS = Arrays.asList(
            "/api/admin"
    );

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // Allow OPTIONS requests (preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        // Check if path is public
        if (isPublicPath(path, method)) {
            chain.doFilter(request, response);
            return;
        }

        // Get token from Authorization header
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            JsonUtil.writeError(httpResponse, 401, "未登录或登录已过期");
            return;
        }

        String token = authHeader.substring(7);

        // Validate token
        if (!JwtUtil.validateToken(token)) {
            JsonUtil.writeError(httpResponse, 401, "登录已过期，请重新登录");
            return;
        }

        // Check admin permission
        if (isAdminPath(path)) {
            if (!JwtUtil.isAdmin(token)) {
                JsonUtil.writeError(httpResponse, 403, "权限不足");
                return;
            }
        }

        // Set user info in request attributes
        Integer userId = JwtUtil.getUserId(token);
        String username = JwtUtil.getUsername(token);
        Integer role = JwtUtil.getRole(token);

        httpRequest.setAttribute("userId", userId);
        httpRequest.setAttribute("username", username);
        httpRequest.setAttribute("role", role);

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path, String method) {
        // GET requests to products, categories, reviews are public
        if ("GET".equalsIgnoreCase(method)) {
            for (String publicPath : PUBLIC_PATHS) {
                if (path.startsWith(publicPath)) {
                    return true;
                }
            }
        }
        
        // Auth endpoints are always public
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register")) {
            return true;
        }
        
        // Health check is public
        if (path.equals("/api/health")) {
            return true;
        }

        return false;
    }

    private boolean isAdminPath(String path) {
        for (String adminPath : ADMIN_PATHS) {
            if (path.startsWith(adminPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
    }
}

