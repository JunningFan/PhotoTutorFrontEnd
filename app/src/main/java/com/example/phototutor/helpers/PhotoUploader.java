package com.example.phototutor.helpers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.core.content.FileProvider;

import com.example.phototutor.BuildConfig;
import com.example.phototutor.Photo.Photo;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoUploader extends ServerClient {

    public interface PhotoUploaderCallback extends Callback<ResponseBody>{}
    private Context context;

    public PhotoUploader(Context context){
        this.context = context;
    }
    public void uploadPhoto(String authKey, Photo photo, PhotoUploaderCallback callback){
            File photoFile = new File(photo.imageURI.getPath());

            String extention = MimeTypeMap.getFileExtensionFromUrl(photoFile.getPath());
            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extention);
            Log.w("PhotoUploader", type);
            RequestBody requestPhotoFile = RequestBody.create(
                    photoFile, MediaType.parse(type));


            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "upload",
                    photoFile.getName(),
                    requestPhotoFile);

            Log.w("PhotoUploader",body.headers().toString());
            getService().uploadImage(authKey,body).enqueue(callback);

    }

    public void getPhoto(String authKey, int id){

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
