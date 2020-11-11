package com.example.phototutor;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phototutor.ui.login.ui.login.LoginActivity;

public class MyAppCompatActivity extends AppCompatActivity {


    public void navigateToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
