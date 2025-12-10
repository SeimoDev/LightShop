package com.lightshop.listener;

import com.lightshop.util.DatabaseUtil;
import com.lightshop.util.JwtUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("==============================================");
        System.out.println("   LightShop Application Starting...          ");
        System.out.println("==============================================");

        // Get database path from environment or context
        String dbPath = System.getenv("DB_PATH");
        if (dbPath == null || dbPath.isEmpty()) {
            dbPath = sce.getServletContext().getInitParameter("dbPath");
        }
        if (dbPath == null || dbPath.isEmpty()) {
            dbPath = "/app/data/lightshop.db";
        }

        System.out.println("Database path: " + dbPath);

        // Initialize database
        try {
            DatabaseUtil.init(dbPath);
            System.out.println("Database initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }

        // Initialize JWT
        String jwtSecret = System.getenv("JWT_SECRET");
        JwtUtil.init(jwtSecret);
        System.out.println("JWT initialized successfully");

        System.out.println("==============================================");
        System.out.println("   LightShop Application Started!             ");
        System.out.println("==============================================");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("LightShop Application Shutting Down...");
    }
}

