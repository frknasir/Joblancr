package com.joblancr.cards;

/**
 * Created by Faruk on 8/11/16.
 */
public class SummaryCard {
    private String summary;

    public SummaryCard(String summary) {
        setSummary(summary);
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return this.summary;
    }
}
