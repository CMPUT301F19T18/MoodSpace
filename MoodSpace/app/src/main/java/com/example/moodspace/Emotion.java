package com.example.moodspace;

import android.graphics.Color;

public enum Emotion {
    ENJOYMENT(1, 0x1F604, "#fcb52b", "Enjoyment"),
    ANGRY(2, 0x1F621, "#a03d3e", "Angry"),
    SAD(3, 0x1F62D, "#3e6baa", "Sadness"),
    DISGUST(4, 0x1F922,"#4f9429", "Disgust"),
    FEAR(5, 0x1F631, "#5a3b85", "Fear"),
    CONTEMPT(6, 0x1f612, "#e9663d", "Contempt"),
    SURPRISE(7, 0x1F62E, "#54eeee", "Surprise");



    private final int id;
    private final int emojiCode;
    private final int colorCode;
    private final String emojiName;

    Emotion(int id, int emojiCode, String colorCode, String emojiName) {
        this.id = id;
        this.emojiCode = emojiCode;
        this.colorCode = Color.parseColor(colorCode);
        this.emojiName = emojiName;
    }

    public int getId() {
        return id;
    }

    public int getColorCode() {
        return this.colorCode;
    }

    public String getEmojiString() {
        return new String(Character.toChars(emojiCode));
    }

    public String getEmojiName() {
        return this.emojiName;
    }

    public static String[] getEmojiList() {
        Emotion[] emotionArray = Emotion.values();
        String[] emotionStringArray = new String[emotionArray.length];
        for (int i = 0; i < emotionArray.length; i++) {
            emotionStringArray[i] = emotionArray[i].getEmojiName() + " " + emotionArray[i].getEmojiString();
        }
        return emotionStringArray;
    }

    @Override
    public String toString() {
        return "Emotion{" +
                "id=" + id +
                ", emojiCode=" + emojiCode +
                ", colorCode=" + colorCode +
                ", emojiName='" + emojiName + '\'' +
                '}';
    }
}
