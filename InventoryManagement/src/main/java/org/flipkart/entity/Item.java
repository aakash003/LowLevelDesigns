package org.flipkart.entity;

public class Item {
    private String brand;

    private String category;
    private double price;


    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }


    public Item(String brand, String category, double price) {
        this.brand = brand;
        this.category = category;
        this.price = price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
