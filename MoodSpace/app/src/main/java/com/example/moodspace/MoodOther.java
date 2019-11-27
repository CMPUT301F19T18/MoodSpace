package com.example.moodspace;

import java.util.Date;

/**
 * Simple storage class of Mood that also has a username
 */
public class MoodOther extends Mood {
    private String username;

    public MoodOther(String id, Date date, Emotion emotion, String reasonText, boolean hasPhoto, boolean locationOn,
                     SocialSituation socialSituation, String username) {

        super(id, date, emotion, reasonText, hasPhoto, locationOn, socialSituation);
        this.username = username;
    }

    public static MoodOther fromMood(Mood mood, String username) {
        return new MoodOther(mood.getId(), mood.getDate(), mood.getEmotion(), mood.getReasonText(),
                mood.getHasPhoto(), mood.getLocationOn(), mood.getSocialSituation(), username);
    }

    public String getUsername() {
        return username;
    }
}
