package com.example.phototutor.ui.localalbum;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;

import java.util.Formatter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UnitLocalPhotoDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UnitLocalPhotoDetailFragment extends Fragment {
    private Photo photo;

    private TextView textView_basic_meta;
    private TextView textView_other_meta;

    public UnitLocalPhotoDetailFragment(Photo photo){
        this.photo = photo;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imageView = view.findViewById(R.id.image_view);
        Log.d(this.getClass().getSimpleName(), "viewing a new photo page");
        Glide.with(this)
                .load(photo.imageURI)
                .placeholder(R.drawable.ic_loading)
                .into(imageView);

        textView_basic_meta = (TextView) getView().findViewById(R.id.textView_basic_metadata);
        if(textView_basic_meta == null) {
            Log.e(this.getClass().getSimpleName(),"text view not found");
        }


        StringBuilder sbuf_expo = new StringBuilder();
        Formatter fmt_expo = new Formatter(sbuf_expo);
        if(photo.shutter_speed > 0) {
            fmt_expo.format("f/%.1f 1/%ds ISO:%d", photo.aperture, (int)photo.shutter_speed, photo.iso);
        } else {
            fmt_expo.format("f/%.1f %.1fs ISO:%d", photo.aperture, Math.abs(photo.shutter_speed), photo.iso);
        }
        textView_basic_meta.setText(sbuf_expo.toString());

        textView_other_meta = (TextView) getView().findViewById(R.id.textView_other_metadata);
        StringBuilder sbuf_other = new StringBuilder();
        Formatter fmt_other = new Formatter(sbuf_other);
        fmt_other.format("%dmm", photo.focal_length);
        textView_other_meta.setText(fmt_other.toString());

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_unit_local_photo_detail, container, false);
    }
}