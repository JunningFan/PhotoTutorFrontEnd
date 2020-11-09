package com.example.phototutor.user;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.http.Url;

public class User {

    private int id;
    private String userName;
    private String nickName;
    private String signature;
    private URL avatarUrl;
    private int nFollowers;
    private int nFollowerings;

    public int getId() {
        return id;
    }

    public String getNickName() {
        return nickName;
    }

    public String getSignature() {
        return signature;
    }

    public URL getAvatarUrl() {
        return avatarUrl;
    }

    public int getnFollowers() {
        return nFollowers;
    }

    public int getnFollowerings() {
        return nFollowerings;
    }

    public User(int id, String userName,String nickName, String signature, String avatarUrl,
                int nFollowerings, int nFollowers) throws MalformedURLException {
        this.id=id;
        this.userName = userName;
        this.nickName = nickName;
        this.signature = signature;
        this.avatarUrl = new URL(avatarUrl);
        this.nFollowerings = nFollowerings;
        this.nFollowers = nFollowers;
    }
}
