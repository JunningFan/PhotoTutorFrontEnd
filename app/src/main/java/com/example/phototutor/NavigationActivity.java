package com.example.phototutor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class NavigationActivity extends MyAppCompatActivity {

    AppCompatActivity self;

    Bitmap bitmap;
    double latitude;
    double longitude;
    double orientation;
    double elevation;

    public final static int CAMERA_REQUEST_CODE = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        self = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        BottomNavigationView nav = findViewById(R.id.nav_view);
        nav.setSelectedItemId(R.id.navigation_map);
        BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
            switch (item.getItemId()) {
                case R.id.navigation_map:
                    Navigation.findNavController(self, R.id.nav_host_fragment).navigate(R.id.navigation_nav_map);
                    return true;
                case R.id.navigation_ar:
                    Navigation.findNavController(self, R.id.nav_host_fragment).navigate(R.id.navigation_nav_ar);
                    return true;
                case R.id.navigation_orientation:
                    Navigation.findNavController(self, R.id.nav_host_fragment).navigate(R.id.navigation_nav_orientation);
                    return true;
                default:

            }
            return false;
        };
        //setup photo data

        final Bundle fromIntent = getIntent().getExtras();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    Log.w("navActi", (fromIntent.getString("photoPath")));
                    url = new URL(fromIntent.getString("photoPath"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(input);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        orientation = fromIntent.getDouble("orientation");
        elevation = fromIntent.getDouble("elevation");
        latitude = fromIntent.getDouble("latitude");
        longitude = fromIntent.getDouble("longitude");

        Toast.makeText(this, "p: " + Double.toString(elevation), Toast.LENGTH_SHORT).show();

        nav.setOnNavigationItemSelectedListener(navListener);
    }

    public interface RequestPhotoData {
        public void receivePhotoBitMap(Bitmap bitmap);
        public void receiveCoordinate(double latitude, double longitude);
        public void receiveOrientationElevation(double orientation, double elevation);
    }

    public void requestPhotoData(RequestPhotoData fragment) {
        fragment.receivePhotoBitMap(bitmap);
        fragment.receiveCoordinate(latitude, longitude);
        fragment.receiveOrientationElevation(orientation, elevation);
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
            onBackPressed();
    }
}
