package service;

import dao.UserDAO;
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

public class TaskServiceTest {
    private static SessionFactory sessionFactory;
    private static TaskService taskService;
    private static UserService userService;
    private static UserDAO userDAO;
    private User testUser;

    @BeforeAll
    public static void setup() {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        sessionFactory = configuration.buildSessionFactory();
        taskService = new TaskService();
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

        testUser = userService.register("Test User", "testuser_" + System.currentTimeMillis(),
                "test" + System.currentTimeMillis() + "@example.com", "password");
    }

    @Test
    public void testCreateTask() {
        Task task = taskService.createTask(testUser.getId(), "Test Task", "Description",
                LocalDateTime.now().plusDays(1), "HIGH", "NEW");
        assertNotNull(task.getId(), "Task should be created successfully");
        assertEquals("Test Task", task.getTitle(), "Task title should match");
        assertEquals("HIGH", task.getPriority(), "Task priority should match");
    }

    @Test
    public void testFindById() {
        Task task = taskService.createTask(testUser.getId(), "Test Task", "Description",
                LocalDateTime.now().plusDays(1), "HIGH", "NEW");
        Task found = taskService.findById(task.getId());
        assertNotNull(found, "Task should be found by ID");
        assertEquals("Test Task", found.getTitle(), "Task title should match");
    }

    @Test
    public void testFindByUser() {
        taskService.createTask(testUser.getId(), "Task 1", "Desc 1", null, "MEDIUM", "NEW");
        taskService.createTask(testUser.getId(), "Task 2", "Desc 2", null, "LOW", "IN_PROGRESS");
        List<Task> tasks = taskService.findByUser(testUser.getId());
        assertEquals(2, tasks.size(), "Should find 2 tasks for user");
    }

    @Test
    public void testFindByStatus() {
        taskService.createTask(testUser.getId(), "Test Task", "Description", null, "HIGH", "COMPLETED");
        List<Task> tasks = taskService.findByStatus(testUser.getId(), "COMPLETED");
        assertEquals(1, tasks.size(), "Should find 1 task with status COMPLETED");
        assertEquals("COMPLETED", tasks.get(0).getStatus(), "Task status should match");
    }

    @Test
    public void testFindByPriority() {
        taskService.createTask(testUser.getId(), "Test Task", "Description", null, "LOW", "NEW");
        List<Task> tasks = taskService.findByPriority(testUser.getId(), "LOW");
        assertEquals(1, tasks.size(), "Should find 1 task with priority LOW");
        assertEquals("LOW", tasks.get(0).getPriority(), "Task priority should match");
    }

    @Test
    public void testUpdate() {
        Task task = taskService.createTask(testUser.getId(), "Test Task", "Description", null, "MEDIUM", "NEW");
        task.setTitle("Updated Task");
        taskService.update(task);

        Task found = taskService.findById(task.getId());
        assertEquals("Updated Task", found.getTitle(), "Task title should be updated");
    }

    @Test
    public void testDelete() {
        Task task = taskService.createTask(testUser.getId(), "Test Task", "Description", null, "HIGH", "NEW");
        taskService.delete(task.getId());
        Task found = taskService.findById(task.getId());
        assertNull(found, "Task should be deleted");
    }
}