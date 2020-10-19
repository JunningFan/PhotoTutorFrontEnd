package com.example.phototutor.ui.localalbum;

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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UnitLocalPhotoDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UnitLocalPhotoDetailFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private Photo photo;

    private TextView textView_basic_meta;
    private TextView textView_other_meta;
    private TextView textView_timestamp;

    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private Marker photoMarker;

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
            fmt_expo.format("f/%.1f  1/%ds  ISO:%d", photo.aperture, (int)photo.shutter_speed, photo.iso);
        } else {
            fmt_expo.format("f/%.1f  %.1fs  ISO:%d", photo.aperture, Math.abs(photo.shutter_speed), photo.iso);
        }
        textView_basic_meta.setText(sbuf_expo.toString());

        textView_other_meta = (TextView) getView().findViewById(R.id.textView_other_metadata);
        StringBuilder sbuf_other = new StringBuilder();
        Formatter fmt_other = new Formatter(sbuf_other);
        fmt_other.format("%dmm", photo.focal_length);
        textView_other_meta.setText(fmt_other.toString());

        textView_timestamp = (TextView) getView().findViewById(R.id.textView_timestamp);
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        java.util.Date currenTimeZone=new java.util.Date((photo.timestamp));
        textView_timestamp.setText(sdf.format(currenTimeZone));

        mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_unit_local_photo_detail, container, false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.getUiSettings().setAllGesturesEnabled(false);  // disable touching to the map before the animation is rendered

        mMap.setMinZoomPreference(10.0f);
        mMap.setMaxZoomPreference(21.0f);

        LatLng photoLL = new LatLng(photo.getLatitude(), photo.getLongitude());

        float degrees = (float) photo.getOrientation();
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
    public void onMapLoaded() {
        // Add a marker in Sydney and move the camera

        mMap.getUiSettings().setAllGesturesEnabled(true);
    }

}