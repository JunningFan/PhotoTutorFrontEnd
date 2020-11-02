package com.example.phototutor.ui.home;

import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.example.phototutor.Photo.CloudPhoto;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;
import com.example.phototutor.adapters.AlbumAdapter;
import com.example.phototutor.adapters.CloudAlbumAdapter;
import com.example.phototutor.helpers.PhotoDownloader;
import com.fivehundredpx.greedolayout.GreedoLayoutManager;
import com.fivehundredpx.greedolayout.GreedoSpacingItemDecoration;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private CloudAlbumAdapter adapter;
    private RecyclerView cloud_photo_gallery;
    private SwipeRefreshLayout swipeRefreshLayout;
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
        cloud_photo_gallery = view.findViewById(R.id.cloud_photo_gallery);
        adapter = new CloudAlbumAdapter(requireContext());

        GreedoLayoutManager layoutManager = new GreedoLayoutManager(adapter);

        cloud_photo_gallery.setLayoutManager(layoutManager);
        int spacing =  (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                requireContext().getResources().getDisplayMetrics());
        cloud_photo_gallery.addItemDecoration(new GreedoSpacingItemDecoration(spacing));


        swipeRefreshLayout = view.findViewById(R.id.swap_fresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> downloadPhotos());

        swipeRefreshLayout.setRefreshing(true);

    }


    private void downloadPhotos(){

        PhotoDownloader downloader = new PhotoDownloader(requireContext());
        downloader.downloadAllPhotos(new PhotoDownloader.OnPhotoDownloaded(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                List<Photo> photoList = new ArrayList<>();
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    JSONArray array = data.getJSONArray("data");
                    for(int i =0; i < array.length()/2;i++){
                        JSONObject object = array.getJSONObject(i);
                        CloudPhoto photo = CloudPhoto.createCloudPhotoFromJSON(object);
                        photoList.add(photo);
                    }

                    adapter.setPhotos(photoList);
                    Log.w(this.getClass().getName(), "" + adapter.getItemCount());
                    adapter.setImageGridOnClickCallBack(new AlbumAdapter.ImageGridOnClickCallBack(){

                        @Override
                        public void run(int pos) {
                            return;
                        }
                    });

                    cloud_photo_gallery.setAdapter(adapter);
                    swipeRefreshLayout.setRefreshing(false);

                } catch (JSONException | IOException | URISyntaxException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        } );
    }

    @Override
    public void onResume() {
        super.onResume();
        downloadPhotos();
    }
}