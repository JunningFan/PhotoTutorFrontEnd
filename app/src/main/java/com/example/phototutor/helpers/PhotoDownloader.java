package com.example.phototutor.helpers;

import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.example.phototutor.Photo.Photo;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;

public class PhotoDownloader extends ServerClient {


    public interface OnPhotoDownloaded extends Callback<ResponseBody> {}
    private Context context;

    public PhotoDownloader(Context context){
        this.context = context;
    }

    public void downloadPhotos(OnPhotoDownloaded onPhotoDownloaded){
        getService().getPhotos().enqueue(onPhotoDownloaded);
    }
}
