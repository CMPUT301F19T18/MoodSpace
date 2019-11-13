package com.example.moodspace;

import java.util.Date;

public class MoodOther extends Mood {
    private String username;

    public MoodOther(String id, Date date, Emotion emotion, String reasonText, boolean hasPhoto,
                     SocialSituation socialSituation, String username) {

        super(id, date, emotion, reasonText, hasPhoto, socialSituation);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
