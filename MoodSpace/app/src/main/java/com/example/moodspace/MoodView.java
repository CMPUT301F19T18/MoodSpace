package com.example.moodspace;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Simple storage class of Mood that also has a username
 */
public class MoodView extends Mood {
    private String username;

    public MoodView(String id, Date date, Emotion emotion, String reasonText,
                    boolean hasPhoto, boolean hasLocation,
                    SocialSituation socialSituation, Double lat, Double lon, String username) {

        super(id, date, emotion, reasonText, hasPhoto, hasLocation, socialSituation, lat, lon);
        this.username = username;
    }

    public static MoodView fromMood(Mood mood, String username) {
        return new MoodView(mood.getId(), mood.getDate(), mood.getEmotion(),
                mood.getReasonText(), mood.getHasPhoto(), mood.getHasLocation(),
                mood.getSocialSituation(), mood.getLat(), mood.getLon(), username);
    }

    public static List<MoodView> addUsernameToMoods(List<Mood> moodList, String username) {
        List<MoodView> moodViewList = new ArrayList<>();
        for (Mood mood : moodList) {
            moodViewList.add(MoodView.fromMood(mood, username));
        }
        return moodViewList;
    }

    public String getUsername() {
        return username;
    }
}
