package org.flipkart.entity;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Actor {
    private final String address;
    private final BlockingQueue<Message> mailbox = new LinkedBlockingQueue<>();
    private final ActorSystem actorSystem;
    private boolean isShutdown = false;

    public Actor(String address, ActorSystem actorSystem) {
        this.address = address;
        this.actorSystem = actorSystem;
    }

    public String getAddress() {
        return address;
    }

    public void send(Message message, Actor receiver) {
        if (!isShutdown) {
            receiver.receive(message);
        }
    }

    public void receive(Message message) {
        if (!isShutdown) {
            mailbox.add(message);
            actorSystem.execute(this::processMessage);
        }
    }

    private void processMessage() {
        if (!isShutdown && !mailbox.isEmpty()) {
            Message message = mailbox.poll();
            // process the message
            System.out.println("Actor " + address + " processed message: " + message.getContent());
        }
    }

    public void shutdown() {
        isShutdown = true;
    }
}