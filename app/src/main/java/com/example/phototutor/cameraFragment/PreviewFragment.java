package com.example.phototutor.cameraFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;

import java.util.zip.Inflater;


public class PreviewFragment extends Fragment {
    private CameraViewModel mViewModel;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("Preview Fragment","onCreate");

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_preview, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(CameraViewModel.class);
        Log.w("Preview Fragment","onViewCreated");
        mViewModel.getSelected().observe(getViewLifecycleOwner(), photo -> {

            ImageView imageView = view.findViewById(R.id.image_view);
            Log.w("Preview Fragment",
                    String.valueOf(photo.getPhoto().getWidth())+' ' + String.valueOf(photo.getPhoto().getHeight()));
            imageView.post(
                    () -> imageView.setImageBitmap(photo.getPhoto())
            );
        });




    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}