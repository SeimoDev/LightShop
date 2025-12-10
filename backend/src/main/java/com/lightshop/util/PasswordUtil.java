package com.lightshop.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    private static final int LOG_ROUNDS = 10;

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS));
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}

