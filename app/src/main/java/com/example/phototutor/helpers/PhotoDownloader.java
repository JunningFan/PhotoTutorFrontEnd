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
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoDownloader extends ServerClient {


    public abstract static class OnPhotoDownloaded implements Callback<ResponseBody>{
        abstract public void onFailResponse(String message,int code );

        abstract public void onFailRequest(Call<ResponseBody> call, Throwable t);

        abstract public void onSuccessResponse(JSONArray imageJSONs);

        private JSONArray getImageJSONs(JSONObject object) throws JSONException {
            JSONObject hits = object.getJSONObject("hits");
            JSONArray srcList = hits.getJSONArray("hits");
            JSONArray imageList = new JSONArray();
            for(int i =0; i<srcList.length();i ++) {
                JSONObject source = (JSONObject) srcList.get(i);
                imageList.put(source.getJSONObject("_source"));
            }
            return imageList;
        }

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if(response.code() != 200) onFailResponse(response.message(),response.code());
            else{

                try {
                    JSONObject data = null;
                    data = new JSONObject(response.body().string());
                    JSONArray imageJSONs = getImageJSONs(data);
                    onSuccessResponse(imageJSONs);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            onFailRequest(call,t);
        }
    }

    private Context context;

    public PhotoDownloader(Context context){
        this.context = context;
    }

    public void downloadPhotosByGeo(long lat, long lon, int from, int size, OnPhotoDownloaded onPhotoDownloaded){

        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("from",from);
        jsonObj.addProperty("size",size);
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
