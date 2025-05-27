package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {
    private UserService userService;
    private ObjectMapper objectMapper;

    public RegisterServlet() {
        this.userService = new UserService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(WRITE_DATES_AS_TIMESTAMPS);
    }

    // For testing
    public RegisterServlet(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Map<String, String> userData = objectMapper.readValue(request.getInputStream(), Map.class);
            String fullname = userData.get("fullname");
            String username = userData.get("username");
            String email = userData.get("email");
            String password = userData.get("password");

            userService.register(fullname, username, email, password);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("application/json");
            Map<String, String> success = new HashMap<>();
            success.put("message", "User registered successfully");
            objectMapper.writeValue(response.getWriter(), success);
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            objectMapper.writeValue(response.getWriter(), error);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            objectMapper.writeValue(response.getWriter(), error);
        }
    }
}