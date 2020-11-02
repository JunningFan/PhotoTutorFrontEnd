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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;


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
import java.net.MalformedURLException;
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
    private boolean isScrolling = false;
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
        adapter.setPhotos(new ArrayList<>());
        GreedoLayoutManager layoutManager = new GreedoLayoutManager(adapter);

        cloud_photo_gallery.setLayoutManager(layoutManager);
        int spacing =  (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                requireContext().getResources().getDisplayMetrics());
        cloud_photo_gallery.addItemDecoration(new GreedoSpacingItemDecoration(spacing));

        cloud_photo_gallery.setAdapter(adapter);
        swipeRefreshLayout = view.findViewById(R.id.swap_fresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> downloadPhotos());

        swipeRefreshLayout.setRefreshing(true);
        cloud_photo_gallery.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int currentItem = layoutManager.getChildCount();
                int totalItems = layoutManager.getItemCount();
                int scrollOutItems = layoutManager.findFirstVisibleItemPosition();
                if (isScrolling && currentItem + scrollOutItems == totalItems){
                    swipeRefreshLayout.setRefreshing(true);
                    isScrolling = false;
                    downloadPhotos();

                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState ==RecyclerView.SCROLL_STATE_DRAGGING) {
                    isScrolling = true;
                }
            }
        });
    }


    private void downloadPhotos(){

        PhotoDownloader downloader = new PhotoDownloader(requireContext());
        downloader.downloadPhotosByGeo(0,0,adapter.getItemCount(),30, new PhotoDownloader.OnPhotoDownloaded(){
            @Override
            public void onFailResponse(String message, int code) {
                Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT);
            }

            @Override
            public void onFailRequest(Call<ResponseBody> call, Throwable t) {

            }

            @Override
            public void onSuccessResponse(JSONArray imageJSONs) {
                List<Photo> photoList = new ArrayList<>();
                try {
                    JSONArray array = imageJSONs;
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        CloudPhoto photo = CloudPhoto.createCloudPhotoFromJSON(object);
                        photoList.add(photo);
                    }

                    adapter.addPhotos(photoList);
                    Log.w(this.getClass().getName(), "" + adapter.getItemCount());
                    adapter.setImageGridOnClickCallBack(pos -> {
                        return;
                    });

                    swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        downloadPhotos();
    }
}