/**
 * @author Manu Parashar
 * This file contains the unit tests for the Emotion enum
 * @see com.example.moodspace.Emotion
 */
package com.example.moodspace;

import org.junit.*;
import java.util.Arrays;

import static org.junit.Assert.*;

public class EmotionTest {

    /**
     * Checks if the getEmojiList function of Emotion enum works appropriately
     */
    @Test
    public void CheckGetEmojiList(){

        String[] emotionList =  Emotion.getEmojiList();
        assertEquals("[Enjoyment 😄, Anger 😡, Sadness 😭, Disgust 🤢, Fear 😱, Contempt 😒, Surprise 😮]", Arrays.toString(emotionList));

    }
}
