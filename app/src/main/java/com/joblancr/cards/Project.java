package com.joblancr.cards;

/**
 * Created by Faruk on 8/4/16.
 */
public class Project {
    private String userId;
    private String id;
    private String title;
    private String budget;
    private String description;
    private String duration;
    private String location;
    private String bid;
    private String time;
    private int status;

    public Project(String user_id, String id, String title, String budget, String description,
            int duration, String location, int bid, String time, int status) {
        setUserId(user_id);
        setId(id);
        setTitle(title);
        setBudget(budget);
        setDescription(description);
        setDuration(""+duration);
        setLocation(location);
        setBid(""+bid);
        setTime(time);
        setStatus(status);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getBudget() {
        return this.budget;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDuration() {
        return this.duration;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return this.location;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getBid() {
        return this.bid;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return this.time;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }
}


