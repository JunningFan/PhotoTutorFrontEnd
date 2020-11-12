package com.example.phototutor.ui.navigation;

import com.google.gson.GsonBuilder;
import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MapsAPIClient {
    static private final String host_address = "https://maps.googleapis.com";
    public static final String MAP_API_KEY = "AIzaSyA7CGn_w-BdDvK41Nku2K9eB3FVyK3V31w";

    static private Retrofit retrofit = initRetrofit();
    static private APIServer service =  retrofit.create(APIServer.class);;

    static private Retrofit initRetrofit(){
        OkHttpClient client = new OkHttpClient.Builder()
                .protocols(Arrays.asList(Protocol.HTTP_1_1))
                .addInterceptor(new OkHttpProfilerInterceptor())
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(host_address)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .build();
        return retrofit;
    }

    static public String getBaseURL(){
        return host_address;
    }

    public APIServer getService(){
        return service;
    }

    public Retrofit getRetrofit(){
        return retrofit;
    }


    public interface APIServer {
        @GET("/maps/api/directions/json")
        Call<ResponseBody> getRouteFromTo(
                @Query("origin") String origin,
                @Query("destination") String destination,
                @Query("mode") String mode,
                @Query("key") String key
        );
    }
}
