package util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JWTUtil {
    private static final Logger logger = LogManager.getLogger(JWTUtil.class);
    // Lấy khóa từ biến môi trường, mặc định nếu không có
    private static final String KEY_STRING = System.getenv("JWT_SECRET") != null
            ? System.getenv("JWT_SECRET")
            : "mySuperSecretKey12345678901234567890"; // Fallback cho dev
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(KEY_STRING.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_TIME = Long.parseLong(
            System.getenv("JWT_EXPIRATION") != null ? System.getenv("JWT_EXPIRATION") : "3600000" // 1 hour default
    );

    public static String generateToken(String username) {
        logger.info("Generating JWT for username: {}", username);
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String username = claims.getSubject();
            if (username == null) {
                logger.warn("No username found in JWT");
                throw new IllegalArgumentException("Invalid token: no username");
            }
            logger.info("Extracted username from JWT: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("Error extracting username from JWT: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token: " + e.getMessage());
        }
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);
            logger.info("JWT validated successfully");
            return true;
        } catch (Exception e) {
            logger.error("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }
}