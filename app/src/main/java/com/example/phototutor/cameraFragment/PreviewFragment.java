package com.example.phototutor.cameraFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.phototutor.CameraActivity;
import com.example.phototutor.EditProfileActivity;
import com.example.phototutor.LocalAlbumActivity;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;
import com.example.phototutor.Photo.PhotoDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;


public class PreviewFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private CameraViewModel mViewModel;
    private Photo currphoto;

    BottomSheetBehavior bottomSheetBehavior;


    private TextView textView_timestamp;
    private TextView textView_weather;

    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private Marker photoMarker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("Preview Fragment","onCreate");

    }

    @Override
    public void onResume() {
        super.onResume();
        mapFragment.onResume();
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
            Toast.makeText(getActivity(), "lat: " + Double.toString(photo.getLatitude()) +
                    "lon: " + Double.toString(photo.getLongitude()) + "\nr: " + Double.toString(photo.getElevation()) +
                    "o: " + Double.toString(photo.getOrientation()) + "time: " + Long.toString(photo.timestamp) + "w: "+ photo.weather, Toast.LENGTH_LONG).show();

            imageView.post(
                    () -> imageView.setImageBitmap(photo.getBitmap())
            );

            textView_timestamp = (TextView) getView().findViewById(R.id.textView_timestamp_preview);
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            java.util.Date currenTimeZone=new java.util.Date((photo.timestamp));
            textView_timestamp.setText(sdf.format(currenTimeZone));

            textView_weather = (TextView) getView().findViewById(R.id.textView_weather_preview);
            StringBuilder weather_display_builder = new StringBuilder(currphoto.weather);
            weather_display_builder.setCharAt(0, Character.toUpperCase(currphoto.weather.charAt(0)));
            textView_weather.setText(weather_display_builder.toString());

        });

        view.findViewById(R.id.back_button).setOnClickListener(
                view1 -> Navigation.findNavController(requireActivity(), R.id.camera_nav_host_fragment).navigateUp()
        );

        view.findViewById(R.id.save_button).setOnClickListener(
                view12 -> {
                    ACProgressFlower loadingDialog = new ACProgressFlower.Builder(requireContext())
                            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                            .themeColor(Color.WHITE)
                            .text("Saving")
                            .fadeColor(Color.DKGRAY).build();
                    loadingDialog.show();
                    new Thread(
                            () -> {
                                PhotoDatabase db = Room.databaseBuilder(requireActivity(),
                                        PhotoDatabase.class, "photo_album").build();
                                currphoto.saveImage(requireActivity().getFilesDir());
                                db.photoDAO().insertPhotos(currphoto);
                                loadingDialog.cancel();
                                ((CameraActivity)getActivity()).setImageCaptured(true);
                                startActivity(new Intent(getActivity(), LocalAlbumActivity.class));
                            }
                    ).start();
                }
        );

        bottomSheetBehavior = BottomSheetBehavior.from(
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
        //setup map
        mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map_preview);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        mapFragment.onPause();
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(currphoto == null) {
            return;
        }
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.getUiSettings().setAllGesturesEnabled(false);  // disable touching to the map before the animation is rendered

        mMap.setMinZoomPreference(10.0f);
        mMap.setMaxZoomPreference(21.0f);

        LatLng photoLL = new LatLng(currphoto.getLatitude(), currphoto.getLongitude());

        float degrees = (float) currphoto.getOrientation();
        photoMarker = mMap.addMarker(new MarkerOptions()
                .title("Photo")
                .position(photoLL)
                .rotation(degrees)
                .flat(true)
                .icon(vectorToBitmap(R.drawable.ic_baseline_navigation_36, Color.parseColor("#FC771A")))
        );

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(photoLL, 18.5f));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }
        });

        mMap.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded() {
        // Add a marker in Sydney and move the camera

        mMap.getUiSettings().setAllGesturesEnabled(true);
    }

    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }




}