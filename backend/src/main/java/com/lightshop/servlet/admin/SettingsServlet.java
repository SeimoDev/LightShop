package com.lightshop.servlet.admin;

import com.google.gson.JsonObject;
import com.lightshop.dao.BannerDao;
import com.lightshop.dao.SettingsDao;
import com.lightshop.model.Banner;
import com.lightshop.model.Settings;
import com.lightshop.util.JsonUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsServlet extends HttpServlet {
    private final SettingsDao settingsDao = new SettingsDao();
    private final BannerDao bannerDao = new BannerDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Settings settings = settingsDao.get();
            JsonUtil.writeSuccess(response, settings);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String body = JsonUtil.readRequestBody(request);
            JsonObject json = JsonUtil.parseJson(body);

            Settings settings = settingsDao.get();

            if (json.has("siteName")) settings.setSiteName(json.get("siteName").getAsString());
            if (json.has("logo")) settings.setLogo(json.get("logo").getAsString());
            if (json.has("description")) settings.setDescription(json.get("description").getAsString());
            if (json.has("keywords")) settings.setKeywords(json.get("keywords").getAsString());
            if (json.has("contactPhone")) settings.setContactPhone(json.get("contactPhone").getAsString());
            if (json.has("contactEmail")) settings.setContactEmail(json.get("contactEmail").getAsString());
            if (json.has("address")) settings.setAddress(json.get("address").getAsString());
            if (json.has("copyright")) settings.setCopyright(json.get("copyright").getAsString());

            settingsDao.update(settings);
            JsonUtil.writeSuccess(response, "更新成功", settings);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String uri = request.getRequestURI();
            
            // Handle banner operations
            if (uri.contains("/banners")) {
                String body = JsonUtil.readRequestBody(request);
                JsonObject json = JsonUtil.parseJson(body);

                String title = json.has("title") ? json.get("title").getAsString() : "";
                String image = json.has("image") ? json.get("image").getAsString() : "";
                String link = json.has("link") ? json.get("link").getAsString() : "";
                int sortOrder = json.has("sortOrder") ? json.get("sortOrder").getAsInt() : 0;
                int status = json.has("status") ? json.get("status").getAsInt() : 1;

                Banner banner = new Banner(title, image, link, sortOrder);
                banner.setStatus(status);

                int bannerId = bannerDao.create(banner);
                banner.setId(bannerId);

                JsonUtil.writeSuccess(response, "创建成功", banner);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo != null && pathInfo.startsWith("/banners/")) {
                int bannerId = Integer.parseInt(pathInfo.substring(9));
                bannerDao.delete(bannerId);
                JsonUtil.writeSuccess(response, "删除成功", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }
}

