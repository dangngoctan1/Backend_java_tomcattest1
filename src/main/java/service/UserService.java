package service;

import dao.UserDAO;
import entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.JWTUtil;
import util.PasswordUtil;

import java.time.LocalDateTime;

public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final UserDAO userDAO;
    private final LogService logService;

    public UserService() {
        this.userDAO = new UserDAO();
        this.logService = new LogService();
    }

    public User register(String fullname, String username, String email, String password) {
        if (userDAO.findByUsername(username) != null) {
            logger.warn("Registration failed: Username {} already exists", username);
            throw new RuntimeException("Username already exists");
        }
        if (userDAO.findByEmail(email) != null) {
            logger.warn("Registration failed: Email {} already exists", email);
            throw new RuntimeException("Email already exists");
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        User user = new User(fullname, username, email, hashedPassword, LocalDateTime.now());
        userDAO.save(user);
        logService.logAction("User registered: " + username);
        logger.info("User registered successfully: {}", username);
        return user;
    }

    public String login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            logger.warn("Login failed: User {} not found", username);
            throw new RuntimeException("User not found");
        }
        if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
            logger.warn("Login failed: Incorrect password for user {}", username);
            throw new RuntimeException("Incorrect password");
        }
        String token = JWTUtil.generateToken(username);
        logService.logAction("User logged in: " + username);
        logger.info("User logged in successfully: {}", username);
        return token;
    }

    public User findById(Long id) {
        User user = userDAO.findById(id);
        if (user != null) {
            logger.debug("Found user by ID: {}", id);
        } else {
            logger.warn("User not found by ID: {}", id);
        }
        return user;
    }

    public User findByUsername(String username) {
        User user = userDAO.findByUsername(username);
        if (user != null) {
            logger.debug("Found user by username: {}", username);
        } else {
            logger.warn("User not found by username: {}", username);
        }
        return user;
    }

    public User findByEmail(String email) {
        User user = userDAO.findByEmail(email);
        if (user != null) {
            logger.debug("Found user by email: {}", email);
        } else {
            logger.warn("User not found by email: {}", email);
        }
        return user;
    }

    public void update(User user) {
        userDAO.update(user);
        logService.logAction("User updated: " + user.getUsername());
        logger.info("User updated successfully: {}", user.getUsername());
    }

    public void delete(Long id) {
        userDAO.delete(id);
        logService.logAction("User deleted: ID " + id);
        logger.info("User deleted successfully: ID {}", id);
    }
}