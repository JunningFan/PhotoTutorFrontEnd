package com.example.phototutor.ui.comment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.example.phototutor.MyAppCompatActivity;
import com.example.phototutor.R;
import com.example.phototutor.comment.Comment;
import com.example.phototutor.helpers.CommentHelper;
import com.example.phototutor.helpers.UserInfoDownloader;
import com.example.phototutor.ui.userprofile.UserProfileFragment;
import com.example.phototutor.user.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import mehdi.sakout.fancybuttons.FancyButton;
import okhttp3.ResponseBody;
import retrofit2.Call;



public class CommentFragment extends BottomSheetDialogFragment {
    private CommentHelper helper;
    private CommentListAdapter adapter;
    private UserInfoDownloader downloader;
    private EditText etComment;
    private RecyclerView rvComments;
    private SwipeRefreshLayout comment_swipe_refresh_layout;
    private MaterialToolbar reply_tool_bar;
    private Toolbar top_tool_bar;

    private int photoId;
    private class CommentListAdapter extends RecyclerView.Adapter<CommentFragment.CommentListAdapter.MyViewHolder>{
        private List<Comment> comments = new ArrayList<>();
        private Context context;
        private String TAG = "CommentListAdapter";

        public CommentListAdapter(Context context){
            this.context = context;
        }

        public void addComments(List<Comment> comments){
            int origPos = getItemCount();
            this.comments.addAll(comments);
            notifyItemRangeInserted(origPos,comments.size());
        }

        public void setComment(List<Comment> comments){
            int origPos = getItemCount();
            this.comments = comments;
            notifyDataSetChanged();
        }

        public void cleanComment(){
            int origPos = getItemCount();
            this.comments = new ArrayList<>();
            notifyItemRangeRemoved(0,origPos);
        }

        @NonNull
        @Override
        public CommentFragment.CommentListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(
                    R.layout.comment_list_view,
                    parent,
                    false);

            return new CommentFragment.CommentListAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentFragment.CommentListAdapter.MyViewHolder holder, int position) {
            Comment comment = comments.get(position);

            holder.createTime.setText(
                    DateUtils.getRelativeTimeSpanString(
                            comment.getCreatedTime().getTime()));

            holder.message_place.setText(comment.getComment());

            downloader.getUserDetail(comment.getUserId(), new UserInfoDownloader.UserDetailRequestCallback() {
                @Override
                public void onSuccessResponse(User user) {
                    Glide.with(CommentFragment.this.getContext())
                            .load(user.getAvatarUrl())
                            .into(holder.avatar);

                    holder.user_name.setText(user.getNickName());
                    holder.replyBtn.setOnClickListener(view -> editTextAddAt(user.getNickName()));

                }

                @Override
                public void onFailResponse(String message, int code) {

                }

                @Override
                public void onFailRequest(Call<ResponseBody> call, Throwable t) {

                }
            });
        }




        @Override
        public int getItemCount() {
            return comments.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public CircleImageView avatar;
            public TextView user_name;
            public TextView createTime;
            public TextView replyBtn;
            public TextView message_place;
            public MyViewHolder(View itemView) {
                super(itemView);
                user_name = itemView.findViewById(R.id.comment_user_name);
                avatar = itemView.findViewById(R.id.comment_avatar);
                createTime =  itemView.findViewById(R.id.comment_time);
                replyBtn = itemView.findViewById(R.id.reply_btn);
                message_place = itemView.findViewById(R.id.comment_message);
            }
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        photoId = getArguments().getInt("photoId");
        super.onCreate(savedInstanceState);
//        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = new CommentHelper(requireContext());
        downloader = new UserInfoDownloader((requireContext()));
        adapter = new CommentListAdapter(requireContext());
        etComment = view.findViewById(R.id.etComment);
        rvComments = view.findViewById(R.id.rvComments);

        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvComments.setAdapter(adapter);
        comment_swipe_refresh_layout =  view.findViewById(R.id.comment_swipe_refresh_layout);
        comment_swipe_refresh_layout.setOnRefreshListener(()->refreshComment());
        reply_tool_bar = view.findViewById(R.id.reply_tool_bar);
        reply_tool_bar.setNavigationOnClickListener(view1->editTextAddAt());

        reply_tool_bar.setOnMenuItemClickListener((Toolbar.OnMenuItemClickListener) menuItem -> {
            if(menuItem.getItemId() == R.id.send){
                helper.commentPhoto(photoId, etComment.getText().toString(), new CommentHelper.CommentSuccessCallback() {
                    @Override
                    public void onFailResponse(String message, int code) {
                        setErrorSnackBar(message,view1 -> {refreshComment();});
                    }

                    @Override
                    public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                        setErrorSnackBar("Network failed. Please check network",view1 -> {refreshComment();});
                    }

                    @Override
                    public void onSuccessResponse(Comment comment) {
                        Log.w("CommenFragment","success comment photo");
                        ArrayList<Comment> newComments = new ArrayList<>();
                        newComments.add(comment);
//                        CommentFragment.this.refreshComment();
                        top_tool_bar.setTitle(""+ adapter.getItemCount()+" comments");
                        adapter.addComments(newComments);
                        etComment.setText("");
                    }
                });
                return true;
            }
            return false;
        });
        top_tool_bar = view.findViewById(R.id.toolbar);
        top_tool_bar.setNavigationOnClickListener(view1->dismiss());
        refreshComment();

    }

    public void downloadComments(){
        comment_swipe_refresh_layout.setRefreshing(true);
        helper.downloadComments(photoId, adapter.getItemCount(),30, new CommentHelper.CommentDownloadSuccessCallback() {
            @Override
            public void onFailResponse(String message, int code) {
                if(code == 401) ((MyAppCompatActivity)requireActivity()).navigateToLogin();
                comment_swipe_refresh_layout.setRefreshing(false);
                setErrorSnackBar(message,view->refreshComment());
            }

            @Override
            public void onFailRequest(Call<ResponseBody> call, Throwable t) {

            }

            @Override
            public void onSuccessResponse(List<Comment> comments, int totalCommentsSize) {
                adapter.addComments(comments);

                comment_swipe_refresh_layout.setRefreshing(false);
                top_tool_bar.setTitle("" +totalCommentsSize +" comments");
            }
        });

    }

    private void setErrorSnackBar(String message,View.OnClickListener listener ){
        Snackbar.make(requireView(),
                message,
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry",listener)
                .setAnchorView(requireView().findViewById(R.id.nav_view))
                .show();
    }

    public void refreshComment(){
        adapter.cleanComment();
        downloadComments();
    }

    public void editTextAddAt(){
        etComment.setText(etComment.getText().append('@'));
    }

    public void editTextAddAt(String username){
        etComment.setText(etComment.getText().append('@' + username));
    }
//
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog  dialog = (BottomSheetDialog)super.onCreateDialog(savedInstanceState);
        BottomSheetBehavior behavior = dialog.getBehavior();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//        behavior.setBottomSheetCallback();
        return dialog;
    }
//
}