package org.flipkart.entity;

public class Patient {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Patient(String name) {
        this.name = name;
    }

    private String name;

}
