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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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


public class PreviewFragment extends Fragment implements OnMapReadyCallback {
    private CameraViewModel mViewModel;
    private Photo currphoto;

    private TextView textView_basic_meta;
    private TextView textView_other_meta;

    private GoogleMap mMap;
    private Marker photoMarker;

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
            Toast.makeText(getActivity(), "lat: " + Double.toString(photo.getLatitude()) +
                    "lon: " + Double.toString(photo.getLongitude()) + "\nr: " + Double.toString(photo.getElevation()) +
                    "o: " + Double.toString(photo.getOrientation()) + "time: " + Long.toString(photo.timestamp), Toast.LENGTH_LONG).show();
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            java.util.Date currenTimeZone=new java.util.Date((photo.timestamp));
            Toast.makeText(getActivity(), sdf.format(currenTimeZone), Toast.LENGTH_SHORT).show();
            imageView.post(
                    () -> imageView.setImageBitmap(photo.getBitmap())
            );

            textView_basic_meta = (TextView) getView().findViewById(R.id.textView_basic_metadata_preview);
            if(textView_basic_meta == null) {
                Log.e(this.getClass().getSimpleName(),"text view not found");
            }


            StringBuilder sbuf_expo = new StringBuilder();
            Formatter fmt_expo = new Formatter(sbuf_expo);
            if(currphoto.shutter_speed > 0) {
                fmt_expo.format("f/%.1f 1/%ds ISO:%d", currphoto.aperture, (int)currphoto.shutter_speed, currphoto.iso);
            } else {
                fmt_expo.format("f/%.1f %.1fs ISO:%d", currphoto.aperture, Math.abs(currphoto.shutter_speed), currphoto.iso);
            }
            textView_basic_meta.setText(sbuf_expo.toString());

            textView_other_meta = (TextView) getView().findViewById(R.id.textView_other_metadata_preview);
            StringBuilder sbuf_other = new StringBuilder();
            Formatter fmt_other = new Formatter(sbuf_other);
            fmt_other.format("%dmm", currphoto.focal_length);
            textView_other_meta.setText(fmt_other.toString());

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

        //setup map
        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map_preview);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.setMinZoomPreference(10.0f);
        mMap.setMaxZoomPreference(21.0f);

        // Add a marker in Sydney and move the camera
        LatLng photoLL = new LatLng(currphoto.getLatitude(), currphoto.getLongitude());

        float degrees = (float) currphoto.getOrientation();
        photoMarker = mMap.addMarker(new MarkerOptions()
                .title("Photo")
                .position(photoLL)
                .rotation(degrees)
                .flat(true)
                .icon(vectorToBitmap(R.drawable.ic_baseline_navigation_36, Color.parseColor("#FC771A")))
        );

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(photoLL, 18.5f));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }
        });
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