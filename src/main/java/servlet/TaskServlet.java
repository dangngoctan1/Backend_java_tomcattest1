package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entity.Task;
import entity.User;
import service.TaskService;
import service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@WebServlet("/api/tasks/*")
public class TaskServlet extends HttpServlet {
    private TaskService taskService;
    private UserService userService;
    private ObjectMapper objectMapper;

    public TaskServlet() {
        this.taskService = new TaskService();
        this.userService = new UserService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(WRITE_DATES_AS_TIMESTAMPS);
    }

    // For testing
    public TaskServlet(TaskService taskService, UserService userService, ObjectMapper objectMapper) {
        this.taskService = taskService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
        User user = userService.findByUsername(username);
        if (user == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            List<Task> tasks = taskService.findByUser(user.getId());
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), tasks);
        } else if (!pathInfo.endsWith("/status")) {
            String idStr = pathInfo.substring(1);
            try {
                Long id = Long.parseLong(idStr);
                Task task = taskService.findById(id);
                if (task == null || !task.getUser().getId().equals(user.getId())) {
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Task not found");
                } else {
                    response.setContentType("application/json");
                    objectMapper.writeValue(response.getWriter(), task);
                }
            } catch (NumberFormatException e) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID");
            }
        } else {
            sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Use PATCH for status update");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
        User user = userService.findByUsername(username);
        if (user == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        }

        try {
            Map<String, Object> taskData = objectMapper.readValue(request.getInputStream(), Map.class);
            String title = (String) taskData.get("title");
            String description = (String) taskData.get("description");
            String dueDateStr = (String) taskData.get("dueDate");
            LocalDateTime dueDate = dueDateStr != null ? LocalDateTime.parse(dueDateStr) : null;
            String priority = (String) taskData.get("priority");
            String status = (String) taskData.get("status");

            Task task = taskService.createTask(user.getId(), title, description, dueDate, priority, status);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), task);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid task data");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
        User user = userService.findByUsername(username);
        if (user == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Task ID required");
            return;
        }
        String idStr = pathInfo.substring(1);
        try {
            Long id = Long.parseLong(idStr);
            Task task = taskService.findById(id);
            if (task == null || !task.getUser().getId().equals(user.getId())) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Task not found");
                return;
            }

            Map<String, Object> taskData = objectMapper.readValue(request.getInputStream(), Map.class);
            if (taskData.containsKey("title")) {
                task.setTitle((String) taskData.get("title"));
            }
            if (taskData.containsKey("description")) {
                task.setDescription((String) taskData.get("description"));
            }
            if (taskData.containsKey("dueDate")) {
                String dueDateStr = (String) taskData.get("dueDate");
                task.setDueDate(dueDateStr != null ? LocalDateTime.parse(dueDateStr) : null);
            }
            if (taskData.containsKey("priority")) {
                task.setPriority((String) taskData.get("priority"));
            }
            if (taskData.containsKey("status")) {
                task.setStatus((String) taskData.get("status"));
            }

            taskService.update(task);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), task);
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID");
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid task data");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
        User user = userService.findByUsername(username);
        if (user == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Task ID required");
            return;
        }
        String idStr = pathInfo.substring(1);
        try {
            Long id = Long.parseLong(idStr);
            Task task = taskService.findById(id);
            if (task == null || !task.getUser().getId().equals(user.getId())) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Task not found");
            } else {
                taskService.delete(id);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID");
        }
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
        User user = userService.findByUsername(username);
        if (user == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.endsWith("/status")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
            return;
        }
        String[] parts = pathInfo.split("/");
        if (parts.length < 3) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
            return;
        }
        String idStr = parts[1];
        try {
            Long id = Long.parseLong(idStr);
            Task task = taskService.findById(id);
            if (task == null || !task.getUser().getId().equals(user.getId())) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Task not found");
                return;
            }

            Map<String, String> statusData = objectMapper.readValue(request.getInputStream(), Map.class);
            String newStatus = statusData.get("status");
            if (newStatus == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Status is required");
                return;
            }
            task.setStatus(newStatus.toUpperCase());
            taskService.update(task);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), task);
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID");
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request data");
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        objectMapper.writeValue(response.getWriter(), error);
    }
}