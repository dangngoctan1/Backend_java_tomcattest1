package dao;

import entity.Task;
import entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskDAOTest {
    private static SessionFactory sessionFactory;
    private static TaskDAO taskDAO;
    private static UserDAO userDAO;
    private User testUser;

    @BeforeAll
    public static void setup() {
        // Sử dụng cấu hình từ hibernate.cfg.xml (SQL Server)
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        sessionFactory = configuration.buildSessionFactory();
        taskDAO = new TaskDAO();
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
        // Xóa tất cả dữ liệu User và Task để đảm bảo môi trường sạch
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createQuery("DELETE FROM Task").executeUpdate();
            session.createQuery("DELETE FROM User").executeUpdate();
            session.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Failed to clean data: " + e.getMessage());
        }

        // Tạo User mới với username và email duy nhất
        testUser = new User("Test User", "testuser_" + System.currentTimeMillis(),
                "test" + System.currentTimeMillis() + "@example.com",
                "password", LocalDateTime.now());
        userDAO.save(testUser);
        assertNotNull(testUser.getId(), "User should be saved successfully before running task tests");
    }

    @Test
    public void testSaveAndFindById() {
        Task task = new Task("Test Task", "Description", LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), "HIGH", "NEW", testUser);
        taskDAO.save(task);

        Task found = taskDAO.findById(task.getId());
        assertNotNull(found, "Task should be found by ID");
        assertEquals("Test Task", found.getTitle(), "Task title should match");
        assertEquals("HIGH", found.getPriority(), "Task priority should match");
        assertEquals("NEW", found.getStatus(), "Task status should match");
        assertEquals(testUser.getId(), found.getUser().getId(), "Task user ID should match");
    }

    @Test
    public void testFindByUser() {
        Task task1 = new Task("Task 1", "Desc 1", LocalDateTime.now(), null, "MEDIUM", "NEW", testUser);
        Task task2 = new Task("Task 2", "Desc 2", LocalDateTime.now(), null, "LOW", "IN_PROGRESS", testUser);
        taskDAO.save(task1);
        taskDAO.save(task2);

        List<Task> tasks = taskDAO.findByUser(testUser.getId());
        assertEquals(2, tasks.size(), "Should find exactly 2 tasks for the user");
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task 1")), "Task 1 should be found");
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task 2")), "Task 2 should be found");
    }

    @Test
    public void testFindByStatus() {
        Task task = new Task("Test Task Status", "Description", LocalDateTime.now(), null,
                "HIGH", "COMPLETED", testUser);
        taskDAO.save(task);

        List<Task> tasks = taskDAO.findByStatus(testUser.getId(), "COMPLETED");
        assertEquals(1, tasks.size(), "Should find exactly 1 task with status COMPLETED");
        assertEquals("COMPLETED", tasks.get(0).getStatus(), "Task status should match");
        assertEquals("Test Task Status", tasks.get(0).getTitle(), "Task title should match");
    }

    @Test
    public void testFindByPriority() {
        Task task = new Task("Test Task Priority", "Description", LocalDateTime.now(), null,
                "LOW", "NEW", testUser);
        taskDAO.save(task);

        List<Task> tasks = taskDAO.findByPriority(testUser.getId(), "LOW");
        assertEquals(1, tasks.size(), "Should find exactly 1 task with priority LOW");
        assertEquals("LOW", tasks.get(0).getPriority(), "Task priority should match");
        assertEquals("Test Task Priority", tasks.get(0).getTitle(), "Task title should match");
    }

    @Test
    public void testUpdate() {
        Task task = new Task("Test Task Update", "Description", LocalDateTime.now(), null,
                "MEDIUM", "NEW", testUser);
        taskDAO.save(task);

        task.setTitle("Updated Task");
        taskDAO.update(task);

        Task found = taskDAO.findById(task.getId());
        assertNotNull(found, "Updated task should be found");
        assertEquals("Updated Task", found.getTitle(), "Task title should be updated");
    }

    @Test
    public void testDelete() {
        Task task = new Task("Test Task Delete", "Description", LocalDateTime.now(), null,
                "HIGH", "NEW", testUser);
        taskDAO.save(task);

        taskDAO.delete(task.getId());
        Task found = taskDAO.findById(task.getId());
        assertNull(found, "Task should be deleted and not found");
    }
}