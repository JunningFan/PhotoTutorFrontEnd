package com.example.phototutor.helpers;

import android.content.Context;

import com.example.phototutor.comment.Comment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoLikeHelper extends ServerClient {

    private Context context;

    public PhotoLikeHelper(Context context) {
        this.context = context;
    }

    public static abstract class LikeRequestSuccessCallback implements Callback<ResponseBody> {
        abstract public void onFailResponse(String message,int code );

        abstract public void onFailRequest(Call<ResponseBody> call, Throwable t);

        abstract public void onSuccessResponse(String message);


        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if(response.isSuccessful()){
                try {
                    JSONObject object = new JSONObject(response.body().string());
                    onSuccessResponse(object.getString("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }else {
                onFailResponse(response.message(),response.code());
            }
        }
        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            onFailRequest(call, t);
        }
    }

    public void likePhoto(int photoId,LikeRequestSuccessCallback callback){
        getService().likePhoto(getAuthorizationToken(context),photoId).enqueue(callback);
    }

    public void dislikePhoto(int photoId,  LikeRequestSuccessCallback callback){
        getService().dislikePhoto(getAuthorizationToken(context),photoId).enqueue(callback);
    }

    public void removeLikePhoto(int photoId,LikeRequestSuccessCallback callback){
        getService().cancelLikePhoto(getAuthorizationToken(context),photoId).enqueue(callback);
    }

    public void removeDislikePhoto(int photoId,LikeRequestSuccessCallback callback){
        getService().cancelDislikePhoto(getAuthorizationToken(context),photoId).enqueue(callback);
    }
}
