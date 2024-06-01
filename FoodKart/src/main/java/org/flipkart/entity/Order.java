package org.flipkart.entity;

public class Order {
    private String restaurantName;
    private String itemName;
    private Integer quantity;
    private Double billAmount;
    private String mobile;

    public Order(String name, String itemName, int quantity, String mobile, double billAmount) {
        this.restaurantName = name;
        this.itemName = itemName;
        this.quantity = quantity;
        this.mobile = mobile;
        this.billAmount = billAmount;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }


    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }


}
