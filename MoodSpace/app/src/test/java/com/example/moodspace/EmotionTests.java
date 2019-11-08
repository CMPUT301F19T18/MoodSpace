package com.example.moodspace;

import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.moodspace.Emotion.ANGRY;
import static com.example.moodspace.Emotion.HAPPY;
import static com.example.moodspace.Emotion.SAD;
import static org.junit.Assert.*;

public class EmotionTests {

    @Test
    public void CheckGetEmojiList(){

        String[] emotionList =  Emotion.HAPPY.getEmojiList();
        assertEquals("[Happy 😄, Angry 😡, Sad 😭]", Arrays.toString(emotionList));

    }
}
