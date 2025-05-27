package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    private static final Logger logger = LogManager.getLogger(PasswordUtil.class);

    public static String hashPassword(String password) {
        try {
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            logger.info("Password hashed successfully");
            return hashed;
        } catch (Exception e) {
            logger.error("Error hashing password: {}", e.getMessage());
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            boolean result = BCrypt.checkpw(password, hashedPassword);
            logger.info("Password verification result: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("Error verifying password: {}", e.getMessage());
            throw new RuntimeException("Error verifying password", e);
        }
    }
}