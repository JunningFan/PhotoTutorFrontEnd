package com.example.phototutor.helpers;

import android.content.Context;

import com.example.phototutor.user.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Repeatable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserInfoDownloader extends ServerClient {

    private Context context;
    public UserInfoDownloader(Context context){
        this.context = context;
    }

    private static abstract class RequestCallback implements Callback<ResponseBody>{
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

    public static abstract class UserDetailRequestCallback extends RequestCallback{
        abstract public void onSuccessResponse(User user);

        @Override
        public void onSuccessResponse(JSONArray array) throws JSONException {}

        @Override
        public void onSuccessResponse(JSONObject object) throws JSONException, MalformedURLException {
            User user = UserInfoDownloader.extractUser(object);
            onSuccessResponse(user);
        }
    }

    public static abstract class UserfollowerRequestCallback extends UserfollowingRequestCallback{}
    public static abstract class UserfollowingRequestCallback extends RequestCallback{

        @Override
        protected boolean isArray() {
            return true;
        }

        abstract public void onSuccessResponse(List<User> users);

        @Override
        public void onSuccessResponse(JSONObject object) throws JSONException {}

        @Override
        public void onSuccessResponse(JSONArray array) throws JSONException, MalformedURLException {
            ArrayList<User> users = new ArrayList();
            for(int i=0;i<array.length();i++){
                User user = UserInfoDownloader.extractUser(array.getJSONObject(i));
                users.add(user);
            }

            onSuccessResponse(users);
        }
    }



    private static User extractUser(JSONObject object)throws JSONException, MalformedURLException {
        int id = object.getInt("ID");
        String username = object.getString("Username");
        String nickName = object.getString("Nickname");
        String signature = object.getString("Signature");
        String avatarUrl = object.getString("img");
        int nFollowers = object.getInt("NFollowers");
        int nFollowing = object.getInt("NFollowing");
        avatarUrl = getBaseURL()+ '/'+ avatarUrl;
        User user = new User(id, username, nickName,signature, avatarUrl, nFollowing, nFollowers);
        return user;
    }

    public void getPrimaryUserDetail(int id){

        //TODO

    }
    public void getUserDetail(int id,UserDetailRequestCallback callback){
        getService().getUserDetail(id).enqueue(callback);
    }

    public void downloadUserFollowing(int userId, UserfollowingRequestCallback callback){
        getService().getUserFollowing(userId).enqueue(callback);
    }

    public void downloadUserFollower(int userId, UserfollowerRequestCallback callback){
        getService().getUserFollowers(userId).enqueue(callback);
    }
}
