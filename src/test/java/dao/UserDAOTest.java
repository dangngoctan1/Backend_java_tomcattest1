package dao;

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

public class UserDAOTest {
    private static SessionFactory sessionFactory;
    private static UserDAO userDAO;
    private User testUser;

    @BeforeAll
    public static void setup() {
        // Sử dụng cấu hình từ hibernate.cfg.xml (SQL Server)
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        sessionFactory = configuration.buildSessionFactory();
        userDAO = new UserDAO();
    }

    @AfterAll
    public static void teardown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    public void setupUser() {
        // Xóa tất cả dữ liệu User để đảm bảo môi trường sạch
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createQuery("DELETE FROM User").executeUpdate();
            session.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Failed to clean User data: " + e.getMessage());
        }

        // Tạo User mới với username và email duy nhất
        testUser = new User("Test User", "testuser_" + System.currentTimeMillis(),
                "test" + System.currentTimeMillis() + "@example.com",
                "password", LocalDateTime.now());
        userDAO.save(testUser);
        assertNotNull(testUser.getId(), "User should be saved successfully before running tests");
    }

    @Test
    public void testSaveAndFindById() {
        User found = userDAO.findById(testUser.getId());
        assertNotNull(found, "User should be found by ID");
        assertEquals(testUser.getUsername(), found.getUsername(), "User username should match");
        assertEquals(testUser.getEmail(), found.getEmail(), "User email should match");
    }

    @Test
    public void testFindByUsername() {
        User found = userDAO.findByUsername(testUser.getUsername());
        assertNotNull(found, "User should be found by username");
        assertEquals(testUser.getUsername(), found.getUsername(), "User username should match");
        assertEquals(testUser.getId(), found.getId(), "User ID should match");
    }

    @Test
    public void testFindByEmail() {
        User found = userDAO.findByEmail(testUser.getEmail());
        assertNotNull(found, "User should be found by email");
        assertEquals(testUser.getEmail(), found.getEmail(), "User email should match");
        assertEquals(testUser.getId(), found.getId(), "User ID should match");
    }

    @Test
    public void testUpdate() {
        testUser.setFullname("Updated User");
        userDAO.update(testUser);

        User found = userDAO.findById(testUser.getId());
        assertNotNull(found, "Updated user should be found");
        assertEquals("Updated User", found.getFullname(), "User fullname should be updated");
    }

    @Test
    public void testDelete() {
        userDAO.delete(testUser.getId());
        User found = userDAO.findById(testUser.getId());
        assertNull(found, "User should be deleted and not found");
    }
}