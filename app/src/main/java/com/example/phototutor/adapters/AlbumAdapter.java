package com.example.phototutor.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;

import java.io.File;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    List<Photo> photoList;
    Context context;
    LayoutInflater inflater;
    private String TAG = "AlbumAdapter";
    public AlbumAdapter(Context context){
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(
                R.layout.album_photo_block,
                parent,
                false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumAdapter.ViewHolder holder, int position) {
        if (photoList != null) {
            Photo current = photoList.get(position);
//            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_camera_flip));
//            Glide.with(context)
//                    .load(current.imageURI)
//                    .placeholder(R.drawable.ic_loading)
//                    .thumbnail(0.5f)
//                    .into(holder.imageView);

            Glide.with(context)
                    .load(current.thumbnailURI)
                    .placeholder(R.drawable.ic_loading)
                    .into(holder.imageView);
            Log.w(TAG,"onBindViewHolder " + String.valueOf(position));

        } else {

        }
    }

    @Override
    public int getItemCount() {
        return photoList==null?0:photoList.size();
    }

    public void setPhotos(List<Photo> photos){
        Log.w(TAG, "photos:"+String.valueOf(photos.size()));
        photoList = photos;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        final ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_block);
        }
    }
}
