package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.JWTUtil;

import java.io.IOException;

public class AuthenticationFilter implements Filter {
    private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, jakarta.servlet.ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String clientIP = httpRequest.getRemoteAddr();
        logger.info("Processing request: {} from IP: {}", requestURI, clientIP);

        // Bỏ qua cho /api/login và /api/register
        if (requestURI.endsWith("/api/login") || requestURI.endsWith("/api/register")) {
            logger.debug("Bypassing authentication for: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header from IP: {}", clientIP);
            sendUnauthorizedResponse(httpResponse, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        if (JWTUtil.validateToken(token)) {
            String username = JWTUtil.getUsernameFromToken(token);
            // Lưu username vào request để servlet sử dụng
            httpRequest.setAttribute("username", username);
            logger.info("Authenticated user: {} for request: {}", username, requestURI);
            chain.doFilter(request, response);
        } else {
            logger.warn("Invalid JWT token from IP: {}", clientIP);
            sendUnauthorizedResponse(httpResponse, "Invalid JWT token");
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}