package com.example.phototutor.ui.cloudphoto;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.example.phototutor.MyAppCompatActivity;
import com.example.phototutor.NavigationActivity;
import com.example.phototutor.Photo.CloudPhoto;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;
import com.example.phototutor.helpers.PhotoDownloader;
import com.example.phototutor.helpers.PhotoLikeHelper;
import com.example.phototutor.helpers.UserInfoDownloader;
import com.example.phototutor.ui.comment.CommentFragment;
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
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
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
    PhotoLikeHelper likeHelper;
    PhotoDownloader photoDownloader;

    private TextView textView_basic_meta;
    private TextView textView_other_meta;
    private TextView textView_timestamp;
    private TextView photo_title;
    private TextView photo_author;
    private TextView textView_location;
    private TagGroup tag_group;
    private CircleImageView avatarView;
    ToggleButton dislike_button;
    ToggleButton like_button;
    private TextView nlikeTv;
    private TextView nDislikeTv;
    private ImageView weather_label;
    private CloudPhotoDetailViewModel mViewModel;
    private PhotoDetailPagerAdapter adapter;
    private int primaryUserId;

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



    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        downloader = new UserInfoDownloader(requireContext());
        photoDownloader = new PhotoDownloader(requireContext());
        likeHelper = new PhotoLikeHelper(requireContext());
        dislike_button = view.findViewById(R.id.dislike_button);
        like_button = view.findViewById(R.id.like_button);
        textView_basic_meta = (TextView) view.findViewById(R.id.textView_basic_metadata);
        textView_other_meta = (TextView) view.findViewById(R.id.textView_other_metadata);
        textView_timestamp = (TextView) view.findViewById(R.id.textView_timestamp);
        photo_title = (TextView)view.findViewById(R.id.photo_title);
        photo_author = (TextView)view.findViewById(R.id.photo_author);
        tag_group = (TagGroup)view.findViewById(R.id.tag_group);
        textView_location =(TextView)requireView().findViewById(R.id.textView_location);
        avatarView = view.findViewById(R.id.avatar);
        primaryUserId = ((MyAppCompatActivity)requireActivity()).getPrimaryUserId();

        nlikeTv = view.findViewById((R.id.nlikeTv));
        nDislikeTv = view.findViewById(R.id.ndislikeTV);
        mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map);

        weather_label = view.findViewById(R.id.weather_label);

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

        ((ImageButton)view.findViewById(R.id.button_comment)).setOnClickListener(
                view1 -> {
                    CommentFragment dialog = new CommentFragment();
                    Bundle args = new Bundle();
                    args.putInt("photoId", adapter.photos.get(photoViewPager.getCurrentItem()).id);
                    dialog.setArguments(args);
                    dialog.show(requireActivity().getSupportFragmentManager(),"comment button sheet");

                }
        );

        like_button.setEnabled(false);
        dislike_button.setEnabled(false);
        like_button.setOnClickListener(view1 -> {
            Log.w(TAG,"in dislike "+like_button.isChecked());
            CloudPhoto photo = adapter.photos.get(index);
            if(!like_button.isChecked()){
                likeHelper.removeLikePhoto(photo.id, new PhotoLikeHelper.LikeRequestSuccessCallback() {
                    @Override
                    public void onFailResponse(String message, int code) {
                        like_button.setChecked(true);
                    }

                    @Override
                    public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                        like_button.setChecked(true);
                    }

                    @Override
                    public void onSuccessResponse(String message) {
                        like_button.setChecked(false);
                        updateVoteInfo(photo.id);
                    }
                });
            }
            else {
                likeHelper.likePhoto(photo.id, new PhotoLikeHelper.LikeRequestSuccessCallback() {
                    @Override
                    public void onFailResponse(String message, int code) {
                        like_button.setChecked(false);
                        dislike_button.setChecked(false);
                    }

                    @Override
                    public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                        like_button.setChecked(false);
                        dislike_button.setChecked(false);
                    }

                    @Override
                    public void onSuccessResponse(String message) {
                        like_button.setChecked(true);
                        dislike_button.setChecked(false);
                        updateVoteInfo(photo.id);
                    }
                });
            }

        });

        dislike_button.setOnClickListener(view1->{
            CloudPhoto photo = adapter.photos.get(index);
            if(!dislike_button.isChecked()){
                likeHelper.removeDislikePhoto(photo.id, new PhotoLikeHelper.LikeRequestSuccessCallback() {
                    @Override
                    public void onFailResponse(String message, int code) {
                        dislike_button.setChecked(true);
                    }

                    @Override
                    public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                        dislike_button.setChecked(true);
                    }

                    @Override
                    public void onSuccessResponse(String message) {
                        dislike_button.setChecked(false);
                        updateVoteInfo(photo.id);
                    }
                });
            }else{
                likeHelper.dislikePhoto(photo.id, new PhotoLikeHelper.LikeRequestSuccessCallback() {
                    @Override
                    public void onFailResponse(String message, int code) {
                        dislike_button.setChecked(false);
                    }

                    @Override
                    public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                        dislike_button.setChecked(false);
                    }

                    @Override
                    public void onSuccessResponse(String message) {
                        dislike_button.setChecked(true);
                        like_button.setChecked(false);
                        updateVoteInfo(photo.id);
                    }
                });
            }

        });
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
        photoDownloader.getPhotoInfoById(photo.id, new PhotoDownloader.OnDownloadPhotoById() {
            @Override
            public void onFailResponse(String message, int code) {
                dislike_button.setEnabled(false);
                like_button.setEnabled(false);
            }

            @Override
            public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                dislike_button.setEnabled(false);
                like_button.setEnabled(false);

            }

            @Override
            public void onSuccessResponse(CloudPhoto photo) {
                dislike_button.setEnabled(true);
                like_button.setEnabled(true);
                updateVoteInfo(photo.id);
                loadWeatherImage(photo.getWeather(),weather_label);
            }
        });
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

        mMap.setOnMapClickListener(latLng -> {
            Log.w("map click","here");
            (new MaterialAlertDialogBuilder(requireContext())).setMessage("Do you want to navigate photo?")
                    .setNegativeButton("No",null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(requireContext(), NavigationActivity.class);
                            Bundle args = new Bundle();
                            args.putString("photoPath", photo.thumbnailURI.toString());
                            args.putDouble("elevation",photo.elevation);
                            args.putDouble("orientation",photo.orientation);
                            args.putDouble("latitude",photo.getLatitude());
                            args.putDouble("longitude",photo.getLongitude());
                            intent.putExtras(args);
                            startActivity(intent);

                        }
                    }).show();

        });
        mMap.setOnMapLoadedCallback(this);


    }


    @Override
    public void onMapLoaded() {
        // Add a marker in Sydney and move the camera

//        mMap.getUiSettings().setAllGesturesEnabled(true);
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

    public void updateVoteInfo(int photoId){
        photoDownloader.getPhotoInfoById(photoId, new PhotoDownloader.OnDownloadPhotoById() {
            @Override
            public void onFailResponse(String message, int code) { }

            @Override
            public void onFailRequest(Call<ResponseBody> call, Throwable t) { }

            @Override
            public void onSuccessResponse(CloudPhoto photo) {
                nlikeTv.setText(""+photo.getnLike());
                nDislikeTv.setText(""+photo.getnDislike());
                like_button.setChecked(false);
                dislike_button.setChecked(false);
                switch (photo.checkLiked(primaryUserId)) {
                    case CloudPhoto.DISLIKE:
                        dislike_button.setChecked(true);
                        break;
                    case CloudPhoto.LIKE:
                        like_button.setChecked(true);
                        break;
                    case CloudPhoto.NEUTRAL:
                        dislike_button.setChecked(false);
                        like_button.setChecked(false);
                        break;
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mViewModel.getDataset().removeObservers(this);
        mViewModel.getCurrIdx().removeObservers(this);
    }

    @Override
    public void onResume() {
        updateVoteInfo(primaryUserId);
        super.onResume();
    }

    private void loadWeatherImage(String weather, ImageView view){
        if(isAdded()){
            String path = "";
            Drawable myIcon;
            switch (weather){
                case Photo.CLEAR:  myIcon = getResources().getDrawable(R.drawable.sunny); break;
                case Photo.MISTY: myIcon = getResources().getDrawable(R.drawable.fog);break;
                case Photo.MOSTLY_CLOUDY:myIcon = getResources().getDrawable(R.drawable.most_cloudy);break;
                case Photo.OVERCAST:myIcon = getResources().getDrawable(R.drawable.overcast);break;
                case Photo.PARTLY_CLOUDY:myIcon = getResources().getDrawable(R.drawable.cloudy);break;
                case Photo.RAIN:myIcon = getResources().getDrawable(R.drawable.rain);break;
                case Photo.SNOW:myIcon = getResources().getDrawable(R.drawable.snowflake);break;
                default:myIcon = getResources().getDrawable(R.drawable.unknown);break;
            }
            view.setImageDrawable(myIcon);
        }
    }
}