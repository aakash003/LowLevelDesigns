package org.flipkart.entity;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    private String name;
    private String designation;

    public User(String name) {
        this.name = name;
    }
}
