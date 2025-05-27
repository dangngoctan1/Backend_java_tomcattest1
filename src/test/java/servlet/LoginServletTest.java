package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import service.UserService;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoginServletTest {
    private LoginServlet loginServlet;
    private UserService userService;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        userService = mock(UserService.class, Mockito.withSettings().defaultAnswer(Mockito.RETURNS_DEFAULTS));
        objectMapper = new ObjectMapper();
        loginServlet = new LoginServlet(userService, objectMapper);
    }

    @Test
    public void testDoPostSuccess() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        String jsonInput = "{\"username\":\"testuser\",\"password\":\"password\"}";
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

        when(userService.login("testuser", "password")).thenReturn("mocktoken");

        loginServlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(stringWriter.toString().contains("\"token\":\"mocktoken\""));
    }

    @Test
    public void testDoPostInvalidCredentials() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        String jsonInput = "{\"username\":\"testuser\",\"password\":\"wrongpassword\"}";
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

        when(userService.login("testuser", "wrongpassword")).thenThrow(new RuntimeException("Incorrect password"));

        loginServlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(stringWriter.toString().contains("\"error\":\"Incorrect password\""));
    }
}