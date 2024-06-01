package org.flipkart.entity;

public class Inventory {
    private Item item;
    private int quantity;
    public Inventory(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }
}
