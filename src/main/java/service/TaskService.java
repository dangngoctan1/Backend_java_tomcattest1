package service;

import dao.TaskDAO;
import entity.Task;
import entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.List;

public class TaskService {
    private static final Logger logger = LogManager.getLogger(TaskService.class);
    private final TaskDAO taskDAO;
    private final LogService logService;

    public TaskService() {
        this.taskDAO = new TaskDAO();
        this.logService = new LogService();
    }

    public Task createTask(Long userId, String title, String description, LocalDateTime dueDate, String priority, String status) {
        UserService userService = new UserService();
        User user = userService.findById(userId);
        if (user == null) {
            logger.warn("Task creation failed: User ID {} not found", userId);
            throw new RuntimeException("User not found");
        }

        Task task = new Task(title, description, LocalDateTime.now(), dueDate, priority.toUpperCase(), status.toUpperCase(), user);
        taskDAO.save(task);
        logService.logAction("Task created: " + title + " for user ID " + userId);
        logger.info("Task created successfully: {} for user ID {}", title, userId);
        return task;
    }

    public Task findById(Long id) {
        Task task = taskDAO.findById(id);
        if (task != null) {
            logger.debug("Found task by ID: {}", id);
        } else {
            logger.warn("Task not found by ID: {}", id);
        }
        return task;
    }

    public List<Task> findByUser(Long userId) {
        List<Task> tasks = taskDAO.findByUser(userId);
        logger.debug("Found {} tasks for user ID: {}", tasks.size(), userId);
        return tasks;
    }

    public List<Task> findByStatus(Long userId, String status) {
        List<Task> tasks = taskDAO.findByStatus(userId, status.toUpperCase());
        logger.debug("Found {} tasks with status {} for user ID: {}", tasks.size(), status, userId);
        return tasks;
    }

    public List<Task> findByPriority(Long userId, String priority) {
        List<Task> tasks = taskDAO.findByPriority(userId, priority.toUpperCase());
        logger.debug("Found {} tasks with priority {} for user ID: {}", tasks.size(), priority, userId);
        return tasks;
    }

    public void update(Task task) {
        taskDAO.update(task);
        logService.logAction("Task updated: " + task.getTitle() + " for user ID " + task.getUser().getId());
        logger.info("Task updated successfully: {}", task.getTitle());
    }

    public void delete(Long id) {
        taskDAO.delete(id);
        logService.logAction("Task deleted: ID " + id);
        logger.info("Task deleted successfully: ID {}", id);
    }
}