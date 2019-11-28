package com.example.moodspace;

import java.util.Date;

/**
 * Simple storage class of Mood that also has a username
 */
public class MoodOther extends Mood {
    private String username;

    public MoodOther(String id, Date date, Emotion emotion, String reasonText, boolean hasPhoto,
                     SocialSituation socialSituation, String username, Double lat, Double lon) {

        super(id, date, emotion, reasonText, hasPhoto, socialSituation, lat, lon);
        this.username = username;
    }

    public static MoodOther fromMood(Mood mood, String username) {
        return new MoodOther(mood.getId(), mood.getDate(), mood.getEmotion(), mood.getReasonText(),
                mood.getHasPhoto(), mood.getSocialSituation(), username, mood.getLat(), mood.getLon());
    }

    public String getUsername() {
        return username;
    }
}
