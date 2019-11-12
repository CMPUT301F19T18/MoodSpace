package com.example.moodspace;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Emotion {
    NULL(0, 0, "#7f8c8d", "Empty"),
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
