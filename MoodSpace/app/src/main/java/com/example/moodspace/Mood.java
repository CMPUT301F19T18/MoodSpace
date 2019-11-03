package com.example.moodspace;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;

/**
 * This class contains the required parameters to create a Mood Object and their respective constructors to initialize.
 */
public class Mood implements Serializable {

    //Combined Date and time to datetime, which will be formatted with DateUtils to the respective forms when the app runs.
    private String id;
    private Emotion emotion;
    private Date date;

    public Mood(String id, Date date, Emotion emotion) {
        this.id = id;
        this.emotion = emotion;
        this.date = date;
    }

    //Getters and Setters for Mood

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }

}
