package org.flipkart.service;


import org.flipkart.entity.Comment;
import org.flipkart.entity.Post;
import org.flipkart.entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flipkart.helper.ServiceFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PlatformService {
    private Map<String, User> users = new HashMap<>();
    private static final Logger log = LogManager.getLogger(PlatformService.class);
    private List<User> activeUsers = new ArrayList<>();

    private NewsFeed newsFeed = ServiceFactory.getNewsFeed();


    public void signUp(String name) {
        // create new user and add to users list
        if (users.containsKey(name)) {
            throw new IllegalArgumentException("User already exists");
        }
        users.put(name, new User(name));
        log.info("sign up successful");
    }

    public void login(String name) {
        // check if user exists and return user
        if (users.containsKey(name)) {
            activeUsers.add(users.get(name));
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


    public void post(String content) {
        User user = activeUsers.get(0);
        Post post = new Post(user, content);
        user.getPosts().add(post);
        newsFeed.addPost(post);
        log.info("Post created successfully");
    }

    public void follow(String name) {
        User userToFollow = users.get(name);
        // add userToFollow to user's followedUsers list
        User user = activeUsers.get(0);
        user.getFollowedUsers().add(userToFollow);
        log.info("User followed successfully");
    }

    public void comment(String postId, String content) {
        // create new comment and add to post's comments list
        User user = activeUsers.get(0);
        Post post = newsFeed.getPost(postId);
        post.getComments().add(new Comment(user, content));
    }

    public void upvote(String postId) {
        // increment post's upvotes
        Post post = newsFeed.getPost(postId);
        post.setUpvotes(post.getUpvotes() + 1);
    }

    public void downvote(String postId) {
        // decrement post's downvotes
        Post post = newsFeed.getPost(postId);
        post.setDownvotes(post.getDownvotes() + 1);
    }

    public List<Post> showNewsFeed() {
        return newsFeed.generateFeed(activeUsers.get(0));
    }
}