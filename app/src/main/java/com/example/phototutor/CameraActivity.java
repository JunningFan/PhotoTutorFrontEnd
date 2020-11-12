package com.example.phototutor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class CameraActivity extends MyAppCompatActivity {

    private boolean imageCaptured = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        NavController navController = Navigation.findNavController(this, R.id.camera_nav_host_fragment);

    }

    @Override
    public void onResume() {
        // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
        // be trying to set app to immersive mode before it's ready and the flags do not stick
        super.onResume();
        Log.w("CameraActivity","onResume");
        findViewById(R.id.activity_camera).postDelayed(new Runnable() {
            @Override
            public void run() {
                CameraActivity.this.findViewById(R.id.activity_camera)
                        .setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                                View.SYSTEM_UI_FLAG_FULLSCREEN  |
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |

                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION );
            }
        },500L);
    }

    public void setImageCaptured(boolean imageCaptured) {
        this.imageCaptured = imageCaptured;
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        if(imageCaptured) {
            setResult(Activity.RESULT_OK);
        }
        super.onBackPressed();
    }
}