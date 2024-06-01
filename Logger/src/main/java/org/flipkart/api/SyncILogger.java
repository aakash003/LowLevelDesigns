package org.flipkart.api;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.flipkart.configuration.LoggerConfiguration;
import org.flipkart.enums.LoggerLevel;
import org.flipkart.model.Message;
import org.flipkart.strategy.SinkContext;
import org.flipkart.strategy.SinkStrategy;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
public class SyncILogger implements ILogger {

    private static LoggerConfiguration instance = LoggerConfiguration.getInstance();

    private final SinkContext context = new SinkContext();


    @Override
    public void log(@NonNull final String message, @NonNull final String level, LocalDateTime now) {

        LoggerLevel messageLoggerLevel = LoggerLevel.getLevelByName(level);
        LoggerLevel globalLoggerLevel = LoggerLevel.getLevelByName(instance.getGlobalLogLevel());

        // check whether need to log or not
        if (messageLoggerLevel.getLevelNumber() >= globalLoggerLevel.getLevelNumber()) {
            // get List<Sink> out of Level from configuration
            List<String> sinkList = instance.getConfig(messageLoggerLevel);

            // traverse sinkList and process message
            for (String sinkName : sinkList) {

                // get Sink Strategy and execute message
                SinkStrategy strategy = SinkContext.getRegisteredStrategy(sinkName);

                // execute message on particular strategy
                context.setStrategy(strategy);

                Message loggerMessage = new Message(messageLoggerLevel, LocalDateTime.now(), message);

                // execute Message
                context.executeStrategy(loggerMessage);
            }
        }
    }

}
