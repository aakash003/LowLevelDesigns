package org.flipkart.entity;

public class CartItem {
    private Item item;

    private int quantity;
    private User user;


    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public CartItem(Item item, int quantity, User user) {
        this.item = item;
        this.quantity = quantity;
        this.user = user;
    }

}
