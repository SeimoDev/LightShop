package com.lightshop.servlet;

import com.lightshop.util.JsonUtil;
import com.lightshop.util.StringUtil;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MultipartConfig(
    maxFileSize = 10 * 1024 * 1024,      // 10MB
    maxRequestSize = 20 * 1024 * 1024,   // 20MB
    fileSizeThreshold = 5 * 1024 * 1024  // 5MB
)
public class UploadServlet extends HttpServlet {
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private String uploadPath;

    @Override
    public void init() {
        uploadPath = System.getenv("UPLOAD_PATH");
        if (uploadPath == null || uploadPath.isEmpty()) {
            uploadPath = "/app/uploads";
        }
        
        // Create upload directory if not exists
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                JsonUtil.writeError(response, 401, "未登录");
                return;
            }

            List<String> uploadedUrls = new ArrayList<>();

            for (Part part : request.getParts()) {
                String contentDisposition = part.getHeader("content-disposition");
                if (contentDisposition == null || !contentDisposition.contains("filename=")) {
                    continue;
                }

                String filename = getFileName(part);
                if (filename == null || filename.isEmpty()) {
                    continue;
                }

                // Validate extension
                String extension = StringUtil.getFileExtension(filename);
                if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                    JsonUtil.writeError(response, 400, "不支持的文件格式，仅支持: " + String.join(", ", ALLOWED_EXTENSIONS));
                    return;
                }

                // Generate unique filename
                String newFilename = StringUtil.generateUUID() + "." + extension;
                
                // Create subdirectory by date
                String subDir = java.time.LocalDate.now().toString().replace("-", "/");
                Path dirPath = Paths.get(uploadPath, subDir);
                Files.createDirectories(dirPath);

                // Save file
                Path filePath = dirPath.resolve(newFilename);
                part.write(filePath.toString());

                // Generate URL
                String fileUrl = "/uploads/" + subDir + "/" + newFilename;
                uploadedUrls.add(fileUrl);
            }

            if (uploadedUrls.isEmpty()) {
                JsonUtil.writeError(response, 400, "没有上传文件");
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("urls", uploadedUrls);
            data.put("url", uploadedUrls.get(0)); // For single file upload

            JsonUtil.writeSuccess(response, "上传成功", data);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "上传失败: " + e.getMessage());
        }
    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String content : contentDisposition.split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}

