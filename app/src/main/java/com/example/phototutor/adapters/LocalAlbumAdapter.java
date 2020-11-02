package com.example.phototutor.adapters;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.phototutor.Photo.Photo;
import com.fivehundredpx.greedolayout.GreedoLayoutSizeCalculator;

public class LocalAlbumAdapter extends AlbumAdapter implements GreedoLayoutSizeCalculator.SizeCalculatorDelegate{
    public LocalAlbumAdapter(Context context) {
        super(context);
    }

    @Override
    public double aspectRatioForIndex(int i) {
        return 1.0;
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder)
    {
        super.onViewRecycled(holder);
        ImageView imageView = (ImageView) holder.imageView;
        if (imageView != null)
            Glide.get(context).clearMemory();
    }
}
