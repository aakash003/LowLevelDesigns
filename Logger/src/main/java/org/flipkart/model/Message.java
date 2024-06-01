package org.flipkart.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.flipkart.enums.LoggerLevel;

import java.time.LocalDateTime;


@AllArgsConstructor
@Getter
@Setter
@ToString
public class Message {
    private LoggerLevel loggerLevel;
    private LocalDateTime epoch;
    private String message;
}