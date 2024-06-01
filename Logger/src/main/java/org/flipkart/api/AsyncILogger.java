package org.flipkart.api;

import lombok.NonNull;
import lombok.Setter;
import org.flipkart.configuration.LoggerConfiguration;
import org.flipkart.enums.LoggerLevel;
import org.flipkart.model.Message;
import org.flipkart.strategy.SinkContext;
import org.flipkart.strategy.SinkStrategy;
import org.flipkart.util.LoggerUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncILogger implements ILogger {

    private static final LoggerConfiguration instance = LoggerConfiguration.getInstance();

    private final BlockingQueue<Message> messageQueue;

    private final SinkContext context = new SinkContext();

    @Setter
    private int bufferSize;

    public AsyncILogger(int bufferSize) {
        messageQueue = new LinkedBlockingQueue<>(bufferSize);
        this.bufferSize = bufferSize;
        Thread messageProcessorThread = new Thread(this::processMessages);
        messageProcessorThread.start();
    }


    @Override
    public void log(@NonNull String message, @NonNull String level, LocalDateTime now) {
        LoggerLevel messageLoggerLevel = LoggerLevel.getLevelByName(level);
        LoggerLevel globalLoggerLevel = LoggerLevel.getLevelByName(instance.getGlobalLogLevel());
        // check whether need to log or not
        if (messageLoggerLevel.getLevelNumber() >= globalLoggerLevel.getLevelNumber()) {
            try {
                putMessage(messageLoggerLevel, message, now);
                //messageQueue.put(new Message(messageLoggerLevel, LocalDateTime.now(), message));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    public synchronized void putMessage(LoggerLevel messageLoggerLevel, String message, LocalDateTime now) throws InterruptedException {
        if (messageQueue.size() > bufferSize) {
            //remove the oldest message
            messageQueue.take();
        }
        messageQueue.offer(new Message(messageLoggerLevel, now, message));
        //System.out.println("ThreadID=" + Thread.currentThread().getId() + " Message added to queue: " + messageQueue.size());
    }

    private void processMessages() {
        while (true) {
            try {
                Message loggerMessage = messageQueue.take();

                // get List<Sink> out of Level from configuration
                List<String> sinkList = instance.getConfig(loggerMessage.getLoggerLevel());

                // traverse sinkList and process message
                for (String sinkName : sinkList) {
                   // System.out.println();
                    // get Sink Strategy and execute message
                    SinkStrategy strategy = SinkContext.getRegisteredStrategy(sinkName);

                    // execute message on particular strategy
                    context.setStrategy(strategy);

                    // execute Message
                    context.executeStrategy(loggerMessage);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

//// ...
//
//    private final ExecutorService executorService;
//
//    public AsyncILogger(int bufferSize) {
//        // ...
//        executorService = Executors.newFixedThreadPool(10); // Adjust the number of threads as needed
//        // ...
//    }
//
//    private void processMessages() {
//        while (true) {
//            try {
//                List<Message> messages = new ArrayList<>();
//                messageQueue.drainTo(messages, 10); // Adjust the batch size as needed
//
//                for (Message message : messages) {
//                    executorService.submit(() -> processMessage(message));
//                }
//            } catch (Exception e) {
//                Thread.currentThread().interrupt();
//                break;
//            }
//        }
//    }
//
//    private void processMessage(Message message) {
//        // Process the message
//        // ...
//    }
//
//    // Don't forget to shutdown the executor service when you're done with it
//    public void shutdown() throws InterruptedException {
//        executorService.shutdown();
//        executorService.awaitTermination(60, TimeUnit.SECONDS);
//    }

}
