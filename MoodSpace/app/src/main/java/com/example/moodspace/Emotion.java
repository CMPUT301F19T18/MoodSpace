package com.example.moodspace;

import android.graphics.Color;

public enum Emotion {
    HAPPY(0x1F604, "#badc58", "Happy"),
    ANGRY(0x1F621, "#eb4d4b", "Angry"),
    SAD(0x1F62D, "#00a8ff", "Sad");

    private final int emojiCode;
    private final String colorCode;
    private final String emojiName;

    Emotion(int emojiCode, String colorCode, String emojiName) {
        this.emojiCode = emojiCode;
        this.colorCode = colorCode;
        this.emojiName = emojiName;
    }

    public int getColor() {
        return Color.parseColor(colorCode);
    }

    public String getEmojiString() {
        return new String(Character.toChars(emojiCode));
    }

    public String getEmojiName() {
        return this.emojiName;
    }
}
