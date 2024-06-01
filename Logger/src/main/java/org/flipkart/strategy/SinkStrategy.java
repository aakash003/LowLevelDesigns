package org.flipkart.strategy;


import org.flipkart.model.Message;

@FunctionalInterface
public interface SinkStrategy {

    void write(Message message);

}