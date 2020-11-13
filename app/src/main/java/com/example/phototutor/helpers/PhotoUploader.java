package com.example.phototutor.helpers;

import android.content.Context;
import android.location.Geocoder;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.core.content.FileProvider;

import com.example.phototutor.BuildConfig;
import com.example.phototutor.Photo.Photo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoUploader extends ServerClient {

    public static abstract  class PhotoInfoUploaderCallback implements Callback<ResponseBody>{

        abstract public void onFailResponse(String message,int code );

        abstract public void onFailRequest(Call<ResponseBody> call, Throwable t);

        abstract public void onSuccessResponse();

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if(!response.isSuccessful()){
                onFailResponse(response.message(),response.code());
            }
            else{
                onSuccessResponse();
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            onFailRequest(call,t);
        }

    }

    public static abstract class PhotoUploaderCallback implements Callback<ResponseBody> {

        abstract public void onFailResponse(String message,int code );

        abstract public void onFailRequest(Call<ResponseBody> call, Throwable t);

        abstract public void onSuccessResponse(int imgId);

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if(!response.isSuccessful()){
                onFailResponse(response.message(),response.code());
            }
            else{
                try {
                    String responseBodyString = response.body().string();
                        onSuccessResponse(new JSONObject(responseBodyString).getInt("img"));
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

    public PhotoUploader(Context context){
        this.context = context;
    }

    public void uploadPhoto(Photo photo, PhotoUploaderCallback callback){

        File photoFile = new File(photo.imageURI.getPath());


        String type = "";
        if(photo.imageURI.toString().startsWith("content")){
           type = context.getContentResolver().getType(photo.imageURI);
        }
        else{
            String extention = MimeTypeMap.getFileExtensionFromUrl(photoFile.getPath());
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extention);
        }

        Log.w("PhotoUploader", type);
        Log.w("PhotoUploader", photoFile.getAbsolutePath());
        RequestBody requestPhotoFile = RequestBody.create(
                photoFile, MediaType.parse(type));

        Log.w("PhotoUploader", photoFile.getPath());
        MultipartBody.Part body = MultipartBody.Part.createFormData(
                "upload",
                photoFile.getName(),
                requestPhotoFile);

        Log.w("PhotoUploader",body.headers().toString());
        getService().uploadImage(getAuthorizationToken(context) ,body).enqueue(callback);

    }

    public void uploadPhotoInfo(Photo photo, int id, String title, String description, String[] tags, String weather, PhotoInfoUploaderCallback callback){
        File photoFile = new File(photo.imageURI.getPath());

        JSONObject info = new JSONObject();
        try {
            info.put("title", title);
            info.put("Img", id);
            info.put("lat", photo.getLatitude());
            info.put("lng", photo.getLongitude());
            info.put("Iso", photo.iso);
            info.put("FocalLength", photo.focal_length);
            info.put("Aperture", photo.aperture);
            info.put("ShutterSpeed", photo.shutter_speed);
            info.put("Orientation", photo.getOrientation());
            info.put("Elevation", photo.getElevation());
            info.put("Timestamp", photo.timestamp/1000); // convert millisecs to unix standard
            info.put("Weather", weather);
            info.put("body", description);
            JSONArray tagsJsonArr = new JSONArray();
            for(String tag: tags) {
                tagsJsonArr.put(tag);
            }
            info.put("Tags", tagsJsonArr);
            Geocoder geocoder = new Geocoder(context);
            try {
                String country = (geocoder.getFromLocation(photo.getLatitude(), photo.getLongitude(), 1)).get(0).getCountryName();
                String adminArea =  (geocoder.getFromLocation(photo.getLatitude(), photo.getLongitude(), 1)).get(0).getAdminArea();
                String locality = (geocoder.getFromLocation(photo.getLatitude(), photo.getLongitude(), 1)).get(0).getLocality();
                JSONObject geo = new JSONObject();
                geo.put("Country", country);
                geo.put("State", adminArea);
                geo.put("City", locality);
                info.put("Location", geo);
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
            }

            Log.d(this.getClass().getSimpleName(), info.toString());

        } catch (JSONException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }

        RequestBody requestBody = RequestBody.create(String.valueOf(info), MediaType.parse("application/json"));
        getService().uploadImageInfo(getAuthorizationToken(context),requestBody).enqueue(callback);
    }

    public void getPhoto(int id){

        getService().getPhotoInfo(id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.w("PhotoUploader",response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                return;
            }
        });

    }
}
