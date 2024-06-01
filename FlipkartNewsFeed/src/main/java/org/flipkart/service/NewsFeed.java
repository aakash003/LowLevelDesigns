package org.flipkart.service;

import org.flipkart.entity.Post;
import org.flipkart.entity.User;
import org.flipkart.util.UniqueIdGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class NewsFeed implements INewsFeedGenerator {
    private HashMap<String, Post> postHashMap = new HashMap<>();
    private static UniqueIdGenerator uniqueIdGenerator;

    public List<Post> generateFeed(User user) {
        if (postHashMap.isEmpty()) {
            return new ArrayList<>();
        }

        // Followers post
        Map<String, Post> userPostMap = new LinkedHashMap<>();
        Optional.ofNullable(user.getFollowedUsers()).orElse(Collections.emptyList())
                .stream()
                .map(User::getPosts)
                .filter(Objects::nonNull)
                .forEach(posts -> posts.forEach(post -> userPostMap.put(post.getPostId(), post)));

        List<Post> feed = sortPosts(new ArrayList<>(userPostMap.values()));

        //General Posts
        List<Post> feed1 = postHashMap.entrySet().stream()
                .filter(entry -> !userPostMap.containsKey(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        feed1 = sortPosts(feed1);
        feed.addAll(feed1);
        return feed;
    }

    private List<Post> sortPosts(List<Post> posts) {
        posts.sort((o1, o2) -> {
            // Compare based on score
            int o1Score = o1.getUpvotes() - o1.getDownvotes();
            int o2Score = o2.getUpvotes() - o2.getDownvotes();

            if (o1Score != o2Score) {
                return o2Score - o1Score; // Higher score comes first
            } else {
                // If scores are equal, compare based on number of comments
                int o1Comments = o1.getComments().size();
                int o2Comments = o2.getComments().size();

                if (o1Comments != o2Comments) {
                    return o2Comments - o1Comments; // More comments come first
                } else {
                    // If number of comments are equal, compare based on timestamp
                    return Long.compare(o2.getTimestamp(), o1.getTimestamp()); // More recent comes first
                }
            }
        });
        return posts;
    }

    public void addPost(Post post) {
        String id = UniqueIdGenerator.generateNewID();
        post.setPostId(id);
        postHashMap.put(id, post);
    }

    public Post getPost(String postId) {
        return postHashMap.get(postId);
    }
}
