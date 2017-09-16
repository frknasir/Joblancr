package com.joblancr.cards;

/**
 * Created by Faruk on 8/11/16.
 */

public class ReviewCard {
    private String image;
    private String userId;
    private String name;
    private double rating;
    private String time;
    private String reviewComment;

    public ReviewCard(String image, String userId, String name, double rating, String time, String reviewComment) {
        setImage(image);
        setUserId(userId);
        setName(name);
        setRating(rating);
        setTime(time);
        setReviewComment(reviewComment);
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return this.image;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getRating() {
        return this.rating;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return this.time;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public String getReviewComment() {
        return this.reviewComment;
    }
}
