package org.flipkart.service;

import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flipkart.entity.User;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class UserService {
    private static final Logger log = LogManager.getLogger(UserService.class);
    Map<String, User> userAccount = new HashMap<>();

    public void createUser(String name, String address, double balance) {
        log.info("Creating user");
        User user = new User(name, address, balance);
        if (userAccount.containsKey(name)) {
            log.error("User already exists");
        } else {
            userAccount.put(name, user);
            log.info("User created");
        }
    }

    private Map<String, User> getUserAccount() {
        return userAccount;
    }

    public User getUser(String name) {
        return userAccount.get(name);
    }

    public boolean checkUser(String name) {
        if (userAccount.containsKey(name)) {
            return true;
        } else {
            log.error("User not found");
            return false;
        }
    }
}
