package com.example.myapplication.model;

import java.util.List;

public class TweetHolder {
    private String tweet;
    private String date;
    private String time;
    private String tweetId;
    private int likes;
    private String userId;
    private String imageUrl;
    private String username;
    private String initials;
    private List<String> likedBy; // List of user IDs who liked the tweet
    private List<Comment> comments; // List of comments

    // Constructor
    public TweetHolder(String tweet, String date, String time, String tweetId, int likes, String userId, String imageUrl, String username, String initials, List<String> likedBy, List<Comment> comments) {
        this.tweet = tweet;
        this.date = date;
        this.time = time;
        this.tweetId = tweetId;
        this.likes = likes;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.username = username;
        this.initials = initials;  // Initialize initials based on username
        this.likedBy = likedBy;
        this.comments = comments;
    }

    // Getters and Setters
    public String getTweet() {
        return tweet;
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTweetId() {
        return tweetId;
    }

    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getInitials() {
        return initials;
    }
    public List<String> getLikedBy() {
        return likedBy;
    }
    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }
    public void addLike(String userId) {
        if (!likedBy.contains(userId)) {
            likedBy.add(userId);
        }
    }
    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    // No need to set initials directly; they are auto-generated based on username
}
