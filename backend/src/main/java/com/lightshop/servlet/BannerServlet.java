package com.lightshop.servlet;

import com.lightshop.dao.BannerDao;
import com.lightshop.model.Banner;
import com.lightshop.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/api/banners", "/api/admin/banners", "/api/admin/banners/*"})
public class BannerServlet extends HttpServlet {
    private final BannerDao bannerDao = new BannerDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String uri = request.getRequestURI();
            
            if (uri.startsWith("/api/admin/")) {
                // Admin: get all banners
                List<Banner> banners = bannerDao.findAll();
                JsonUtil.writeSuccess(response, banners);
            } else {
                // Public: get active banners only
                List<Banner> banners = bannerDao.findActive();
                JsonUtil.writeSuccess(response, banners);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }
}

