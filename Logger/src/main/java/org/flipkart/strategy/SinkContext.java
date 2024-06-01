package org.flipkart.strategy;

import org.flipkart.model.Message;
import org.flipkart.strategy.impl.ConsoleSinkStrategy;
import org.flipkart.strategy.impl.DatabaseSinkStrategy;
import org.flipkart.strategy.impl.FileSinkStrategy;

import java.util.HashMap;
import java.util.Map;

import static org.flipkart.constants.LoggerConstants.*;

public class SinkContext {

    private SinkStrategy sinkStrategy;
    private static final Map<String, SinkStrategy> strategyRegister = new HashMap<>();

    static {
        SinkContext.registerStrategy(CONSOLE, new ConsoleSinkStrategy());
        SinkContext.registerStrategy(DATABASE, new DatabaseSinkStrategy());
        SinkContext.registerStrategy(FILE, new FileSinkStrategy());
    }

    private static void registerStrategy(String strategyName, SinkStrategy strategy) {
        strategyRegister.put(strategyName, strategy);
    }

    public static SinkStrategy getRegisteredStrategy(String strategyName) {
        if (strategyRegister.containsKey(strategyName)) {
            return strategyRegister.get(strategyName);
        }

        return null;
    }

    public void setStrategy(SinkStrategy strategy) {
        this.sinkStrategy = strategy;
    }

    public void executeStrategy(Message loggerMessage){
        this.sinkStrategy.write(loggerMessage);
    }

}