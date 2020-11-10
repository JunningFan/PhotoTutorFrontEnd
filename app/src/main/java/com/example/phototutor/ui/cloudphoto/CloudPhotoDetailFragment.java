package com.example.phototutor.ui.cloudphoto;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.wifi.aware.AttachCallback;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.phototutor.Photo.CloudPhoto;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;
import com.example.phototutor.helpers.UserInfoDownloader;
import com.example.phototutor.ui.home.HomeViewModel;
import com.example.phototutor.ui.localalbum.LocalPhotoDetailFragment;
import com.example.phototutor.ui.localalbum.UnitLocalPhotoDetailFragment;
import com.example.phototutor.user.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.trafi.anchorbottomsheetbehavior.AnchorBottomSheetBehavior;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import me.gujun.android.taggroup.TagGroup;
import okhttp3.ResponseBody;
import retrofit2.Call;


public class CloudPhotoDetailFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback{
    private int index = 0;
    static private String TAG ="CloudPhotoDetailFragment";
    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private boolean isMapReady = false;
    private Marker photoMarker;
    UserInfoDownloader downloader;

    private TextView textView_basic_meta;
    private TextView textView_other_meta;
    private TextView textView_timestamp;
    private TextView photo_title;
    private TextView photo_author;
    private TextView textView_location;
    private TagGroup tag_group;
    private CircleImageView avatarView;

    private CloudPhotoDetailViewModel mViewModel;
    private PhotoDetailPagerAdapter adapter;

    //    @Override
//    public void onStop() {
//        super.onStop();
//        getActivity().findViewById(R.id.appbarlayout).setVisibility(View.VISIBLE);
//
//        getActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
//        getActivity().findViewById(R.id.nav_host_fragment).setLayoutParams(new ConstraintLayout.LayoutParams(
//                ConstraintLayout.LayoutParams.MATCH_PARENT,0));
//        getActivity().findViewById(R.id.nav_host_fragment).refreshDrawableState();
//        mViewModel.getCurrIdx().removeObservers(this);
//    }
//
//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        getActivity().findViewById(R.id.appbarlayout).setVisibility(View.GONE);
//        getActivity().findViewById(R.id.nav_host_fragment).setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
//        getActivity().findViewById(R.id.nav_view).setVisibility(View.GONE);
//        getView()
//                .setSystemUiVisibility(
//                        View.SYSTEM_UI_FLAG_LOW_PROFILE |
//                                View.SYSTEM_UI_FLAG_FULLSCREEN  |
//                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
//
//                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION );
//    }

    public class PhotoDetailPagerAdapter extends FragmentStateAdapter {
        private List<CloudPhoto> photos = new ArrayList<>();
        public PhotoDetailPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        public void setPhotos(List<CloudPhoto> photos) {

            this.photos = photos;
            this.notifyDataSetChanged();
        }




        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Bundle bundle = new Bundle();

            bundle.putString("photo_url", photos.get(position).imageURI.toString());
            bundle.putString("thumbnail_url", photos.get(position).thumbnailURI.toString());
            UnitLocalPhotoDetailFragment fragment = new UnitLocalPhotoDetailFragment();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getItemCount() {

            return this.photos.size();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        index = getArguments().getInt("pos");

        mViewModel =
                ViewModelProviders.of(requireActivity()).get(CloudPhotoDetailViewModel.class);

        return inflater.inflate(R.layout.fragment_cloud_photo_detail, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        downloader = new UserInfoDownloader(requireContext());
        textView_basic_meta = (TextView) view.findViewById(R.id.textView_basic_metadata);
        textView_other_meta = (TextView) view.findViewById(R.id.textView_other_metadata);
        textView_timestamp = (TextView) view.findViewById(R.id.textView_timestamp);
        photo_title = (TextView)view.findViewById(R.id.photo_title);
        photo_author = (TextView)view.findViewById(R.id.photo_author);
        tag_group = (TagGroup)view.findViewById(R.id.tag_group);
        textView_location =(TextView)requireView().findViewById(R.id.textView_location);

        avatarView = view.findViewById(R.id.avatar);
        mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map);



        ViewPager2 photoViewPager = view.findViewById(R.id.photo_view_pager);

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(
                view.findViewById(R.id.button_sheet_image_detail));

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View view, float offset) {
                Log.w(TAG,"button Sheet onSlide "+offset);
                photoViewPager.setTranslationY(-view.getHeight()*offset*0.7f);
            }
        });
        view.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        bottomSheetBehavior.setPeekHeight(
                                view.findViewById(R.id.extra_photo_info).getTop()
                        );

                    }
                }
        );

        ((MaterialToolbar)view.findViewById(R.id.topAppBar)).setOnClickListener(

                view1 -> {
                    if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
        );

        ((MaterialToolbar)view.findViewById(R.id.topAppBar)).setNavigationOnClickListener(
                view1 -> {
                    Navigation.findNavController(CloudPhotoDetailFragment.this.requireActivity(), R.id.nav_host_fragment).navigateUp();
                }
        );
        adapter = new PhotoDetailPagerAdapter(this);
        photoViewPager.setAdapter(adapter);
        Log.w(TAG,mViewModel.getDataset().toString());
        mViewModel.getDataset().observe(requireActivity(), cloudPhotos -> {
            Log.w(TAG, String.valueOf(cloudPhotos.size()));
            adapter.setPhotos(cloudPhotos);

            photoViewPager.setCurrentItem(index,false);
            if(index != adapter.getItemCount())
                updatePhotoData(adapter.photos.get(index));

        });

        mViewModel.getCurrIdx().observe(requireActivity(), idx -> {
            index = idx;
        });
//        photoViewPager.setOnClickListener(view12 -> {
//            Log.w(TAG,"photoViewPager clicked");
//            View buttonSheetDetail = view12.findViewById(R.id.button_sheet_container);
//            if (buttonSheetDetail.getVisibility() == View.VISIBLE)
//                buttonSheetDetail.setVisibility(View.GONE);
//            else{
//                buttonSheetDetail.setVisibility(View.VISIBLE);
//            }
//
//        });
        photoViewPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback(){
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        mViewModel.select(position);
                        updatePhotoData(adapter.photos.get(position));
                    }
                }
        );

