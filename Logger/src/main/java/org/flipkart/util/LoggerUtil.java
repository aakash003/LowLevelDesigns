package org.flipkart.util;

import org.flipkart.model.Message;

public class LoggerUtil {
    public static String printStatement(Message loggerMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append(loggerMessage.getEpoch().toString());
        sb.append(" [");
        sb.append(loggerMessage.getLoggerLevel().getLevel());
        sb.append("] ");
        sb.append(loggerMessage.getMessage());
        return sb.toString();
    }
}
