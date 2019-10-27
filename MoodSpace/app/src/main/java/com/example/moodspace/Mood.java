package com.example.moodspace;

import java.io.Serializable;
import java.util.Date;

/**
 * This class contains the required parameters to create a Moode Object and their respective constructors to initialize.
 */
public class Mood implements Serializable {

    //Combined Date and time to datetime, which will be formatted with DateUtils to the respective forms when the app runs.
    private Emotion emotion;
    private Date dateTime;

    public Mood(Date dateTime, Emotion emotion) {
        this.emotion = emotion;
        this.dateTime = dateTime;
    }

    //Getters and Setters for Mood

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }

}
