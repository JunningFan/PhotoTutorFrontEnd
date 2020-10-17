package com.example.phototutor.ui.localalbum;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.TextView;

import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;

import org.w3c.dom.Text;

import java.util.Formatter;
import java.util.List;
import java.util.Set;

public class LocalPhotoDetailFragment extends Fragment {
    private LocalAlbumViewModel mViewModel;
    private photoDetailPagerAdapter adapter;
    static private String TAG ="LocalPhotoDetailFragment";



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
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return new UnitLocalPhotoDetailFragment(photos.get(position));
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
        mViewModel = ViewModelProviders.of(this).get(LocalAlbumViewModel.class);
        mViewModel.loadDatabase(requireContext());
        mViewModel.getAllPhotos().observe(
                requireActivity(), photos -> {
                    adapter.setPhotos(photos);
                    viewPager.setAdapter(adapter);

                }
        );


        adapter = new photoDetailPagerAdapter(this.getChildFragmentManager());
        mViewModel.getSelected().observe(
                requireActivity(), i -> {
                    Log.w(TAG,"current item " + i);
                    viewPager.setCurrentItem(i);

                }
        );

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}