package com.example.moodspace;

import java.io.Serializable;
import java.util.Date;

/**
 * This class contains the required parameters to create a Moode Object and their respective constructors to initialize.
 */
public class Mood implements Serializable {

    //Combined Date and time to datetime, which will be formatted with DateUtils to the respective forms when the app runs.
    private String mood;
    private Date dateTime;
    private String reason;
    private String emotionalState;

    public Mood(String mood, Date dateTime, String reason, String emotionalState) {
        this.mood = mood;
        this.dateTime = dateTime;
        this.reason = reason;
        this.emotionalState = emotionalState;
    }

    //Getters and Setters for Mood

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getEmotionalState() {
        return emotionalState;
    }

    public void setEmotionalState(String emotionalState) {
        this.emotionalState = emotionalState;
    }


}
