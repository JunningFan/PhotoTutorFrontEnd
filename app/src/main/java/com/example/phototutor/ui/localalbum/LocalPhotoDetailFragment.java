package com.example.phototutor.ui.localalbum;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.room.Room;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.phototutor.BuildConfig;
import com.example.phototutor.LocalAlbumActivity;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.Photo.PhotoDAO;
import com.example.phototutor.Photo.PhotoDatabase;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class LocalPhotoDetailFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback  {
    private LocalAlbumViewModel mViewModel;
    private PhotoDetailPagerAdapter adapter;
    private int index = 0;
    static private String TAG ="LocalPhotoDetailFragment";
    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private boolean isMapReady = false;
    private Marker photoMarker;
    private TextView textView_basic_meta;
    private TextView textView_other_meta;
    private TextView textView_timestamp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local_photo_detail, container, false);
    }

    public class PhotoDetailPagerAdapter extends FragmentStateAdapter {
        private List<Photo> photos = new ArrayList<>();
        public PhotoDetailPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        public void setPhotos(List<Photo> photos) {

            this.photos = photos;
            this.notifyDataSetChanged();
        }




        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Bundle bundle = new Bundle();

            bundle.putString("photo_url", adapter.photos.get(position).imageURI.getPath());

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
    public void onDestroy() {
        super.onDestroy();
        mViewModel.getAllPhotos().removeObservers(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        textView_basic_meta = (TextView) view.findViewById(R.id.textView_basic_metadata);
        textView_other_meta = (TextView) view.findViewById(R.id.textView_other_metadata);
        textView_timestamp = (TextView) view.findViewById(R.id.textView_timestamp);

        super.onViewCreated(view, savedInstanceState);
        Log.w(TAG,"onViewCreated");
        ViewPager2 viewPager = view.findViewById(R.id.photo_view_pager);
        viewPager.setOffscreenPageLimit(2);
        mViewModel = ViewModelProviders.of(requireActivity()).get(LocalAlbumViewModel.class);
        adapter = new PhotoDetailPagerAdapter(this);
        mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map);


        Log.w(TAG,mViewModel.getSelected().toString());
        viewPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback(){
                       @Override
                       public void onPageSelected(int position) {
                           super.onPageSelected(position);
                           mViewModel.select(position);
                           updatePhotoData(adapter.photos.get(position));
                       }
                }
        );
        viewPager.setAdapter(adapter);

        mViewModel.getSelected().observe(
                requireActivity(), i -> {
                    Log.w(TAG,"current item " + i);
                    index = i;
                }
        );

        mViewModel.getAllPhotos().observe(
                requireActivity(), photos -> {
                    Log.w(TAG,"loadAllData observer");
                    adapter.setPhotos(photos);
                    viewPager.setAdapter(viewPager.getAdapter());
                    Log.w(TAG, "mViewModel "+index);
                    viewPager.setCurrentItem(index,false);
                    if(index != adapter.getItemCount())
                        updatePhotoData(adapter.photos.get(index));
                }
        );





        view.findViewById(R.id.share_button).setOnClickListener(
                view1 -> {

                    Intent intent = new Intent();
                    Photo photo = adapter.photos.get(viewPager.getCurrentItem());
                    Log.w(TAG,"share_button "+photo.toString());


                    File file = new File(photo.imageURI.getPath());
                    Log.w(TAG, file.getPath());


                    Uri contentUri = FileProvider.getUriForFile(view1.getContext(),
                            BuildConfig.APPLICATION_ID + ".provider",
                            file);


                    intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("image/png");


                    // Launch the intent letting the user choose which app to share with
                    startActivity(Intent.createChooser(intent, "Share your photo to your friend"));

                }
        );

        view.findViewById(R.id.edit_button).setOnClickListener(
                view1 -> {
                    if (!hasPermissions(requireContext())){
                        requestPermissions(PERMISSIONS_REQUIRED, READ_EXTERNAL_STORAGE_REQUEST_CODE);
                    }
                    else{
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto , 0);
                    }
                }
        );

        view.findViewById(R.id.upload_button).setOnClickListener(
                view1 -> {

                    Bundle bundle = new Bundle();
                    bundle.putInt("pos", viewPager.getCurrentItem());




                    Navigation.findNavController(
                            requireActivity(),R.id.local_album_nav_host_fragment
                    ).navigate(R.id.action_local_photo_detail_fragment_to_uploadFragment,bundle);
                }
        );

        view.findViewById(R.id.delete_button).setOnClickListener(
                view1 -> {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Warning")
                            .setMessage("You are about to delete this photo")
                            .setNeutralButton("Cancel", (dialogInterface, i) -> {

                            })
                            .setPositiveButton("Ok", (dialogInterface, i) -> {
                                int pos = viewPager.getCurrentItem();
                                Photo photo = adapter.photos.get(pos);
                                if(adapter.getItemCount() <= 1){
                                    Navigation.findNavController(
                                            requireActivity(), R.id.local_album_nav_host_fragment).navigateUp();
                                }
                                else if(pos +1 >= adapter.getItemCount())
                                    mViewModel.select(pos-1);

                                mViewModel.deletePhoto(requireContext(), photo);
                                Log.w(TAG,"delete position " + pos);

                            })
                            .show();
                }

                );

    }


    private void updatePhotoData(Photo photo) {

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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK){
            Uri selectedImage = intent.getData();
            ViewPager2 viewPager = requireView().findViewById(R.id.photo_view_pager);
            Photo photo = adapter.photos.get(viewPager.getCurrentItem());
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            if (selectedImage != null) {
                Cursor cursor = requireActivity().getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    Bitmap newBitmap = BitmapFactory.decodeFile(picturePath);
                    photo.updatePhotoBitmap(requireActivity().getFilesDir(), newBitmap);
//                    db.photoDAO().getPhotoById(photo.id).observe(
//                            requireActivity(), i -> {
//                                Bitmap hi = BitmapFactory.decodeFile(i.imageURI.getPath());
//                            }
//                    );
                    mViewModel.updateDataset(requireContext(),photo);

                    cursor.close();
                }
            }

        }



    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    static private String[] PERMISSIONS_REQUIRED = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE};
    static private final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 0x1045;

    static public boolean hasPermissions(Context context){
        for(String permission: PERMISSIONS_REQUIRED){
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PERMISSION_GRANTED)
                return false;
        }
        return true;

    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Photo photo = adapter.photos.get(index);
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