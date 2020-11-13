package com.example.phototutor.Photo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import androidx.camera.core.ImageProxy;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;

class Coordinates {
    Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    double latitude;
    double longitude;
}

@Entity(tableName = "photo_album")
public class Photo {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public Uri imageURI;
    public Uri thumbnailURI;
    public int iso; // from 1 to IMT_MAX
    public int focal_length; // from 1 to INT_MAX, unit: millimeter
    public double aperture; // from 0 to DOUBLE_MAX
    public double shutter_speed; //Shutter speed (in unit of second or fraction of second) is defined as:
    // negative: integer part of longer/equal to 1 second, within range from DOUBLE_MIN inclusive to -1 inclusive;
    // positive: the fraction of 1, from 1 (exclusive) to DOUBLE_MAX, should always be an fraction of integers
    public Long timestamp;
    public Long lastModifiedTime;
    @Embedded
    public Coordinates location;
    public double orientation;
    public double elevation;

    // Weather typer constants
    public final static String CLEAR = "clear";
    public final static String PARTLY_CLOUDY = "partly_cloudy";
    public final static String MOSTLY_CLOUDY = "mostly_cloudy";
    public final static String OVERCAST = "overcast";
    public final static String RAIN = "raining";
    public final static String SNOW = "snow";
    public final static String MISTY = "misty";
    public final static String UNKNOWN = "unknown";

    public String weather;

    @Ignore
    private Bitmap bitmap;
    @Ignore
    private Bitmap thumbnail;
    @Ignore
    private int width;
    @Ignore
    private int height;

    public Photo(){}
    public Photo(Bitmap bitmap, Long timestamp){
        setBitmap(bitmap);
        this.timestamp = timestamp;
        this.iso = 1;
        this.focal_length = 6;
        this.aperture = 0.95;
        this.shutter_speed = -3.2;
        lastModifiedTime = timestamp;
    }

    public Photo(Bitmap bitmap, long currentTimeMillis, double latitude, double longitude, float orientationDegree, float orientationDegree1, String weather) {
        this(bitmap, currentTimeMillis, latitude, longitude, orientationDegree, orientationDegree1);
        this.weather = weather;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight(){
        return height;
    }

    public void setWidth(int width){
        this.width = width;
    }

    public void setHeight(int height){
        this.height = height;
    }
    public Photo(Bitmap bitmap, Long timestamp, double latitude, double longitude){
        this(bitmap, timestamp);
        setLocation(new Coordinates(latitude, longitude));
    }

    public Photo(Bitmap bitmap, Long timestamp, double latitude, double longitude,
                 double elevation, double orientation){
        this(bitmap, timestamp);
        setLocation(new Coordinates(latitude, longitude));
        this.elevation = elevation;
        this.orientation = orientation;
    }


    public Bitmap getBitmap() {
        return bitmap;
    }

    public Coordinates getLocation() {
        if(location != null) {
            return location;}
        return new Coordinates(-720, -720);
    }

    public double getLatitude() {
        return getLocation().latitude;
    }

    public double getLongitude() {
        return getLocation().longitude;
    }

    public void setLocation(Coordinates location) {
        this.location = location;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.thumbnail = ThumbnailUtils.extractThumbnail(bitmap,64,64);
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
    }

    public double getElevation() {
        return elevation;
    }

    public double getOrientation() {
        return orientation;
    }

    public boolean saveImage(File directory) {
        try {
            File path = new File(directory, String.valueOf(timestamp) + ".png");
            File thumbnailPath = new File(directory, String.valueOf(timestamp) + "_thumbnail.png");
            FileOutputStream out = new FileOutputStream(path,false);
            FileOutputStream thumbnail_out = new FileOutputStream(thumbnailPath,false);
            imageURI= Uri.fromFile(path);
            thumbnailURI = Uri.fromFile(thumbnailPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            thumbnail.compress(Bitmap.CompressFormat.PNG,100,thumbnail_out);
            out.close();
            thumbnail_out.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean updatePhotoBitmap(File directory,Bitmap newBitmap){
        setBitmap(newBitmap);
        this.lastModifiedTime = System.currentTimeMillis();
        return saveImage(directory);
    }

    public void delete(){
        new File(imageURI.getPath()).delete();
        new File(thumbnailURI.getPath()).delete();
        bitmap = null;
        thumbnailURI = null;
    }
}
