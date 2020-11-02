package com.example.phototutor.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.phototutor.Photo.Photo;
import com.fivehundredpx.greedolayout.GreedoLayoutSizeCalculator;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class CloudAlbumAdapter extends AlbumAdapter implements GreedoLayoutSizeCalculator.SizeCalculatorDelegate {
    public CloudAlbumAdapter(Context context) {
        super(context);
    }

    private Bitmap getBitmapFromUrl(String src) throws IOException {
        URL url = new URL(src);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        Bitmap myBitmap = BitmapFactory.decodeStream(input);
        return myBitmap;
    }
    @Override
    public double aspectRatioForIndex(int i) {
        if(i < getItemCount()){
            Photo photo = photoList.get(i);
            return (double)photo.getWidth()/(double)photo.getHeight();
        }
        else{
            return 1;
        }
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