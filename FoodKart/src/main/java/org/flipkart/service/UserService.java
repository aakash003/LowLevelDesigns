package org.flipkart.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flipkart.entity.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
    private Map<String, User> users = new HashMap<>();
    private static final Logger log = LogManager.getLogger(UserService.class);
    private List<User> activeUsers = new ArrayList<>();

    public void signUp(String name, String email, String mobile, String address) {
        // create new user and add to users list
        if (users.containsKey(name)) {
            throw new IllegalArgumentException("User already exists");
        }
        users.put(mobile, new User(name, email, mobile, address));
        log.info("sign up successful");
    }

    public void login(String mobile) {
        // check if user exists and return user
        if (users.containsKey(mobile)) {
            activeUsers.clear();
            activeUsers.add(users.get(mobile));
            log.info("User loggedIn successfully");
            return;
        }
        log.info("User not found.PLease sign up first");
    }

    public void logout() {
        User user = activeUsers.get(0);
        activeUsers.remove(user);
        log.info("User loggedOut successfully");
    }

    public User getActiveUser() {
        return activeUsers.get(0);
    }
}
