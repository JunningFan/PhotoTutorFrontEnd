package com.example.phototutor.helpers;

import android.net.Uri;

import com.example.phototutor.Photo.Photo;
import com.google.gson.GsonBuilder;
import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

class ServerClient{
    static private String host_address = "http://whiteboard.house:8000";

    static private Retrofit retrofit = initRetrofit();


    static private APIServer service =  retrofit.create(APIServer.class);;

    static private Retrofit initRetrofit(){
        OkHttpClient client = new OkHttpClient.Builder()
                .protocols(Arrays.asList(Protocol.HTTP_1_1))
                .addInterceptor(new OkHttpProfilerInterceptor())
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS).build();

        retrofit = new Retrofit.Builder()
                .baseUrl(host_address)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .build();
        return retrofit;

    }

    public APIServer getService(){
        return service;
    }

    public Retrofit getRetrofit(){
        return retrofit;
    }

    interface APIServer {

        @Multipart
        @POST("/upload/")
        Call<ResponseBody> uploadImage(
                @Header("Authorization") String authKey,
                @Part MultipartBody.Part photo
        );


        @POST("/picture/")
        Call<ResponseBody> uploadImageInfo(
                @Header("Authorization") String authKey,
                @Part RequestBody info
        );


        @GET("/upload/{id}")
        Call<ResponseBody> getPhotoInfo(
                @Path("id") Integer id);

    }
}




