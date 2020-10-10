package com.example.phototutor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;

import com.example.phototutor.adapters.AlbumAdapter;
import com.example.phototutor.ui.localalbum.LocalAlbumFragment;

public class LocalAlbumActivity extends AppCompatActivity {
    private String TAG = "LocalAlbumActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_album);
        Log.w(TAG, "onCreate");

    }
}