package org.flipkart.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * defined message levels
 */
public enum LoggerLevel {

    FATAL("FATAL", 5),
    ERROR("ERROR", 4),
    WARN("WARN", 3),
    INFO("INFO", 2),
    DEBUG("DEBUG", 1);

    private final String level;
    private final int levelNumber;


    // Cache LoggerLevel values by their names
    private static final Map<String, LoggerLevel> LEVELS_BY_NAME = new HashMap<>();

    static {
        // Populate the cache
        for (LoggerLevel loggerLevel : LoggerLevel.values()) {
            LEVELS_BY_NAME.put(loggerLevel.getLevel(), loggerLevel);
        }
    }


    LoggerLevel(String level, int levelNumber) {
        this.level = level;
        this.levelNumber = levelNumber;
    }

    public String getLevel() {
        return level;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public static LoggerLevel getLevelByName(String levelName) {
        return LEVELS_BY_NAME.get(levelName);
    }

}