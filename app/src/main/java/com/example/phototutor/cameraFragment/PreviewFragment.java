package com.example.phototutor.cameraFragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.phototutor.LocalAlbumActivity;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;
import com.example.phototutor.Photo.PhotoDatabase;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;


public class PreviewFragment extends Fragment {
    private CameraViewModel mViewModel;
    private Photo currphoto;


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
            currphoto = photo;
            ImageView imageView = view.findViewById(R.id.image_view);
            Log.w("Preview Fragment",
                    String.valueOf(photo.getBitmap().getWidth())+' ' + String.valueOf(photo.getBitmap().getHeight()));
            Toast.makeText(getActivity(), "lat: " + Double.toString(photo.getLatitude()) + "lon: " + Double.toString(photo.getLongitude()), Toast.LENGTH_LONG).show();

            imageView.post(
                    () -> imageView.setImageBitmap(photo.getBitmap())
            );

        });

        view.findViewById(R.id.back_button).setOnClickListener(
                view1 -> Navigation.findNavController(requireActivity(), R.id.camera_nav_host_fragment).navigateUp()
        );

        view.findViewById(R.id.save_button).setOnClickListener(
                view12 -> {
                    new Thread(
                            () -> {
                                currphoto.saveImage(requireActivity().getFilesDir());
                                PhotoDatabase db = Room.databaseBuilder(requireActivity(),
                                        PhotoDatabase.class, "photo_album").build();
                                db.photoDAO().insertPhotos(currphoto);
                                startActivity(new Intent(getActivity(), LocalAlbumActivity.class));

                            }
                    ).start();
                }
        );

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(
                view.findViewById(R.id.button_sheet_image_detail));
//        bottomSheetBehavior.setPeekHeight(300);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                yourView.animate().y(v <= 0 ?
//                        view.getY() + mSheetBehavior.getPeekHeight() - yourView.getHeight() :
//                        view.getHeight() - yourView.getHeight()).setDuration(0).start();
            }
        });
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }




}