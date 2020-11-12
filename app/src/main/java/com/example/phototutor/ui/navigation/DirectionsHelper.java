package com.example.phototutor.ui.navigation;

import okhttp3.ResponseBody;
import retrofit2.Callback;

public class DirectionsHelper extends MapsAPIClient {

    public DirectionsHelper() {
    }

    public void getWalkDirection(double from_lat, double from_lng, double to_lat, double to_lng, DirectionCallback cb) {
        getService().getRouteFromTo(from_lat + "," + from_lng,
                to_lat + "," + to_lng,
                "walking", MAP_API_KEY).enqueue(cb);
    }

    public interface DirectionCallback extends Callback<ResponseBody> {
    }
}
