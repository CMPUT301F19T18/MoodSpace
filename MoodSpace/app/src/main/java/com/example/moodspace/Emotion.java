package com.example.moodspace;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Emotion {
    NULL(0, "#9F9F9F", "Empty"),
    HAPPY(0x1F604, "#ffe9ab", "Happy"),
    ANGRY(0x1F621, "#FF8585", "Angry"),
    SAD(0x1F62D, "#6F94E3", "Sad");

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

    // yet again another temporary hack
    public static List<Emotion> getValuesNonNull() {
        List<Emotion> emotionList = new ArrayList<>(Arrays.asList(Emotion.values()));
        emotionList.remove(Emotion.NULL);
        return emotionList;
    }

    public static String[] getEmojiList() {
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
