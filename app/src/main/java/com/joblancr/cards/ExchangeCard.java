package com.joblancr.cards;

/**
 * Created by Faruk on 8/11/16.
 */
public class ExchangeCard {
    private String name;
    private String exchange;
    private String image;
    private String time;
    private String userId;

    public ExchangeCard(String name, String image, String exchange, String time, String userId) {
        setName(name);
        setImage(image);
        setExchange(exchange);
        setTime(time);
        setUserId(userId);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getExchange() {
        return this.exchange;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return this.image;
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
