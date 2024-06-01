package org.flipkart.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Post {
    public String getPostId() {
        return postId;
    }

    private String postId;
    private String content;
    private Long timestamp;
    private List<Comment> comments;
    private int upvotes;
    private int downvotes;
    private User author;

    public Post(User user,String content) {
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        comments = new ArrayList<>();
        upvotes = 0;
        downvotes = 0;
        this.author = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public List<Comment> getComments() {
        if(comments==null)
            return new ArrayList<>();
        return comments;
    }

    public String getCommentListAsString() {
        StringBuilder sb = new StringBuilder();
        for (Comment comment : comments) {
            sb.append(comment.getContent()).append(" ");
        }
        return sb.toString();
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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public void setPostId(String id) {
        this.postId = id;
    }


    // constructors, getters, setters, etc.
}