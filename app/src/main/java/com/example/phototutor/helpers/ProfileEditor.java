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

    public interface ProfileEditorCallback extends Callback<ResponseBody> {}
    private Context context;

    public ProfileEditor(Context context){
        this.context = context;
    }

    public void uploadDetails(String authKey, String nickname, String signature, Integer image){
        JSONObject userData = new JSONObject();
        try {
            userData.put("Nickname", nickname);
            userData.put("Signature", signature);
            // userData.put("img", image);
        } catch (JSONException e) {
            Log.d("OKHTTP3", "JSON Exception");
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(String.valueOf(userData),MediaType.parse("application/json"));
        getService().uploadUserData(authKey,requestBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("Profile Editor", "User details updated.");
                try {
                    Log.e("OKHTTP3", response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Profile Editor", "Fail to update user's details.");
            }
        });

    }
}
