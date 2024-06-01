package org.flipkart.configuration;

import lombok.Getter;
import lombok.Setter;
import org.flipkart.enums.LoggerLevel;
import org.flipkart.enums.LoggerType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class LoggerConfiguration {
    private static volatile LoggerConfiguration instance = null; //for thread safe and singleton inside JVM as well
    private final Map<LoggerLevel, List<String>> levelSinkConfigmap = new HashMap<>();
    private final Map<LoggerLevel, LoggerType> levelLoggerTypeMap = new HashMap<>();
    @Getter
    @Setter
    private String globalLogLevel = null;
    @Setter
    private LoggerType loggerType = null;

    private LoggerConfiguration() {
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class."); //prevent from reflexion
        }
    }

    public static LoggerConfiguration getInstance() {
        if (instance == null) {
            synchronized (LoggerConfiguration.class) { //double check locking :  if two threads are going at it and if other thread has lock on it then it will block our thread and then other thread will create the instance and return  to which our thread will go back in by acquiring lock and check for null instance and then return so it will not create it again.
                if (instance == null) {
                    instance = new LoggerConfiguration();
                } else {
                    return instance;
                }
            }
        }
        return instance;
    }


    //Make thread safe for serialisation and deserialisation
    protected LoggerConfiguration readResolve() {
        return getInstance();
    }

    public void addLevelSinkConfiguration(LoggerLevel level, List<String> sinkTypes) {
        if (level == null || sinkTypes == null || sinkTypes.isEmpty()) {
            return;
        }
        levelSinkConfigmap.put(level, sinkTypes);
    }

    public void addLevelLoggerTypeConfiguration(LoggerLevel level, LoggerType loggerType) {
        if (loggerType == null) {
            return;
        }

        levelLoggerTypeMap.put(level, loggerType);
    }

    public List<String> getConfig(LoggerLevel level) {
        if (level == null) {
            return null;
        }

        return levelSinkConfigmap.getOrDefault(level, null);
    }

}
