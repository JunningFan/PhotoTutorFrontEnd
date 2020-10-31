package com.example.phototutor.ui.localalbum;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;
import com.example.phototutor.helpers.PhotoUploader;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UploadFragment extends DialogFragment {

    private LocalAlbumViewModel mViewModel;
    private Photo photo;
    private String TAG = "UploadFragment";
    private String authKey ="eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJJRCI6MSwiQWNjZXNzIjp0cnVlLCJFeHBpcmUiOjE2MDQyMTIwNjZ9.DPWfY_5VAXDdOjPExC1_w7Og-uWfVOv9JvrK823Ld84MDVjgra8L1nALENuPwQfcQ4tXhs6SVV_Dz2XdzrNkrw";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int pos = getArguments().getInt("pos");
        mViewModel = ViewModelProviders.of(requireActivity()).get(LocalAlbumViewModel.class);
        mViewModel.getAllPhotos().observe(requireActivity(), photos ->{
            photo = photos.get(pos);
            ImageView imageView = view.findViewById(R.id.image_view);
            Glide.with(view)
                    .load(photo.imageURI)
                    .placeholder(R.drawable.ic_loading)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(imageView);


        });
        view.findViewById(R.id.btn_submit).setOnClickListener(
                view1 -> {
                    PhotoUploader photoUploader = new PhotoUploader(getContext());
                    photoUploader.uploadPhoto(authKey
                            ,
                            photo,
                            new PhotoUploader.PhotoUploaderCallback() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    Log.w(TAG,response.toString());
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Log.e(
                                            TAG,
                                            "---TTTT :: POST msg from server :: " + t.toString()
                                    );
                                }
                            }
                    );
//                    photoUploader.getPhoto("hi",9);
                }
        );
    }
}