//        new MaterialAlertDialogBuilder(requireContext()).setTitle("map").setView(
//                new MapView(requireContext())
//        ).show();
    }


    private void updatePhotoData(CloudPhoto photo) {
        downloader.getUserDetail(photo.getUserId(), new UserInfoDownloader.UserDetailRequestCallback() {
            @Override
            public void onSuccessResponse(User user) {
                Log.w(TAG,"username "+user.getNickName());
                photo_author.setText(user.getNickName());
                if(CloudPhotoDetailFragment.this.getContext() != null)
                    Glide.with(CloudPhotoDetailFragment.this.getContext())
                            .load(user.getAvatarUrl())
                            .into(avatarView);
                avatarView.setOnClickListener(view -> {
                    Bundle args = new Bundle();
                    args.putInt("userId",user.getId());
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                            .navigate(R.id.action_navigation_cloud_photo_detail_to_navigation_user_profile,args);

                });
            }

            @Override
            public void onFailResponse(String message, int code) {

            }

            @Override
            public void onFailRequest(Call<ResponseBody> call, Throwable t) {

            }
        });
        photo_title.setText(photo.getTitle());
        tag_group.setTags(photo.getTags());
        Log.w(TAG,"coordinate "+photo.getLatitude() + ','+photo.getLongitude());
        textView_location.setText(""+photo.getLatitude() + ' '+ photo.getLongitude());
        if (textView_basic_meta == null) {
            Log.e(this.getClass().getSimpleName(), "text view not found");
        }


        StringBuilder sbuf_expo = new StringBuilder();
        Formatter fmt_expo = new Formatter(sbuf_expo);
        if (photo.shutter_speed > 0) {
            fmt_expo.format("f/%.1f  1/%ds  ISO:%d", photo.aperture, (int) photo.shutter_speed, photo.iso);
        } else {
            fmt_expo.format("f/%.1f  %.1fs  ISO:%d", photo.aperture, Math.abs(photo.shutter_speed), photo.iso);
        }
        textView_basic_meta.setText(sbuf_expo.toString());

        StringBuilder sbuf_other = new StringBuilder();
        Formatter fmt_other = new Formatter(sbuf_other);
        fmt_other.format("%dmm", photo.focal_length);
        textView_other_meta.setText(fmt_other.toString());
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        java.util.Date currenTimeZone = new java.util.Date((photo.timestamp));
        textView_timestamp.setText(sdf.format(currenTimeZone));
        if (photoMarker != null) {
            photoMarker.remove();
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        CloudPhoto photo = adapter.photos.get(index);
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.getUiSettings().setAllGesturesEnabled(false);  // disable touching to the map before the animation is rendered

        mMap.setMinZoomPreference(10.0f);
        mMap.setMaxZoomPreference(21.0f);

        LatLng photoLL = new LatLng(photo.getLatitude(), photo.getLongitude());

        float degrees = (float) photo.getOrientation();
        if(photoMarker != null)
            photoMarker.remove();
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

    public void viewPagerOnClick(View view){
        Log.w(TAG,"photoViewPager clicked");
        View buttonSheetDetail = view.findViewById(R.id.button_sheet_container);
        if (buttonSheetDetail.getVisibility() == View.VISIBLE)
            buttonSheetDetail.setVisibility(View.GONE);
        else{
            buttonSheetDetail.setVisibility(View.VISIBLE);
        }

    }
}