package org.flipkart.entity;

public class User {
    private String name;
    private String address;
    private double balance;
    public User(String name, String address, double balance) {
        this.name = name;
        this.address = address;
        this.balance = balance;
    }
    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
