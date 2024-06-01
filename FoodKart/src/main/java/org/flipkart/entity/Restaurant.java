package org.flipkart.entity;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private String name;
    private String address;
    private String foodItem;
    private double rating;
    private List<String> review;
    private int quantity;
    private double price;

    public Restaurant(String name, String address, String foodItem, double price, int quantity) {
        this.name = name;
        this.address = address;
        this.foodItem = foodItem;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(String foodItem) {
        this.foodItem = foodItem;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public List<String> getReview() {
        if (review == null) {
            return new ArrayList<>();
        }
        return review;
    }

    public String getReviewAsString() {
        StringBuilder sb = new StringBuilder();
        for (String s : review) {
            sb.append(s).append(" ## ");
        }
        return sb.toString();
    }


    public void setReview(List<String> review) {
        this.review = review;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }



}
