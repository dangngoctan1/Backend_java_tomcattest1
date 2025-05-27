package service;

import dao.UserDAO;
import entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private static SessionFactory sessionFactory;
    private static UserService userService;
    private static UserDAO userDAO;

    @BeforeAll
    public static void setup() {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        sessionFactory = configuration.buildSessionFactory();
        userService = new UserService();
        userDAO = new UserDAO();
    }

    @AfterAll
    public static void teardown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    public void setupData() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createQuery("DELETE FROM Task").executeUpdate();
            session.createQuery("DELETE FROM User").executeUpdate();
            session.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Failed to clean data: " + e.getMessage());
        }
    }

    @Test
    public void testRegister() {
        User user = userService.register("Test User", "testuser", "test@example.com", "password");
        assertNotNull(user.getId(), "User should be registered successfully");
        assertEquals("testuser", user.getUsername(), "Username should match");
        assertEquals("test@example.com", user.getEmail(), "Email should match");

        User found = userService.findByUsername("testuser");
        assertNotNull(found, "Registered user should be found");
    }

    @Test
    public void testRegisterDuplicateUsername() {
        userService.register("Test User", "testuser", "test@example.com", "password");
        Exception exception = assertThrows(RuntimeException.class, () ->
                userService.register("Test User 2", "testuser", "test2@example.com", "password"));
        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    public void testLogin() {
        userService.register("Test User", "testuser", "test@example.com", "password");
        String token = userService.login("testuser", "password");
        assertNotNull(token, "Login should return a valid JWT token");
    }

    @Test
    public void testLoginInvalidPassword() {
        userService.register("Test User", "testuser", "test@example.com", "password");
        Exception exception = assertThrows(RuntimeException.class, () ->
                userService.login("testuser", "wrongpassword"));
        assertEquals("Incorrect password", exception.getMessage());
    }

    @Test
    public void testFindById() {
        User user = userService.register("Test User", "testuser", "test@example.com", "password");
        User found = userService.findById(user.getId());
        assertNotNull(found, "User should be found by ID");
        assertEquals("testuser", found.getUsername(), "Username should match");
    }

    @Test
    public void testUpdate() {
        User user = userService.register("Test User", "testuser", "test@example.com", "password");
        user.setFullname("Updated User");
        userService.update(user);

        User found = userService.findById(user.getId());
        assertEquals("Updated User", found.getFullname(), "Fullname should be updated");
    }

    @Test
    public void testDelete() {
        User user = userService.register("Test User", "testuser", "test@example.com", "password");
        userService.delete(user.getId());
        User found = userService.findById(user.getId());
        assertNull(found, "User should be deleted");
    }
}