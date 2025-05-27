package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entity.Task;
import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import service.TaskService;
import service.UserService;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaskServletTest {
    private TaskServlet taskServlet;
    private TaskService taskService;
    private UserService userService;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        taskService = mock(TaskService.class, Mockito.withSettings().defaultAnswer(Mockito.RETURNS_DEFAULTS));
        userService = mock(UserService.class, Mockito.withSettings().defaultAnswer(Mockito.RETURNS_DEFAULTS));
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        taskServlet = new TaskServlet(taskService, userService, objectMapper);
    }

    @Test
    public void testDoGetAllTasks() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        when(request.getAttribute("username")).thenReturn("testuser");
        when(request.getPathInfo()).thenReturn("/");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);

        Task task = new Task("Test Task", "Description", LocalDateTime.now(), null, "HIGH", "NEW", user);
        when(taskService.findByUser(1L)).thenReturn(List.of(task));

        taskServlet.doGet(request, response);

        verify(response).setContentType("application/json");
        assertTrue(stringWriter.toString().contains("\"title\":\"Test Task\""));
    }

    @Test
    public void testDoPostSuccess() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        when(request.getAttribute("username")).thenReturn("testuser");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);

        String jsonInput = "{\"title\":\"Test Task\",\"description\":\"Description\",\"priority\":\"HIGH\",\"status\":\"NEW\"}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(jsonInput.getBytes());
        ServletInputStream servletInputStream = new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // Không cần cho test này
            }
        };
        when(request.getInputStream()).thenReturn(servletInputStream);

        Task task = new Task("Test Task", "Description", LocalDateTime.now(), null, "HIGH", "NEW", user);
        when(taskService.createTask(eq(1L), eq("Test Task"), eq("Description"), isNull(), eq("HIGH"), eq("NEW"))).thenReturn(task);

        taskServlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        assertTrue(stringWriter.toString().contains("\"title\":\"Test Task\""));
    }

    @Test
    public void testDoPatchStatus() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        when(request.getAttribute("username")).thenReturn("testuser");
        when(request.getPathInfo()).thenReturn("/1/status");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);

        Task task = new Task("Test Task", "Description", LocalDateTime.now(), null, "HIGH", "NEW", user);
        task.setId(1L);
        when(taskService.findById(1L)).thenReturn(task);

        String jsonInput = "{\"status\":\"COMPLETED\"}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(jsonInput.getBytes());
        ServletInputStream servletInputStream = new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // Không cần cho test này
            }
        };
        when(request.getInputStream()).thenReturn(servletInputStream);

        taskServlet.doPatch(request, response);

        verify(taskService).update(task);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(stringWriter.toString().contains("\"status\":\"COMPLETED\""));
    }
}