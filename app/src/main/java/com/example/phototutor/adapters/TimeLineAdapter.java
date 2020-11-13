package com.example.phototutor.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.phototutor.R;
import com.example.phototutor.helpers.UserInfoDownloader;
import com.example.phototutor.notification.Notification;
import com.example.phototutor.user.User;
import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.material.snackbar.Snackbar;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineAdapter.TimelineViewHolder> {

    private Context mContext;
    private List<Notification> mdata;

    UserInfoDownloader downloader;
    public TimeLineAdapter(Context mContext, List<Notification> mdata) {
        this.mContext = mContext;
        this.mdata = mdata;
        downloader = new UserInfoDownloader(mContext);
    }

    public class TimelineViewHolder extends RecyclerView.ViewHolder{
        private TimelineView mTimelineView;
        private TextView txtPost,txtTime;
        private ImageView imgUser;
        private TextView post_user_name;

        public TimelineViewHolder(@NonNull View itemView, int viewType){
            super(itemView);
            mTimelineView =(TimelineView) itemView.findViewById(R.id.timeline);
            mTimelineView.initLine(viewType);
            txtPost = itemView.findViewById(R.id.notification_content);
            txtTime = itemView.findViewById(R.id.post_time);
            imgUser = itemView.findViewById(R.id.userAvatar);
//            tvHeader = itemView.findViewById(R.id.header_text);
            post_user_name = itemView.findViewById(R.id.post_user_name);
        }

        void setData(Notification item) {
//            PostTextItem post = item.getPostTextItem();
            txtPost.setText(item.getMessage());
            //ZonedDateTime.parse().toInstant()
            Date dateTime = Date.from(item.getCreateTime().toInstant());
//            Date dateTime = Date.from(ZonedDateTime.parse(item.get).toInstant());
            txtTime.setText(DateUtils.getRelativeTimeSpanString(dateTime.getTime()));

            downloader.getUserDetail(item.getActorId(), new UserInfoDownloader.UserDetailRequestCallback() {
                @Override
                public void onSuccessResponse(User user) {
                    Glide.with(mContext).load(user.getAvatarUrl()).into(imgUser);
                    post_user_name.setText(user.getNickName());
                }

                @Override
                public void onFailResponse(String message, int code) {
                }

                @Override
                public void onFailRequest(Call<ResponseBody> call, Throwable t) {

                }
            });

//            Glide.with(itemView.getContext()).load().into(imgUser);
//            HeaderTextItem header = item.getHeaderTextItem();
//            tvHeader.setText(header.getHeaderText());
        }
    }


    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.notification_post,
                parent,false);
        return new TimelineViewHolder(view,viewType);

    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        holder.setData(mdata.get(position));
    }



    @Override
    public int getItemCount() {
        return mdata.size();
    }
}
