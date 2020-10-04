package com.example.phototutor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class CameraActivity<FLAGS_FULLSCREEN> extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Log.d("Camera Activity",findViewById(R.id.activity_camera).toString());
    }

    @Override
    public void onResume() {
        // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
        // be trying to set app to immersive mode before it's ready and the flags do not stick
        super.onResume();
        findViewById(R.id.activity_camera).postDelayed(new Runnable() {
            @Override
            public void run() {
                CameraActivity.this.findViewById(R.id.activity_camera)
                        .setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                                View.SYSTEM_UI_FLAG_FULLSCREEN  |
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        },500L);
    }



}