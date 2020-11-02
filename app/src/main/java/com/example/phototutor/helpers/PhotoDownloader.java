package com.example.phototutor.helpers;

import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.example.phototutor.Photo.Photo;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoDownloader extends ServerClient {


    public interface OnPhotoDownloaded extends Callback<ResponseBody> {}
    private Context context;

    public PhotoDownloader(Context context){
        this.context = context;
    }

    public void downloadPhotosByGeo(OnPhotoDownloaded onPhotoDownloaded){

        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("from",0);
        jsonObj.addProperty("size",10);
        JsonObject query = new JsonObject();
        JsonObject geo_bounding_box = new JsonObject();
        JsonObject geoHash = new JsonObject();
        JsonObject bottom_right = new JsonObject();
        JsonObject top_left = new JsonObject();

        top_left.addProperty("lat",-32);
        top_left.addProperty("lon",149);
        bottom_right.addProperty("lat",-36);
        bottom_right.addProperty("lon",153);

        geoHash.add("top_left",top_left);
        geoHash.add("bottom_right",bottom_right);
        geo_bounding_box.add("GeoHash",geoHash);
        query.add("geo_bounding_box",geo_bounding_box);

        jsonObj.add("query",query);
        getService().getPhotosByEls(jsonObj).enqueue(onPhotoDownloaded);


    }

    public void downloadAllPhotos(OnPhotoDownloaded onPhotoDownloaded){


        getService().getAllPictures().enqueue(onPhotoDownloaded);

    }

    public void getPhotoById(int id){
        getService().getPhotoDetail(id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.w("getPhotoById", response.toString());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
