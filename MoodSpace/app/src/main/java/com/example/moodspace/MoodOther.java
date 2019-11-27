package com.example.moodspace;

import java.util.Date;

/**
 * Simple storage class of Mood that also has a username
 */
public class MoodOther extends Mood {
    private String username;

    public MoodOther(String username, String id, Date date, Emotion emotion, String reasonText,
                     boolean hasPhoto, boolean hasLocation,
                     SocialSituation socialSituation, double lat, double lon) {

        super(id, date, emotion, reasonText, hasPhoto, hasLocation, socialSituation, lat, lon);
        this.username = username;
    }

    public static MoodOther fromMood(Mood mood, String username) {
        return new MoodOther(username, mood.getId(), mood.getDate(), mood.getEmotion(), mood.getReasonText(),
                mood.getHasPhoto(), mood.getHasLocation(), mood.getSocialSituation(),
                mood.getLat(), mood.getLon());
    }

    public String getUsername() {
        return username;
    }
}
