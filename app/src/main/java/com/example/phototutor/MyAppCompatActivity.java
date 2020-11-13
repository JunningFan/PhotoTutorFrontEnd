package com.example.phototutor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.phototutor.ui.login.ui.login.LoginActivity;

public class MyAppCompatActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState);
    }

    public void navigateToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);

        if(!sharedPreferences.contains("username")) {
            navigateToLogin();
        }
        super.onResume();
    }

    public int getPrimaryUserId(){
        if(!sharedPreferences.contains("accessToken") || sharedPreferences.getString("accessToken",null) == null) {
            navigateToLogin();
        }
        return sharedPreferences.getInt("userID",-1);
    }
}
