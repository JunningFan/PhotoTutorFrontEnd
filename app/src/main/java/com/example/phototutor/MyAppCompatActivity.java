package com.example.phototutor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phototutor.ui.login.ui.login.LoginActivity;

public class MyAppCompatActivity extends AppCompatActivity {


    public void navigateToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);

        if(!sharedPreferences.contains("username")) {
            navigateToLogin();
        }
        super.onResume();
    }
}
