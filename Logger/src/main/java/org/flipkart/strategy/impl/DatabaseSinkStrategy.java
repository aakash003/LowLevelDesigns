package org.flipkart.strategy.impl;

import org.flipkart.model.Message;
import org.flipkart.strategy.SinkStrategy;
import org.flipkart.util.LoggerUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class DatabaseSinkStrategy implements SinkStrategy {

    private final Map<LocalDateTime, Message> inMemoryDatabase = new HashMap();

    @Override
    public void write(Message message) {
        inMemoryDatabase.put(message.getEpoch(), message);
        System.out.println("[DB] " + LoggerUtil.printStatement(message));
    }
}