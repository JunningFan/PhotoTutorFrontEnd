package com.example.phototutor.ui.userprofile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyboardShortcutGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.target.Target;
import com.example.phototutor.EditProfileActivity;
import com.example.phototutor.MyAppCompatActivity;
import com.example.phototutor.Photo.CloudPhoto;
import com.example.phototutor.R;
import com.example.phototutor.adapters.AlbumAdapter;
import com.example.phototutor.adapters.CloudAlbumAdapter;
import com.example.phototutor.helpers.PhotoDownloader;
import com.example.phototutor.helpers.UserFollowHelper;
import com.example.phototutor.helpers.UserInfoDownloader;
import com.example.phototutor.ui.cloudphoto.CloudPhotoDetailViewModel;
import com.example.phototutor.user.User;
import com.fivehundredpx.greedolayout.GreedoLayoutManager;
import com.fivehundredpx.greedolayout.GreedoSpacingItemDecoration;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import mehdi.sakout.fancybuttons.FancyButton;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Url;

public class UserProfileFragment extends Fragment {
    private CloudPhotoDetailViewModel cloudPhotoDetailViewModel;
    private CloudAlbumAdapter cloudAlbumAdapter;
    private UserListAdapter followerAdapter;
    private UserListAdapter followingAdapter;

    private RecyclerView cloud_photo_gallery;
    private RecyclerView followerRecycleView;
    private RecyclerView followingRecycleView;
    private TabLayout userFollowInfos;
    private int userId;
    private boolean isPrimaryUser;
    private UserInfoDownloader downloader;

    private SwipeRefreshLayout photo_swipe_fresh_layout;
    private SwipeRefreshLayout followers_swipe_fresh_layout;
    private SwipeRefreshLayout following_swipe_fresh_layout;

