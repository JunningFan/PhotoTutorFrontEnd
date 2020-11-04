package com.example.phototutor.ui.notifications;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.bumptech.glide.Glide;
import com.example.phototutor.Photo.CloudPhoto;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;
import com.example.phototutor.adapters.CloudAlbumAdapter;
import com.example.phototutor.helpers.PhotoDownloader;
import com.example.phototutor.ui.localalbum.UnitLocalPhotoDetailFragment;
import com.fivehundredpx.greedolayout.GreedoLayoutManager;
import com.fivehundredpx.greedolayout.GreedoSpacingItemDecoration;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private CloudAlbumAdapter cloudAlbumAdapter;
    private RecyclerView cloud_photo_gallery;
    private RecyclerView followerRecycleView;
    private RecyclerView followingRecycleView;
    private TabLayout userFollowInfos;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.w("NotificationsFragment", "notification");
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Glide.with(requireContext())
                .load("https://picsum.photos/200/300")
                .placeholder(R.drawable.ic_camera)
                .into((ImageView)view.findViewById(R.id.avatar));
        cloudAlbumAdapter = new CloudAlbumAdapter(requireContext());
        userFollowInfos = view.findViewById(R.id.user_follows);

        for (int i = 0; i < userFollowInfos.getTabCount();i++){
            TabLayout.Tab tab = userFollowInfos.getTabAt(i);
            View v = LayoutInflater.from(requireContext()).inflate(
                    R.layout.customer_tab_layout, null);
            TextView tabShowTitle = v.findViewById(R.id.tab_show_title);
            TextView tabShowCount = v.findViewById(R.id.tab_show_count);
            tabShowTitle.setText(tab.getText());
            tabShowTitle.setTextColor(userFollowInfos.getTabTextColors());
            tabShowCount.setTextColor(userFollowInfos.getTabTextColors());
            tab.setCustomView(v);

        }

        userFollowInfos.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0: cloud_photo_gallery.setVisibility(View.VISIBLE);break;
                    case 1: followerRecycleView.setVisibility(View.VISIBLE);break;
                    case 2: followingRecycleView.setVisibility(View.VISIBLE);break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0: cloud_photo_gallery.setVisibility(View.INVISIBLE);break;
                    case 1: followerRecycleView.setVisibility(View.INVISIBLE);break;
                    case 2: followingRecycleView.setVisibility(View.INVISIBLE);break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        cloud_photo_gallery = view.findViewById(R.id.cloud_photo_gallery);
        followerRecycleView = view.findViewById(R.id.followers);
        followingRecycleView = view.findViewById(R.id.following);

        cloudAlbumAdapter = new CloudAlbumAdapter(requireContext());
        cloudAlbumAdapter.setNeedRatio(false);
        cloudAlbumAdapter.setPhotos(new ArrayList<>());
        GreedoLayoutManager layoutManager = new GreedoLayoutManager(cloudAlbumAdapter);

        cloud_photo_gallery.setLayoutManager(layoutManager);
        int spacing =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                requireContext().getResources().getDisplayMetrics());
        cloud_photo_gallery.addItemDecoration(new GreedoSpacingItemDecoration(spacing));

        cloud_photo_gallery.setAdapter(cloudAlbumAdapter);

        notificationsViewModel.getPhotoList().observe(
                requireActivity(), cloudPhotos -> {
                    cloudAlbumAdapter.addPhotos(cloudPhotos);

                }
        );
    }

    private void downloadPhotos(){

        PhotoDownloader downloader = new PhotoDownloader(requireContext());
        downloader.downloadPhotosByGeo(0,0,cloudAlbumAdapter.getItemCount(),30, new PhotoDownloader.OnPhotoDownloadedByGeo(){
            @Override
            public void onFailResponse(String message, int code) {
                Toast.makeText(requireContext(),
                        "Network Failed. Please check the network",Toast.LENGTH_LONG);
                Snackbar.make(requireView(),
                        message,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry", view -> {
                            downloadPhotos();
                        })

                        .show();
            }

            @Override
            public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(requireContext(),
                        "Network Failed. Please check the network",Toast.LENGTH_LONG);
                Snackbar.make(requireView(),
                        "Network Failed. Please check the network",
                        Snackbar.LENGTH_INDEFINITE)

                        .setAction("Retry", view -> {
                            downloadPhotos();
                        })
//                        .setAnchorView(requireView().findViewById(R.id.nav_view))
                        .show();
            }

            @Override
            public void onSuccessResponse(PhotoDownloader.PhotoDownloadResult result) {
                List<CloudPhoto> cloudPhotos = result.getImageArray();

                notificationsViewModel.addPhotosToList(cloudPhotos);
                Log.w(this.getClass().getName(), "" + cloudAlbumAdapter.getItemCount());
                cloudAlbumAdapter.setImageGridOnClickCallBack(pos -> {
//                    Bundle args = new Bundle();
//                    args.putInt("pos",pos);
//                    Navigation.findNavController(requireActivity(),R.id.nav_host_fragment)
//                            .navigate(R.id.action_navigation_home_to_navigation_cloud_photo_detail,args);
                });
                TextView tab_show_count = userFollowInfos.getTabAt(0).getCustomView()
                        .findViewById(R.id.tab_show_count);

                tab_show_count.setText(String.valueOf(result.getTotalSize()));

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        downloadPhotos();
    }
}