package org.flipkart.service;

import org.flipkart.entity.Post;
import org.flipkart.entity.User;

import java.util.List;

public interface INewsFeedGenerator {
    public List<Post> generateFeed(User user);
}
