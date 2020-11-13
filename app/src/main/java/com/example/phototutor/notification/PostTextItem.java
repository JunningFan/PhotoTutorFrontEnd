package com.example.phototutor.notification;

public class PostTextItem {

    private String postText;
    private int imgUser;
    private String time;

    public PostTextItem(String postText, int imgUser, String time){
        this.postText = postText;
        this.imgUser = imgUser;
        this.time = time;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public int getImgUser() {
        return imgUser;
    }

    public void setImgUser(int imgUser) {
        this.imgUser = imgUser;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
