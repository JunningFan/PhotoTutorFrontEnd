package com.example.phototutor.ui.localalbum;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UnitLocalPhotoDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UnitLocalPhotoDetailFragment extends Fragment {
    private Photo photo;
    public UnitLocalPhotoDetailFragment(Photo photo){
        this.photo = photo;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imageView = view.findViewById(R.id.image_view);

        Glide.with(this)
                .load(photo.imageURI)
                .placeholder(R.drawable.ic_loading)
                .into(imageView);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_unit_local_photo_detail, container, false);
    }
}