package com.example.phototutor.Photo;

import android.graphics.Bitmap;
import android.net.Uri;

import com.example.phototutor.helpers.ServerClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class CloudPhoto extends Photo {
    public final static int LIKE = 1;
    public final static int NEUTRAL = 0;
    public final static int DISLIKE=2;

    private int userId;
    private String title;
    private int nLike;
    private int nDislike;
    private String description;
    private List<String> tags;
    private List<Integer> likedUserIds = new ArrayList<>();
    private List<Integer> dislikeUserIds = new ArrayList<>();
    private String weather;

    static public CloudPhoto createCloudPhotoFromJSON(JSONObject json) throws JSONException, MalformedURLException, URISyntaxException {
        CloudPhoto photo = new CloudPhoto();
        photo.id = json.getInt("ID");
        photo.title = json.getString("Title");
        photo.description = "";

        photo.userId = json.getInt("UserID");
        double longitude = json.getDouble("Lng");
        double latitude = json.getDouble("Lat");
        photo.setLocation(new Coordinates(latitude,longitude));
        photo.timestamp = getLongDataFromStringData(json.getString("Timestamp"));
        photo.lastModifiedTime = System.currentTimeMillis();

        photo.focal_length = json.getInt("FocalLength");

        photo.elevation = json.getDouble("Elevation");
        photo.orientation = json.getDouble("Orientation");

        photo.setWidth(json.getInt("Width"));
        photo.setHeight(json.getInt("Height"));
        photo.imageURI =  Uri.parse(new URL(
                ServerClient.getBaseURL() + '/' + json.getString("ImgBig")
        ).toURI().toString());
        photo.thumbnailURI = Uri.parse(new URL(
                ServerClient.getBaseURL() + '/' + json.getString("ImgSmall")
        ).toURI().toString());
        photo.nLike = json.getInt("NLike");
        photo.nDislike = json.getInt("NDislike");
        JSONArray jsonTags = json.getJSONArray("Tags");
        photo.tags = new ArrayList<>();
        photo.weather = json.getString("Weather");
        for(int i =0;i<jsonTags.length();i++)
            photo.tags.add(jsonTags.getJSONObject(i).getString("Name"));
        if(!json.isNull("Votes")){
            JSONArray votesJSONArray = json.getJSONArray("Votes");
            for(int i=0;i<votesJSONArray.length();i++){
                JSONObject votesObject = votesJSONArray.getJSONObject(i);
                if(votesObject.getBoolean("Like"))
                    photo.likedUserIds.add(votesObject.getInt("UID"));
                else
                    photo.dislikeUserIds.add(votesObject.getInt("UID"));
            }
        }
        photo.description = json.getString("Body");
        return photo;
    }

    static private long getLongDataFromStringData(String data){
        ZonedDateTime dateTime = ZonedDateTime.parse(data);
        return    dateTime.toInstant().toEpochMilli();
    }

    public String getTitle() {
        return title;
    }

    public int getnLike() {
        return nLike;
    }

    public List<String> getTags(){
        return tags;
    }

    public int getUserId() {
        return userId;
    }

    public int checkLiked(int userId){
        if(this.likedUserIds.contains(userId)) return LIKE;
        else if (this.dislikeUserIds.contains(userId)) return DISLIKE;
        else return NEUTRAL;
    }

    public int getnDislike() {
        return nDislike;
    }

    public String getWeather() {
        return weather;
    }

    public String getDescription() {
        return description;
    }
}
