package org.flipkart.strategy.impl;

import org.flipkart.model.Message;
import org.flipkart.strategy.SinkStrategy;
import org.flipkart.util.LoggerUtil;

public class FileSinkStrategy implements SinkStrategy {

    @Override
    public void write(Message message) {
        System.out.println("[FILE] " + LoggerUtil.printStatement(message));
    }
}