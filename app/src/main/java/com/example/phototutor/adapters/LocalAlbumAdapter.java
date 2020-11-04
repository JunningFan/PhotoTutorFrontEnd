package com.example.phototutor.adapters;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.phototutor.Photo.Photo;
import com.fivehundredpx.greedolayout.GreedoLayoutSizeCalculator;

public class LocalAlbumAdapter extends AlbumAdapter<Photo> implements GreedoLayoutSizeCalculator.SizeCalculatorDelegate{
    public LocalAlbumAdapter(Context context) {
        super(context);
    }

    @Override
    public double aspectRatioForIndex(int i) {
        return 1.0;
    }


}
