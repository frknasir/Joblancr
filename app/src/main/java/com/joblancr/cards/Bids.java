package com.joblancr.cards;

/**
 * Created by Faruk on 8/4/16.
 */
public class Bids {
    private String image;
    private String userId;
    private String ownerId;
    private String projectId;
    private String bidderName;
    private String time;
    private String offerText;
    private String bidId;

    public Bids(String image, String user_id, String owner_id, String project_id, String bidder_name,
                String time, String offer_text, String bidId) {
        setImage(image);
        setUserId(user_id);
        setOwnerId(owner_id);
        setProjectId(project_id);
        setBidderName(bidder_name);
        setTime(time);
        setOfferText(offer_text);
        setBidId(bidId);
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

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return this.projectId;
    }

    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
    }

    public String getBidderName() {
        return this.bidderName;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return this.time;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    public String getOfferText() {
        return this.offerText;
    }

    public void setBidId(String bidId) {
        this.bidId = bidId;
    }

    public String getBidId() {
        return this.bidId;
    }
}


