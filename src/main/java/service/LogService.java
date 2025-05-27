package service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogService {
    private static final Logger logger = LogManager.getLogger(LogService.class);

    public void logAction(String message) {
        logger.info("Action: {}", message);
    }

    public void logError(String message, Throwable throwable) {
        logger.error("Error: {}", message, throwable);
    }
}