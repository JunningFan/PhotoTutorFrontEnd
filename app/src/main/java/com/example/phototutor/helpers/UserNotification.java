package com.example.phototutor.helpers;

import android.content.Context;
import android.util.Log;

import com.example.phototutor.notification.Notification;
import com.example.phototutor.user.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserNotification extends ServerClient {
    private Context context;

    public UserNotification(Context context){
        this.context = context;
    }

    public abstract static class NotificationOnDownloadSuccessCallback extends RequestCallback{

        public abstract void onSuccessResponse(List<Notification> notifications);
        @Override
        public void onSuccessResponse(JSONObject object) throws JSONException, MalformedURLException {
            JSONArray array = object.getJSONArray("data");
            onSuccessResponse(array);

        }

        @Override
        public void onSuccessResponse(JSONArray array) throws JSONException, MalformedURLException {
            List<Notification> notifications = new ArrayList<>();
            for(int i=0;i<array.length();i++){
                Notification notification =  saveNotification(array.getJSONObject(i));
                notifications.add(notification);
                onSuccessResponse(notifications);
            }
        }
    }

    private static abstract class RequestCallback implements Callback<ResponseBody> {
        abstract public void onFailResponse(String message,int code );

        abstract public void onFailRequest(Call<ResponseBody> call, Throwable t);

        abstract public void onSuccessResponse(JSONObject object) throws JSONException, MalformedURLException;
        abstract public void onSuccessResponse(JSONArray array) throws JSONException, MalformedURLException;

        protected boolean isArray(){
            return false;
        }

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if(!response.isSuccessful()){
                onFailResponse(response.message(),response.code());
                return;
            }
            try {
                if(!isArray()) {
                    onSuccessResponse(new JSONObject(response.body().string()));
                }
                else{
                    onSuccessResponse(new JSONArray(response.body().string()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            onFailRequest(call,t);
        }
    }

    public void getNotificationList(RequestCallback callback){
        getService().getUserNotifications(getAuthorizationToken(context)).enqueue(callback);
//        return getService().getUserNotifications(authKey);
    }

    private static  Notification saveNotification(JSONObject object)throws JSONException, MalformedURLException {
        int notificationID = object.getInt("ID");
        int userID = object.getInt("UID");
        int actor = object.getInt("Actor");
        String type = object.getString("Type");
        String message = object.getString("Message");
        String datetime = object.getString("CreatedAt");
//        URL avatarURL = new URL(object.getString("Avatar"));

        Log.e("Notification object", object.toString());

        Notification notification = new Notification(notificationID,userID,actor,type,message,datetime);

        return notification;
    }
}
