package com.example.phototutor.cameraFragment;

import android.util.Log;

import com.example.phototutor.Photo.Photo;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public class WeatherGetter  {
    static private String host_address = "http://api.openweathermap.org/data/2.5/weather?";
    static private String openWeatherAPIKey = "4da4e693f83e9293ffb90c926b750203";

    private boolean hasRespond = false;


    public String getWeather(double lat, double lng) {
        String weather = Photo.UNKNOWN;

        OkHttpClient client = new OkHttpClient();
        Log.d("OKHTTP3", "Request body created");
        Request newReq = new Request.Builder()
                .url(host_address+"lat="+Double.toString(lat)+"&lon="+Double.toString(lng)+"&appid="+openWeatherAPIKey)
                .get()
                .build();

        hasRespond = false;

        client.newCall(newReq).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d("OKHTTP3", "Exception while doing request.");
                e.printStackTrace();
                hasRespond = true;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    Log.d(this.getClass().getSimpleName(), response.toString());
                }
                hasRespond = true;
            }
        });

        while(!hasRespond){};


        return weather;
    }

}
