package com.example.moodspace;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class LocationController {

    public static BitmapDescriptor getColor(Emotion emotion) {
        switch (emotion) {
            case ENJOYMENT:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
            case SAD:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
            case ANGER:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            case FEAR:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
            case DISGUST:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            case CONTEMPT:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
            default:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
        }
    }


}
