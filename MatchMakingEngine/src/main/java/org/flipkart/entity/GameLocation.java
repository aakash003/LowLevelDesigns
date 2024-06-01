package org.flipkart.entity;

public class GameLocation {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public GameLocation(String name) {
        this.name = name;
    }

    // getters and setters
}