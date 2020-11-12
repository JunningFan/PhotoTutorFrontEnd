package com.example.phototutor.helpers;

import android.content.Context;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserNotification extends ServerClient {
    private Context context;

    public UserNotification(Context context){
        this.context = context;
    }

    static public abstract class UserNotificationCallback implements Callback<ResponseBody> {
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

    public void getNotificationList(int userId, UserNotification.UserNotificationCallback callback){
        getService().getUserNotifications(getAuthorizationToken(context)).enqueue(callback);
    }
}
