package org.flipkart.entity;


import java.util.ArrayList;
import java.util.List;

public class User {
    private String email;
    private String mobile;
    private String name;
    private String address;
    private List<Order> orderHistory;

    public User(String name,String email,String mobile,String address) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.address = address;
        orderHistory = new ArrayList<>();
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}