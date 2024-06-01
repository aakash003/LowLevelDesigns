package org.flipkart.entity;


import org.flipkart.service.NewsFeed;

import java.util.ArrayList;
import java.util.List;


public class User {

    private String email;
    private String password;
    private String name;
    private List<User> followedUsers;
    private List<Post> posts;
    private List<Comment> comments;
    private int upvotes;
    private int downvotes;
    private NewsFeed newsFeed;

    public User(String name) {
        this.name = name;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getFollowedUsers() {
        if(followedUsers==null)
            return new ArrayList<>();
        return followedUsers;
    }

    public void setFollowedUsers(List<User> followedUsers) {
        this.followedUsers = followedUsers;
    }

    public List<Post> getPosts() {
        if(posts==null)
            return new ArrayList<>();
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    public NewsFeed getNewsFeed() {
        return newsFeed;
    }

    public void setNewsFeed(NewsFeed newsFeed) {
        this.newsFeed = newsFeed;
    }


    // constructors, getters, setters, etc.
}