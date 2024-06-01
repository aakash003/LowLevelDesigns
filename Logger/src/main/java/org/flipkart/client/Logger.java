package org.flipkart.client;

import lombok.Getter;
import org.flipkart.api.AsyncILogger;
import org.flipkart.api.ILogger;
import org.flipkart.api.SyncILogger;
import org.flipkart.configuration.LoggerConfiguration;
import org.flipkart.enums.LoggerLevel;
import org.flipkart.enums.LoggerType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
public class Logger {

    private static final LoggerConfiguration instance = LoggerConfiguration.getInstance();
    private static final Map<LoggerType, ILogger> loggerTypeFactoryMap = new HashMap<>();

    static {
        loggerTypeFactoryMap.put(LoggerType.SYNC, new SyncILogger());
        loggerTypeFactoryMap.put(LoggerType.ASYNC, new AsyncILogger(25));
    }


    public void addLoggerConfig(String level, LoggerType loggerType, List<String> sink) {
        LoggerLevel logLevel = LoggerLevel.getLevelByName(level);

        //one or more sinks associated with level
        instance.addLevelSinkConfiguration(logLevel, sink);


        instance.addLevelLoggerTypeConfiguration(logLevel, loggerType);
    }

    public void addGlobalLogLevel(String level) {
        instance.setGlobalLogLevel(level);
    }

    //Accepts messages from client(s)
    public void log(String message, String level, LocalDateTime now) {
        LoggerLevel logLevel = LoggerLevel.getLevelByName(level);
        ILogger logger = loggerTypeFactoryMap.get(instance.getLevelLoggerTypeMap().get(logLevel));
        logger.log(message, level, now);
    }

    public void addLogType(LoggerType loggerType) {
        instance.setLoggerType(loggerType);
    }

    public void setBufferSize(int bufferSize) {
        AsyncILogger asyncILogger = (AsyncILogger) loggerTypeFactoryMap.get(LoggerType.ASYNC);
        asyncILogger.setBufferSize(bufferSize);
    }
}
