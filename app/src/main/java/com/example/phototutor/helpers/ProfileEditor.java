package com.example.phototutor.helpers;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileEditor extends ServerClient  {

    public static abstract class ProfileEditorCallback implements Callback<ResponseBody> {
        abstract public void onFailResponse(String message,int code );

        abstract public void onFailRequest(Call<ResponseBody> call, Throwable t);

        abstract public void onSuccessResponse();

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if(response.isSuccessful()){
                onSuccessResponse();

            }else {
                onFailResponse(response.message(),response.code());
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            onFailRequest(call, t);
        }
    }
    private Context context;

    public ProfileEditor(Context context){
        this.context = context;
    }

    public void uploadDetails( String nickname, String signature, int imgId, ProfileEditorCallback callback) {
        JSONObject userData = new JSONObject();
        try {
            userData.put("Nickname", nickname);
            userData.put("Signature", signature);
            userData.put("Img", imgId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(String.valueOf(userData),MediaType.parse("application/json"));
        getService().uploadUserData(getAuthorizationToken(context),requestBody).enqueue(callback);
    }


    public void uploadDetails( String nickname, String signature, ProfileEditorCallback callback){
        JSONObject userData = new JSONObject();
        try {
            userData.put("Nickname", nickname);
            userData.put("Signature", signature);
        } catch (JSONException e){
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(String.valueOf(userData),MediaType.parse("application/json"));
        getService().uploadUserData(getAuthorizationToken(context),requestBody).enqueue(callback);

    }
}
