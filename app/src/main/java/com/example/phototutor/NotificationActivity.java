package com.example.phototutor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.phototutor.adapters.TimeLineAdapter;
import com.example.phototutor.helpers.UserNotification;
import com.example.phototutor.notification.Notification;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class NotificationActivity extends MyAppCompatActivity {
    UserNotification helper;
    private RecyclerView timelineRv;
    private TimeLineAdapter adapter;
    private SwipeRefreshLayout notification_swipe_fresh_layout;
    private MaterialToolbar tool_bar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        helper  = new UserNotification(this);
        timelineRv = findViewById(R.id.timeline_rv);
        timelineRv.setLayoutManager(new LinearLayoutManager(this));
        notification_swipe_fresh_layout = findViewById(R.id.notification_swipe_fresh_layout);
        notification_swipe_fresh_layout.setOnRefreshListener(() -> {
            refreshNotifications();
        });
        refreshNotifications();
        tool_bar = findViewById(R.id.tool_bar);
        tool_bar.setNavigationOnClickListener(view->{onBackPressed();});


//        Call<List<Notification>> notificationList = UserNotification.getNotificationList();
//
//        notificationList.enqueue(new Callback<List<Notification>>() {
//            @Override
//            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
//                if(response.isSuccessful()) {
//                    Log.e("success", response.body().toString());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Notification>> call, Throwable t) {
//                Log.e("failure", t.getLocalizedMessage());
//            }
//        });



    }
    private void setErrorSnackBar(String message, View.OnClickListener listener ){
        Snackbar.make(findViewById(R.id.container),
                message,
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry",listener)
                .show();
    }
    public void refreshNotifications(){
        notification_swipe_fresh_layout.setRefreshing(true);
        helper.getNotificationList(new UserNotification.NotificationOnDownloadSuccessCallback() {
            @Override
            public void onFailResponse(String message, int code) {
                if (code == 401) navigateToLogin();
                setErrorSnackBar(message,view->{refreshNotifications();});
            }

            @Override
            public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                setErrorSnackBar("Network Failed. Please check the network.",view->{refreshNotifications();});
            }

            @Override
            public void onSuccessResponse(List<Notification> notifications) {
                adapter = new TimeLineAdapter(NotificationActivity.this,notifications);
                Log.w("NotificationActivity",""+adapter.getItemCount());
                timelineRv.setAdapter(adapter);
                tool_bar.setTitle(""+notifications.size() +" Notifications");
                notification_swipe_fresh_layout.setRefreshing(false);
            }
        });

    }

}