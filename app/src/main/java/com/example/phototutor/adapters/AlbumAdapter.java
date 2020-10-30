package com.example.phototutor.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.io.File;
import java.util.List;
import java.util.Random;

import jp.wasabeef.glide.transformations.CropTransformation;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    List<Photo> photoList;
    Context context;
    LayoutInflater inflater;
    private ImageGridOnClickCallBack imageGridOnClickCallBack;
    private String TAG = "AlbumAdapter";

    public interface ImageGridOnClickCallBack {
        void run(int pos);
    }

    public AlbumAdapter(Context context ){
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }
    public void setImageGridOnClickCallBack(ImageGridOnClickCallBack callback){
        imageGridOnClickCallBack = callback;
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
                    .load(current.imageURI)
                    .placeholder(R.drawable.ic_loading)

                    .thumbnail(0.1f)

                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ViewGroup.LayoutParams lp = holder.imageView.getLayoutParams();
                            if (lp instanceof FlexboxLayoutManager.LayoutParams) {
                                Log.w("ResourceReady","" +resource.getIntrinsicHeight()+' ' + resource.getIntrinsicWidth() );
                                FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams)lp;
//                                flexboxLp.setHeight(resource.getIntrinsicHeight());
//                                flexboxLp.setWidth(resource.getIntrinsicWidth());
                                flexboxLp.setFlexGrow(1.e4f);
                            }
                            return false;
                        }
                    })
//                    .apply(RequestOptions.bitmapTransform(new CropTransformation(1920,1080)))

                    .into(holder.imageView);
//
//            if(Math.random() > 0.5){
//                holder.imageView.setRotation(90);
//            }



            holder.imageView.setOnClickListener(
                    view -> {
                        imageGridOnClickCallBack.run(position);
                    }

            );


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
