package com.example.phototutor.Photo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.net.Uri;
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
    double latitude;
    double longitude;
}

@Entity(tableName = "photo_album")
public class Photo {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public Uri imageURI;
    public Uri thumbnailURI;
    public int iso;
    public int focal_length;
    public int aperture;
    public int shutter_speed;
    public Long timestamp;
    @Embedded
    public Coordinates location;
    public double orientation;
    public double elevation;

    @Ignore
    private Bitmap bitmap;
    @Ignore
    private Bitmap thumbnail;

    public Photo(){}
    public Photo(Bitmap bitmap, Long timestamp){
        setBitmap(bitmap);
        this.timestamp = timestamp;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.thumbnail = ThumbnailUtils.extractThumbnail(bitmap,64,64);
    }

    public boolean saveImage(File directory) {
        try {
            File path = new File(directory, String.valueOf(timestamp) + ".png");
            File thumbnailPath = new File(directory, String.valueOf(timestamp) + "_thumbnail.png");
            FileOutputStream out = new FileOutputStream(path);
            FileOutputStream thumbnail_out = new FileOutputStream(thumbnailPath);
            imageURI= Uri.fromFile(path);
            thumbnailURI = Uri.fromFile(thumbnailPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            thumbnail.compress(Bitmap.CompressFormat.JPEG,100,thumbnail_out);
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

    public boolean saveImage(String directory) {
        return saveImage(new File(directory));
    }
}
