package com.example.phototutor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProfileActivity extends AppCompatActivity {

    private String authKey ="eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJJRCI6MSwiQWNjZXNzIjp0cnVlLCJFeHBpcmUiOjE2MDQ0MTEzNzN9.EhSIO6etnZGoEwyyOkyNKH95e6QztUCh1oKIHnvRKcUhbDhiWIrr24k93yXMenbqqPc3-BYLDkhbaYpTnERXpA";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        final EditText name = findViewById(R.id.editTextName);
        final EditText bio = findViewById(R.id.editTextBio);
        final ImageView userImage = (ImageView) findViewById(R.id.userImage);
        final TextView changeProfilePhotoButton=(TextView)findViewById(R.id.changeProfilePhoto);
        final Button updateButton = findViewById(R.id.updateButton);

        int imageResource = getResources().getIdentifier("@drawable/avatar", null, this.getPackageName());
        userImage.setImageResource(imageResource);

//        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);


        changeProfilePhotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //perform your action here
                Log.e("onClick!", "change profile button is clicked");
                Intent intent = new Intent(EditProfileActivity.this, LocalAlbumActivity.class);
                startActivity(intent);
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Name = name.getText().toString();
                String Bio = bio.getText().toString();
                Integer Photo = 1;
                if(name.getText().toString().isEmpty()) {
                    Name = null;
                }

                if(bio.getText().toString().isEmpty()) {
                    Bio = null;
                }
                putDataToDatabase(Name, Bio, Photo);
                Toast.makeText(getApplicationContext(), "Details updated", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(EditProfileActivity.this, LocalAlbumActivity.class);
                startActivity(intent);
            }
        });
    }

    public void putDataToDatabase(String name, String bio, Integer photo) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://whiteboard.house:8000/user/";
        Log.d("OKHTTP3", "PUT Function called");
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject actualData = new JSONObject();
        try {
            actualData.put("Nickname", name);
            actualData.put("Signature", bio);
            actualData.put("Img", photo);

        } catch (JSONException e) {
            Log.d("OKHTTP3", "JSON Exception");
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON, actualData.toString());
        Log.d("OKHTTP3", "Request body created");
        Request newReq = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", authKey)
                .build();

        client.newCall(newReq).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("OKHTTP3", "Exception while doing request.");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("OKHTTP3", "Request Done, got the response.");
                Log.d("OKHTTP3", response.body().string());
            }
        });
    }
}