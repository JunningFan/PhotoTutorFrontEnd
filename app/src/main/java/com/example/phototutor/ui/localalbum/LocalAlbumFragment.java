package com.example.phototutor.ui.localalbum;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;
import com.example.phototutor.adapters.AlbumAdapter;

import java.util.List;

public class LocalAlbumFragment extends Fragment {

    private LocalAlbumViewModel mViewModel;
    private AlbumAdapter adapter;
    private static String TAG = "LocalAlbumFragment";
    public static LocalAlbumFragment newInstance() {
        return new LocalAlbumFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local_album, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.w(TAG,"onViewCreated");


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(LocalAlbumViewModel.class);
        mViewModel.loadDatabase(requireContext());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(),
                3,GridLayoutManager.VERTICAL,false);
        RecyclerView local_album_recycleview = getView().findViewById(R.id.local_album_recyclerview);
        adapter = new AlbumAdapter(requireContext());
        adapter.setImageGridOnClickCallBack(
                pos -> {

                    mViewModel.select(pos);
                    Log.w(TAG,"in Call Back " + pos);
                    Log.w(TAG,"in Call Back " +  requireActivity().toString());
                    Navigation.findNavController(
                            requireActivity(),R.id.local_album_nav_host_fragment
                    ).navigate(R.id.action_local_album_fragment_to_local_photo_detail_fragment);
                }
        );
        local_album_recycleview.setAdapter(adapter);
        local_album_recycleview.setLayoutManager(gridLayoutManager);
        // TODO: Use the ViewModel
        mViewModel.getAllPhotos().observe(
                requireActivity(), photos -> {
                    adapter.setPhotos(photos);
                    Log.w(TAG,String.valueOf(adapter.getItemCount()));

                }
        );

    }

}