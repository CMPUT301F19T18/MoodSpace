package com.example.moodspace;

import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public int getColor() {
        return Color.parseColor(colorCode);
    }

    public String getEmojiString() {
        return new String(Character.toChars(emojiCode));
    }

    public String getEmojiName() {
        return emojiName;
    }

    public String[] getEmojiList() {
        List<Emotion> emotionList = new ArrayList<>(Arrays.asList(Emotion.values()));
        // hack to remove null from filters
        emotionList.remove(Emotion.NULL);
        String[] emotionArray = new String[emotionList.size()];
        for (int i = 0; i < emotionList.size(); i++) {
            emotionArray[i] = emotionList.get(i).getEmojiName() + " " + emotionList.get(i).getEmojiString();
        }
        return emotionArray;
    }
}
