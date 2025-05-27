package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class CORSFilter implements Filter {
    private static final Logger logger = LogManager.getLogger(CORSFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, jakarta.servlet.ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Giới hạn origin cho frontend (thay đổi theo domain thực tế)
        String origin = System.getenv("FRONTEND_ORIGIN") != null
                ? System.getenv("FRONTEND_ORIGIN")
                : "http://localhost:3000"; // Default cho dev
        httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            logger.info("Handling CORS preflight request for: {}", httpRequest.getRequestURI());
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        logger.info("Processing CORS for request: {}", httpRequest.getRequestURI());
        chain.doFilter(request, response);
    }
}