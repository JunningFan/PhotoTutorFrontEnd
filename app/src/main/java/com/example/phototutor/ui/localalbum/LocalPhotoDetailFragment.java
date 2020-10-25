package com.example.phototutor.ui.localalbum;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.room.Room;
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

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Set;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class LocalPhotoDetailFragment extends Fragment {
    private LocalAlbumViewModel mViewModel;
    private PhotoDetailPagerAdapter adapter;
    private int index = 0;
    static private String TAG ="LocalPhotoDetailFragment";

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
        }



        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Bundle bundle = new Bundle();
            bundle.putInt("pos", position);
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
        super.onViewCreated(view, savedInstanceState);
        Log.w(TAG,"onViewCreated");
        ViewPager2 viewPager = view.findViewById(R.id.photo_view_pager);
        viewPager.setOffscreenPageLimit(2);
        mViewModel = ViewModelProviders.of(requireActivity()).get(LocalAlbumViewModel.class);
        adapter = new PhotoDetailPagerAdapter(this);
        Log.w(TAG,mViewModel.getSelected().toString());
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
                                                   @Override
                                                   public void onPageSelected(int position) {
                                                       super.onPageSelected(position);
                                                       mViewModel.select(position);
                                                       mViewModel.getSelected();
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
                    viewPager.setCurrentItem(index,false);
                }
        );





        view.findViewById(R.id.share_button).setOnClickListener(
                view1 -> {

                    Intent intent = new Intent();
                    Photo photo = adapter.photos.get(viewPager.getCurrentItem());
                    Log.w(TAG,"share_button "+photo.toString());


                    File file = new File(photo.imageURI.getPath());
                    Log.w(TAG, file.getPath());
//                    File file_out = new File(view1.getContext().getCacheDir(), filename);
//                    try {
//                        FileOutputStream file_os = new FileOutputStream(file);
//                        file_os.write();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }


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


}