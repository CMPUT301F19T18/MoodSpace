package com.example.moodspace;

public enum EmotionWithNull {
    NULL(0),
    ENJOYMENT(1),
    ANGER(2),
    SAD(3),
    DISGUST(4),
    FEAR(5),
    CONTEMPT(6),
    SURPRISE(7);

    private final int id;

    EmotionWithNull(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Emotion toEmotion() {
        if (this == NULL) {
            throw new RuntimeException("Cannot convert null EmotionWithNull into Emotion");
        }
        return Emotion.valueOf(this.name());
    }

    @Override
    public String toString() {
        return "Emotion{" +
                "id=" + id +
                '}';
    }


}
