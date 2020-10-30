package com.example.phototutor.ui.home;

import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;
import com.example.phototutor.adapters.AlbumAdapter;
import com.example.phototutor.adapters.CloudAlbumAdapter;
import com.example.phototutor.helpers.PhotoDownloader;
import com.fivehundredpx.greedolayout.GreedoLayoutManager;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private CloudAlbumAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView cloud_photo_gallery = view.findViewById(R.id.cloud_photo_gallery);
        adapter = new CloudAlbumAdapter(requireContext());


        PhotoDownloader downloader = new PhotoDownloader(requireContext());
//        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(requireContext());

        GreedoLayoutManager layoutManager = new GreedoLayoutManager(adapter);
//        layoutManager.setFlexDirection(FlexDirection.ROW);
//        layoutManager.setFlexWrap(FlexWrap.WRAP);
//        layoutManager.setAlignItems(AlignItems.STRETCH);

//        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
//                2, StaggeredGridLayoutManager.VERTICAL);

        cloud_photo_gallery.setLayoutManager(layoutManager);

        downloader.downloadPhotos(new PhotoDownloader.OnPhotoDownloaded(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.w("HomeFragment",response.toString());
                List<Photo> photoList = new ArrayList<>();
                try {
                    JSONArray array = new JSONArray(response.body().string());
                    for(int i =0; i < array.length();i++){
                        JSONObject object = array.getJSONObject(i);
                        Photo photo = new Photo();
                        photo.imageURI = Uri.parse(object.getString("fullUrl"));
                        photo.setWidth(object.getInt("width"));
                        photo.setHeight(object.getInt("height"));
                        Log.w("HomeFragment",photo.imageURI.toString());
                        photoList.add(photo);
                        adapter.setPhotos(photoList);
                        adapter.setImageGridOnClickCallBack(new AlbumAdapter.ImageGridOnClickCallBack(){

                            @Override
                            public void run(int pos) {
                                return;
                            }
                        });
                        cloud_photo_gallery.setAdapter(adapter);
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        } );


    }
}