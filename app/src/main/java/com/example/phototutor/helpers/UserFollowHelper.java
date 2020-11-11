package com.example.phototutor.helpers;

import android.content.Context;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserFollowHelper extends ServerClient {
    private Context context;

    public UserFollowHelper(Context context){
        this.context = context;
    }

    static public abstract class UserFollowActionCallback implements Callback<ResponseBody> {
        abstract public void onFailResponse(String message,int code );

        abstract public void onFailRequest(Call<ResponseBody> call, Throwable t);

        abstract public void onSuccessResponse();

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if(response.isSuccessful()){
                onSuccessResponse();
            }
            else{
                onFailResponse(response.message(),response.code());
            }

        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            onFailRequest(call, t);
        }
    }

    public void removeFollow(int userId, UserFollowActionCallback callback){
        getService().removeFollower(getAuthorizationToken(context), userId).enqueue(callback);
    }
    public void addFollow(int userId, UserFollowActionCallback callback){
        getService().addFollower(getAuthorizationToken(context), userId).enqueue(callback);
    }
}
