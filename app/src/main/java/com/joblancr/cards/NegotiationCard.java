package com.joblancr.cards;

/**
 * Created by Faruk on 8/11/16.
 */
public class NegotiationCard {
    private String photoID;
    private String title;
    private String lastExchange;
    private String time;
    private String selectedBidder;
    private String projectOwner;
    private String negotiationId;
    private int exchangeStatus;

    public NegotiationCard(String photoID, String title, String lastExchange, String time,
                           String selectedBidder, String negotiationId, int exchangeStatus,
                           String projectOwner) {
        setPhotoID(photoID);
        setTitle(title);
        setLastExchange(lastExchange);
        setTime(time);
        setSelectedBidder(selectedBidder);
        setNegotiationId(negotiationId);
        setExchangeStatus(exchangeStatus);
        setProjectOwner(projectOwner);
    }

    public void setPhotoID(String photoID) {
        this.photoID = photoID;
    }

    public String getPhotoID() {
        return this.photoID;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setLastExchange(String lastExchange) {
        this.lastExchange = lastExchange;
    }

    public String getLastExchange() {
        return this.lastExchange;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return this.time;
    }

    public void setSelectedBidder(String selectedBidder) {
        this.selectedBidder = selectedBidder;
    }

    public String getSelectedBidder() {
        return this.selectedBidder;
    }

    public void setNegotiationId(String negotiationId) {
        this.negotiationId = negotiationId;
    }

    public String getNegotiationId() {
        return this.negotiationId;
    }

    public void setExchangeStatus(int exchangeStatus) {
        this.exchangeStatus = exchangeStatus;
    }

    public int getExchangeStatus() {
        return this.exchangeStatus;
    }

    public void setProjectOwner(String projectOwner) {
        this.projectOwner = projectOwner;
    }

    public String getProjectOwner() {
        return this.projectOwner;
    }
}
