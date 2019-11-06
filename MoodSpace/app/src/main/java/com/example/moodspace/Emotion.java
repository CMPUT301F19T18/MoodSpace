package com.example.moodspace;

import android.graphics.Color;

public enum Emotion {
    NULL(0, "#7f8c8d", "Empty"),
    HAPPY(0x1F604, "#badc58", "Happy"),
    ANGRY(0x1F621, "#eb4d4b", "Angry"),
    SAD(0x1F62D, "#00a8ff", "Sad");

    private final int emojiCode;
    private final String colorCode;
    public final String emojiName;

    Emotion(int emojiCode, String colorCode, String emojiName) {
        this.emojiCode = emojiCode;
        this.colorCode = colorCode;
        this.emojiName = emojiName;
    }

    int getColor() {
        return Color.parseColor(colorCode);
    }

    String getEmojiString() {
        return new String(Character.toChars(emojiCode));
    }
}
