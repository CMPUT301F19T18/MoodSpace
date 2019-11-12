package com.example.moodspace;

import android.graphics.Color;

public enum Emotion {
    HAPPY(1, 0x1F604, "#badc58", "Happy"),
    ANGRY(2, 0x1F621, "#eb4d4b", "Angry"),
    SAD(3, 0x1F62D, "#00a8ff", "Sad");

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
}
