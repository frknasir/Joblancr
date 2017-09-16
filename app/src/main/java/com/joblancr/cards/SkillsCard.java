package com.joblancr.cards;

/**
 * Created by Faruk on 8/11/16.
 */
public class SkillsCard {
    private String skills;

    public SkillsCard(String skills) {
        setSkills(skills);
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getSkills() {
        return this.skills;
    }
}
