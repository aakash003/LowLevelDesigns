package org.flipkart.api;

import lombok.NonNull;

import java.time.LocalDateTime;

public interface ILogger {
    void log(@NonNull final String message, @NonNull final String level, LocalDateTime now);
}