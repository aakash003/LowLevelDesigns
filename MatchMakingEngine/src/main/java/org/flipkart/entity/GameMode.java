package org.flipkart.entity;

public class GameMode {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    public void setRequiredPlayers(int requiredPlayers) {
        this.requiredPlayers = requiredPlayers;
    }

    private int requiredPlayers;

    public GameMode(String name, int requiredPlayers) {
        this.name = name;
        this.requiredPlayers = requiredPlayers;
    }

    // getters and setters
}
