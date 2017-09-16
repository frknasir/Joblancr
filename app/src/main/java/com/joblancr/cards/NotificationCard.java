package com.joblancr.cards;

/**
 * Created by Faruk on 8/11/16.
 */
public class NotificationCard {
    private String userId;
    private String image;
    private String messageText;
    private String time;

    public NotificationCard(String image, String messageText, String time, String userId) {
        setImage(image);
        setMessageText(messageText);
        setTime(time);
        setUserId(userId);
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return this.image;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageText() {
        return this.messageText;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return this.time;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }
}