    private TextView user_signature;
    private TextView user_name;
    private CircleImageView user_avatar;
    Button user_action_btn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();
        isPrimaryUser =  args.getBoolean("primaryUser",false);
        userId  =  args.getInt("userId",0);
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }
    private class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MyViewHolder>{
        private List<User> users = new ArrayList<>();
        private Context context;
        private String TAG = "UserListAdapter";
        public UserListAdapter(Context context){
            this.context = context;
        }

        public void addUsers(List<User> users){
            Log.w(TAG,"add users "+users.size());
            int origPos = getItemCount();
            this.users.addAll(users);
            notifyItemRangeInserted(origPos,users.size());
        }

        public void setUsers(List<User> users){
            int origPos = getItemCount();
            this.users = users;
            notifyItemRangeRemoved(0,origPos);
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(
                    R.layout.user_list_layout,
                    parent,
                    false);

            return new UserListAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.user_name.setText(users.get(position).getNickName());
            holder.avatar.setOnClickListener(view -> {
                Bundle args = new Bundle();
                args.putInt("userId",users.get(position).getId());
                Navigation.findNavController(UserProfileFragment.this.requireActivity(),R.id.nav_host_fragment)
                        .navigate(R.id.action_navigation_user_profile_self,args);
            });
            Glide.with(context)
                    .load(users.get(position).getAvatarUrl())
                    .placeholder(R.drawable.ic_loading)
                    .format(DecodeFormat.PREFER_RGB_565)
                    .into(holder.avatar);
            holder.nfollowers.setText("" + users.get(position).getnFollowers() +" followers");
            downloader.getAmiFollowing(users.get(position).getId(), new UserInfoDownloader.UserIsFollowingRequestCallback() {
                @Override
                public void onSuccessResponse(boolean isFollowing) {
                    if(isFollowing){
                        holder.user_list_follow_btn.setText("unfollow");
                        holder.user_list_follow_btn.setOnClickListener(view->UserProfileFragment.this.onUnFollowBtnClicked(users.get(position).getId()));

                    }
                    else{
                        holder.user_list_follow_btn.setText("follow");
                        holder.user_list_follow_btn.setOnClickListener(view->UserProfileFragment.this.onFollowBtnClicked(users.get(position).getId()));
                    }
                }

                @Override
                public void onFailResponse(String message, int code) {
                    if(code == 401) ((MyAppCompatActivity)requireActivity()).navigateToLogin();
                }

                @Override
                public void onFailRequest(Call<ResponseBody> call, Throwable t) {

                }
            });
        }


        @Override
        public int getItemCount() {
            return users.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public CircleImageView avatar;
            public TextView user_name;
            public TextView nfollowers;
            FancyButton user_list_follow_btn;
            public MyViewHolder(View itemView) {
                super(itemView);
                user_name = itemView.findViewById(R.id.user_list_user_name);
                avatar = itemView.findViewById(R.id.user_list_avatar);
                nfollowers =  itemView.findViewById(R.id.user_list_nfollowers);
                user_list_follow_btn = itemView.findViewById(R.id.user_list_follow_btn);
            }
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        cloudPhotoDetailViewModel = ViewModelProviders.of(requireActivity()).get(CloudPhotoDetailViewModel.class);
        user_name = view.findViewById(R.id.user_name);
        user_signature  = view.findViewById(R.id.user_signature);
        user_avatar = view.findViewById(R.id.avatar);
        user_action_btn = requireView().findViewById(R.id.user_action_btn);


        downloader = new UserInfoDownloader(requireContext());
        Glide.with(requireContext())
                .load("https://picsum.photos/seed/picsum/200/300")
                .placeholder(R.drawable.ic_camera)
                .into((ImageView)view.findViewById(R.id.image_wall));

        cloudAlbumAdapter = new CloudAlbumAdapter(requireContext());
        followerAdapter = new UserListAdapter(requireContext());
        followingAdapter = new UserListAdapter(requireContext());

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
                    case 0: photo_swipe_fresh_layout.setVisibility(View.VISIBLE);break;
                    case 1: followers_swipe_fresh_layout.setVisibility(View.VISIBLE);break;
                    case 2: following_swipe_fresh_layout.setVisibility(View.VISIBLE);break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0: photo_swipe_fresh_layout.setVisibility(View.INVISIBLE);break;
                    case 1: followers_swipe_fresh_layout.setVisibility(View.INVISIBLE);break;
                    case 2: following_swipe_fresh_layout.setVisibility(View.INVISIBLE);break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        cloud_photo_gallery = view.findViewById(R.id.cloud_photo_gallery);
        followerRecycleView = view.findViewById(R.id.followers);
        followingRecycleView = view.findViewById(R.id.following);

        photo_swipe_fresh_layout = view.findViewById(R.id.photo_swipe_fresh_layout);
        photo_swipe_fresh_layout.setOnRefreshListener(()->refreshUserPhoto());
        followers_swipe_fresh_layout = view.findViewById(R.id.followers_swipe_fresh_layout);
        followers_swipe_fresh_layout.setOnRefreshListener(()->refreshUserFollowers());
        following_swipe_fresh_layout = view.findViewById(R.id.following_swipe_fresh_layout);
        following_swipe_fresh_layout.setOnRefreshListener(()->refreshUserFollowings());

        followerRecycleView.setLayoutManager(new LinearLayoutManager(requireContext()));
        followingRecycleView.setLayoutManager(new LinearLayoutManager(requireContext()));

        followerRecycleView.setAdapter(followerAdapter);
        followingRecycleView.setAdapter(followingAdapter);

        cloudAlbumAdapter = new CloudAlbumAdapter(requireContext());
        cloudAlbumAdapter.setNeedRatio(false);
        cloudAlbumAdapter.setPhotos(new ArrayList<>());
        GreedoLayoutManager layoutManager = new GreedoLayoutManager(cloudAlbumAdapter);

        cloud_photo_gallery.setLayoutManager(layoutManager);
        int spacing =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                requireContext().getResources().getDisplayMetrics());
        cloud_photo_gallery.addItemDecoration(new GreedoSpacingItemDecoration(spacing));
        cloud_photo_gallery.setAdapter(cloudAlbumAdapter);


        AppBarLayout mAppLayout = view.findViewById(R.id.app_bar_layout);
        mAppLayout.addOnOffsetChangedListener(
                new AppBarLayout.OnOffsetChangedListener() {
                    private float initialOffset = 0;

                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        Log.w("AppBarCollapse","offset "+verticalOffset + " total range " + appBarLayout.getTotalScrollRange());
                        view.findViewById(R.id.avatar).setTranslationY(verticalOffset * 0.7f);
                        view.findViewById(R.id.avatar).setAlpha(1.0f+(float)verticalOffset/(float) appBarLayout.getTotalScrollRange());
                    }
                }

        );


    }

    private UserInfoDownloader.UserDetailRequestCallback callback = new UserInfoDownloader.UserDetailRequestCallback() {
        @Override
        public void onSuccessResponse(User user) {
            user_name.setText(user.getNickName());
            user_signature.setText( user.getSignature());
            Glide.with(requireContext())
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.ic_camera)
                    .into(user_avatar);

            TextView tab_show_count = userFollowInfos.getTabAt(1).getCustomView()
                    .findViewById(R.id.tab_show_count);

            tab_show_count.setText(String.valueOf(user.getnFollowers()));

            tab_show_count = userFollowInfos.getTabAt(2).getCustomView()
                    .findViewById(R.id.tab_show_count);

            tab_show_count.setText(String.valueOf(user.getnFollowerings()));

            if(isPrimaryUser){
                user_action_btn.setText("Edit Profile");
                user_action_btn.setOnClickListener(view1->onEditProfileClicked(
                        user.getSignature(),user.getNickName(),user.getAvatarUrl()));
            }
            else{
                downloader.getAmiFollowing(user.getId(), new UserInfoDownloader.UserIsFollowingRequestCallback() {
                    @Override
                    public void onSuccessResponse(boolean isFollowing) {
                        if(isFollowing){
                            user_action_btn.setText("unfollow");
                            user_action_btn.setOnClickListener(view1->onUnFollowBtnClicked(userId));
                        }
                        else{
                            user_action_btn.setText("follow");
                            user_action_btn.setOnClickListener(view1->onFollowBtnClicked(userId));
                        }
                    }

                    @Override
                    public void onFailResponse(String message, int code) {
                        if(code == 401) ((MyAppCompatActivity)requireActivity()).navigateToLogin();
                    }

                    @Override
                    public void onFailRequest(Call<ResponseBody> call, Throwable t) {

                    }
                });

            }
        }

        @Override
        public void onFailResponse(String message, int code) {
            if(code == 401) ((MyAppCompatActivity)requireActivity()).navigateToLogin();
        }

        @Override
        public void onFailRequest(Call<ResponseBody> call, Throwable t) {

        }
    };

    private void downloadUserInfo(){
        if(isPrimaryUser)
            downloader.getPrimaryUserDetail(callback);
        else
            downloader.getUserDetail(userId, callback);
    }

    private void downloadUserFollowings(){
        following_swipe_fresh_layout.setRefreshing(true);
        downloader.downloadUserFollowing(userId, new UserInfoDownloader.UserfollowingRequestCallback() {
            @Override
            public void onSuccessResponse(List<User> users) {
                following_swipe_fresh_layout.setRefreshing(false);
                Log.w("downloadUserFollowings",""+users.size());
                followingAdapter.addUsers(users);
            }

            @Override
            public void onFailResponse(String message, int code) {
                following_swipe_fresh_layout.setRefreshing(false);
                setErrorSnackBar(message,following_swipe_fresh_layout,view->refreshUserFollowings());
            }

            @Override
            public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                following_swipe_fresh_layout.setRefreshing(false);
                setErrorSnackBar("Network failed. Please retry",following_swipe_fresh_layout,view->refreshUserFollowings());
            }
        });
    }


    private void setErrorSnackBar(String message, SwipeRefreshLayout swipeRefreshLayout,View.OnClickListener listener ){
        Snackbar.make(requireView(),
                message,
                Snackbar.LENGTH_INDEFINITE)

                .setAction("Retry",listener)
                .setAnchorView(requireView().findViewById(R.id.nav_view))
                .show();
    }

    private void downloadUserFollowers(){
        followers_swipe_fresh_layout.setRefreshing(true);
        downloader.downloadUserFollower(userId, new UserInfoDownloader.UserfollowerRequestCallback() {
            @Override
            public void onSuccessResponse(List<User> users) {

                followers_swipe_fresh_layout.setRefreshing(false);
                followerAdapter.addUsers(users);
            }

            @Override
            public void onFailResponse(String message, int code) {
                if(code == 401) ((MyAppCompatActivity)requireActivity()).navigateToLogin();
                followers_swipe_fresh_layout.setRefreshing(false);
                setErrorSnackBar(message,following_swipe_fresh_layout,view->refreshUserFollowers());
            }

            @Override
            public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                followers_swipe_fresh_layout.setRefreshing(false);
                setErrorSnackBar("Network Failed. Please Retry.",following_swipe_fresh_layout,view->refreshUserFollowers());
            }
        });
    }
    private void downloadPhotos(){
        photo_swipe_fresh_layout.setRefreshing(true);
        PhotoDownloader downloader = new PhotoDownloader(requireContext());
        downloader.downloadPhotoByUserId(userId,0, cloudAlbumAdapter.getItemCount(), new PhotoDownloader.OnPhotoDownloadedbyUser(){
            @Override
            public void onFailResponse(String message, int code) {
                photo_swipe_fresh_layout.setRefreshing(false);
                setErrorSnackBar(message,following_swipe_fresh_layout,view->refreshUserPhoto());
            }

            @Override
            public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                photo_swipe_fresh_layout.setRefreshing(false);
                setErrorSnackBar("Network Failed. Please Retry.",following_swipe_fresh_layout,view->refreshUserPhoto());
            }

            @Override
            public void onSuccessResponse(PhotoDownloader.PhotoDownloadResult result) {
                photo_swipe_fresh_layout.setRefreshing(false);
                List<CloudPhoto> cloudPhotos = result.getImageArray();
                cloudAlbumAdapter.setPhotos(cloudPhotos);

                Log.w(this.getClass().getName(), "" + cloudAlbumAdapter.getItemCount());
                cloudAlbumAdapter.setImageGridOnClickCallBack(pos -> {
                    Bundle args = new Bundle();
                    args.putInt("pos",pos);
                    cloudPhotoDetailViewModel.setDataset(cloudAlbumAdapter.getPhotoList());
                    Navigation.findNavController(requireActivity(),R.id.nav_host_fragment)
                            .navigate(R.id.action_navigation_user_profile_to_navigation_cloud_photo_detail,args);
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
        downloadUserInfo();
        refreshUserFollowings();
        refreshUserFollowers();
        refreshUserPhoto();
    }


    public void onEditProfileClicked(String signature, String nickname, URL avatarUrl){
        Log.w("UserProfile",signature);
        Intent intent = new Intent(requireActivity(), EditProfileActivity.class);
        intent.putExtra("signature", signature );
        intent.putExtra("nickname", nickname );
        intent.putExtra("avatarUrl", avatarUrl.toString());
        startActivity(intent);
    }

    public void onFollowBtnClicked(int id){
        UserFollowHelper helper = new UserFollowHelper(requireContext());
        helper.addFollow(id, new UserFollowHelper.UserFollowActionCallback() {
            @Override
            public void onFailResponse(String message, int code) {

            }

            @Override
            public void onFailRequest(Call<ResponseBody> call, Throwable t) {

            }

            @Override
            public void onSuccessResponse() {
                Toast.makeText(requireContext(),"Followed",Toast.LENGTH_SHORT).show();
                downloadUserInfo();
                refreshUserFollowers();
                refreshUserFollowings();
            }
        });
    }

    public void onUnFollowBtnClicked(int id){
        UserFollowHelper helper = new UserFollowHelper(requireContext());
        helper.removeFollow(id, new UserFollowHelper.UserFollowActionCallback() {
            @Override
            public void onFailResponse(String message, int code) {

            }

            @Override
            public void onFailRequest(Call<ResponseBody> call, Throwable t) {

            }

            @Override
            public void onSuccessResponse() {
                Toast.makeText(requireContext(),"UnFollowed",Toast.LENGTH_SHORT).show();
                downloadUserInfo();
                refreshUserFollowers();
                refreshUserFollowings();
            }
        });
    }

    public void refreshUserPhoto(){
        cloudAlbumAdapter.setPhotos(new ArrayList<>());
        downloadPhotos();
    }

    public void refreshUserFollowers(){
        followerAdapter.setUsers(new ArrayList<>());
        downloadUserFollowers();
    }

    public void refreshUserFollowings(){
        followingAdapter.setUsers(new ArrayList<>());
        Log.w("refreshUserFollowings",""+followingAdapter.getItemCount());
        downloadUserFollowings();
    }

}