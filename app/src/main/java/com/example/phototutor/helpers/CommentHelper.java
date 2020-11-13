package com.example.phototutor.helpers;

import android.content.Context;

import com.example.phototutor.comment.Comment;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentHelper extends ServerClient {
    private Context context;
    public CommentHelper(Context context){this.context = context;}


    public static abstract class CommentDownloadSuccessCallback implements Callback<ResponseBody> {
        abstract public void onFailResponse(String message,int code );

        abstract public void onFailRequest(Call<ResponseBody> call, Throwable t);

        abstract public void onSuccessResponse(List<Comment> comments, int totalCommentsSize);


        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if(response.isSuccessful()){

                try {
                    ArrayList<Comment> comments = new ArrayList<>();
                    JSONObject object = new JSONObject(response.body().string());
                    JSONArray array = object.getJSONObject("hits").getJSONArray("hits");
                    int totalSize = object.getJSONObject("hits").getJSONObject("total").getInt("value");
                    for(int i =0; i< array.length();i++ ){
                        int id = array.getJSONObject(i).getJSONObject("_source").getInt("ID");
                        int userId = array.getJSONObject(i).getJSONObject("_source").getInt("UID");
                        int photoId = array.getJSONObject(i).getJSONObject("_source").getInt("PictureID");
                        String message = array.getJSONObject(i).getJSONObject("_source").getString("Message");
                        String createDate = array.getJSONObject(i).getJSONObject("_source").getString("CreatedAt");
                        Comment comment = new Comment(photoId,message,userId,createDate,id);
                        comments.add(comment);
                    }

                    comments.sort((v1,v2)->{
                       if(v1.getCreatedTime().before(v2.getCreatedTime())){
                           return 1;
                       }
                       else if (v1.getCreatedTime().after(v2.getCreatedTime())){
                           return -1;
                        }
                       else{
                           return 0;
                        }
                    });
                    onSuccessResponse(comments,totalSize);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else {
                onFailResponse(response.message(),response.code());
            }
        }
        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            onFailRequest(call, t);
        }
    }


    public static abstract class CommentDeletedCallback implements  Callback<ResponseBody>{
        abstract public void onFailResponse(String message,int code );
        abstract public void onFailRequest(Call<ResponseBody> call, Throwable t);
        abstract public void onSuccessResponse();

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if(response.isSuccessful()){
                onSuccessResponse();
            }else {
                onFailResponse(response.message(),response.code());
            }
        }
        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            onFailRequest(call, t);
        }
    }
    public static abstract class CommentSuccessCallback implements Callback<ResponseBody>{
        abstract public void onFailResponse(String message,int code );

        abstract public void onFailRequest(Call<ResponseBody> call, Throwable t);

        abstract public void onSuccessResponse(Comment comment);

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if(response.isSuccessful()){
                try {
                    JSONObject object = new JSONObject(response.body().string());
                    int id = object.getInt("ID");
                    String createTime = object.getString("UpdatedAt");
                    String message = object.getString("Message");
                    int userId = object.getInt("UID");
                    int photoId =object.getInt("PictureID");
                    Comment comment = new Comment(photoId,message,userId,createTime,id);
                    onSuccessResponse(comment);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                onFailResponse(response.message(),response.code());
            }
        }
        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            onFailRequest(call, t);
        }
    }

    public void downloadComments(int photoId, int from, int size, CommentDownloadSuccessCallback callback){
        JsonObject object = new JsonObject();
        object.addProperty("from", from);
        object.addProperty("size", size);
        JsonObject query = new JsonObject();
        JsonObject match = new JsonObject();
        match.addProperty("PictureID", photoId);
        query.add("match",match);
        object.add("query",query);
        getService().getComments(getAuthorizationToken(context),object).enqueue(callback);
    }

    public void commentPhoto(int photoId,String message, CommentSuccessCallback callback){
        JsonObject object = new JsonObject();
        object.addProperty("message",message);
        getService().commentPhoto(getAuthorizationToken(context), photoId, object).enqueue(callback);
    }

    public void deleteComment(int commentId, CommentDeletedCallback callback){
        getService().deleteComment(getAuthorizationToken(context),commentId).enqueue(callback);
    }
}
