package com.example.phototutor.ui.localalbum;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.phototutor.BuildConfig;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Formatter;
import java.util.List;
import java.util.Set;

public class LocalPhotoDetailFragment extends Fragment {
    private LocalAlbumViewModel mViewModel;
    private photoDetailPagerAdapter adapter;
    static private String TAG ="LocalPhotoDetailFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local_photo_detail, container, false);
    }

    public static class photoDetailPagerAdapter extends FragmentStatePagerAdapter {
        private List<Photo> photos;
        public void setPhotos(List<Photo> photos) {
            this.photos = photos;
        }
        public photoDetailPagerAdapter(@NonNull FragmentManager fm) {
            super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }



        @NonNull
        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putInt("pos", position);
            UnitLocalPhotoDetailFragment fragment = new UnitLocalPhotoDetailFragment();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return this.photos.size();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.w(TAG,"onViewCreated");
        ViewPager viewPager = view.findViewById(R.id.photo_view_pager);
        viewPager.setOffscreenPageLimit(2);
        mViewModel = ViewModelProviders.of(requireActivity()).get(LocalAlbumViewModel.class);
        mViewModel.loadDatabase(requireContext());
        adapter = new photoDetailPagerAdapter(this.getChildFragmentManager());
        Log.w(TAG,mViewModel.getSelected().toString());




        mViewModel.getAllPhotos().observe(
                requireActivity(), photos -> {
                    adapter.setPhotos(photos);
                    viewPager.setAdapter(adapter);
                    mViewModel.getSelected().observe(
                            requireActivity(), i -> {
                                Log.w(TAG,"current item " + i);
                                viewPager.setCurrentItem(i);

                            }
                    );


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




    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}