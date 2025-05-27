package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LogServiceTest {
    private final LogService logService = new LogService();

    @Test
    public void testLogAction() {
        // Log4j2 ghi log ra file/console, khó kiểm tra trực tiếp
        // Kiểm tra rằng phương thức không ném ngoại lệ
        assertDoesNotThrow(() -> logService.logAction("Test action"));
    }

    @Test
    public void testLogError() {
        // Kiểm tra rằng phương thức không ném ngoại lệ
        assertDoesNotThrow(() -> logService.logError("Test error", new RuntimeException("Test")));
    }
}