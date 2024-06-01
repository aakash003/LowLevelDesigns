package org.flipkart.entity;

import java.time.LocalDateTime;

public class Comment {
    private String content;

    private Long timestamp;
    private int upvotes;
    private int downvotes;
    private User author;

    public Comment(User user,String content) {
        this.content = content;
        timestamp  = System.currentTimeMillis();
        upvotes = 0;
        downvotes = 0;
        author = user;
    }


    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }



    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }



    // constructors, getters, setters, etc.
}