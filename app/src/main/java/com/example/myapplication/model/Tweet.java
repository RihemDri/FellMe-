package com.example.myapplication.model;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Tweet {
    private String tweet;
    private String image;
    private String id;
    private String userId;
    private String date; // Date as String
    private String time; // Time as String
    private List<String> likedBy; // List of user IDs who liked the tweet
    private List<Comment> comments; // List of comments
    private String predictionResult; // New column
    private long modelConfidence;  // New column


    // Default constructor is required for Firebase
    public Tweet() {
    }

    // Constructor
    public Tweet(String tweet, String image, String id, String userId, String date, String time, List<String> likedBy, List<Comment> comments, String predictionResult, long modelConfidence) {
        this.tweet = tweet;
        this.image = image;
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.time = time;
        this.likedBy = likedBy != null ? likedBy : new ArrayList<>();
        this.comments = comments != null ? comments : new ArrayList<>();
        this.predictionResult = predictionResult;
        this.modelConfidence = modelConfidence;
    }

    // Static method to create a Tweet with current date and time
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Tweet createTweet(String tweet, String id, String userId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        String dateString = today.format(dateFormatter);
        String timeString = now.format(timeFormatter);
        List<String> likes = new ArrayList<>();
        //likes.add(id);
        return new Tweet(
                tweet,
                id,
                id,
                userId,
                dateString,
                timeString,
                likes,
                new ArrayList<>(),
                "",
                0);
    }

    // Getters and Setters
    public String getTweet() {
        return tweet;
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public List<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public int getLikes() {
        if (likedBy == null) {
            return 0;
        }
        return likedBy.size(); // Number of likes is the size of the likedBy list
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getPredictionResult() {
        return predictionResult;
    }

    public void setPredictionResult(String predictionResult) {
        this.predictionResult = predictionResult;
    }

    public long getModelConfidence() {
        return modelConfidence;
    }

    public void setModelConfidence(long modelConfidence) {
        this.modelConfidence = modelConfidence;
    }


    public void addLike(String userId) {
        if (!likedBy.contains(userId)) {
            likedBy.add(userId);
        }
    }

    public void removeLike(String userId) {
        likedBy.remove(userId);
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "tweet='" + tweet + '\'' +
                ", image='" + image + '\'' +
                ", id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", likes=" + likedBy +
                ", comments=" + comments +
                ", predictionResult='" + predictionResult + '\'' +
                ", modelConfidence='" + modelConfidence + '\'' +
                '}';
    }
}
