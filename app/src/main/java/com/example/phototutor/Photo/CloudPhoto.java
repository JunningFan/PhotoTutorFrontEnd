package com.example.phototutor.Photo;

import android.graphics.Bitmap;
import android.net.Uri;

import com.example.phototutor.helpers.ServerClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private int userId;
    private String title;
    private int nLike;
    private List<String> tags;
    static public CloudPhoto createCloudPhotoFromJSON(JSONObject json) throws JSONException, MalformedURLException, URISyntaxException {
        CloudPhoto photo = new CloudPhoto();
        photo.id = json.getInt("ID");
        photo.title = json.getString("Title");
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
        JSONArray jsonTags = json.getJSONArray("Tags");
        photo.tags = new ArrayList<>();
        for(int i =0;i<jsonTags.length();i++)
            photo.tags.add(jsonTags.getString(i));
        return photo;
    }

    static private long getLongDataFromStringData(String data){
        ZonedDateTime dateTime = ZonedDateTime.parse(data);
        return    dateTime.toInstant().toEpochMilli();
    }
}
