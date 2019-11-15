package com.example.moodspace;

import android.graphics.Color;

public enum Emotion {
    HAPPY(1, 0x1F604, "#ffe9ab", "Happy"),
    ANGRY(2, 0x1F621, "#FF8585", "Angry"),
    SAD(3, 0x1F62D, "#6F94E3", "Sad");

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
